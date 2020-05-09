/*******************************************************************************
 * Copyright 2012-2019,
 *  Centro Algoritmi - University of Minho
 * 
 *  This is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This code is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Public License for more details.
 * 
 *  You should have received a copy of the GNU Public License
 *  along with this code.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  @author Vítor Pereira
 ******************************************************************************/

package pt.uminho.algoritmi.netopt.cplex;

import ilog.cplex.*;
import pt.uminho.algoritmi.netopt.cplex.utils.Arc;
import pt.uminho.algoritmi.netopt.cplex.utils.Arcs;
import pt.uminho.algoritmi.netopt.ospf.simulation.Demands;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkLoads;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.Simul;

import java.util.HashMap;
import java.util.List;
import ilog.concert.*;

public class MCFPhiSolver {

	private NetworkTopology topology;
	private Demands demands;
	private NetworkLoads loads;
	private boolean saveLoads;

	public MCFPhiSolver(NetworkTopology topology, Demands demands) {
		this.topology = topology;
		this.demands = demands;
		this.setSaveLoads(false);
	}

	public static void main(String[] args) {
		String nodesFile = args[0];
		String edgesFile = args[1];
		String demandsFile = args[2];

		try {
			NetworkTopology topology = new NetworkTopology(nodesFile, edgesFile);
			Demands demands = new Demands(topology.getDimension(), demandsFile);
			MCFPhiSolver solver = new MCFPhiSolver(topology, demands);
			solver.setSaveLoads(true);
			solver.optimize();
			NetworkLoads loads= solver.getNetworkLoads();
			System.out.println("Congestion = "+loads.getCongestion());
			System.out.println("MLU = "+loads.getMLU());
		} catch (Exception e) {
		}

	}

	public double optimize() throws IloException {
		return this.optimize(this.topology, this.demands);
	}

	/**
	 * 
	 * @param Network
	 *            Topology
	 * @param Demands
	 *            matrix
	 * @return the normalized congestion measure Phi
	 * @throws IloException
	 */
	public double optimize(NetworkTopology topology, Demands demands) throws IloException {
		double[][] cp = topology.getNetGraph().createGraph().getCapacitie();
		double[][] d = demands.getDemands();
		double res = optimize(cp, d);
		Simul s = new Simul(topology);
		double uncap = s.phiUncap(demands);
		double normalized = res / uncap;
		return normalized;
	}

	/**
	 * 
	 * @param Capacity
	 *            matrix
	 * @param Demands
	 *            matrix
	 * @return the unnormalized congestion measure Phi
	 * @throws IloException
	 */
	public double optimize(double[][] capacity, double[][] demands) throws IloException {

		IloCplex cplex = new IloCplex();
		cplex.setName("Multi commodity flow Phi optimization");
		// for each arc a matrix of variables fa[s][t] identify the amout of
		// traffic
		// with source s and destination t travels over a.
		HashMap<Arc, IloNumVar[][]> fa = new HashMap<Arc, IloNumVar[][]>();
		Arcs arcs = new Arcs();
		// number of nodes
		int n = capacity.length;
		int k = 0; // just for naming
		for (int i = 0; i < n; i++)
			for (int j = 0; j < n; j++)
				if (capacity[i][j] > 0) {
					Arc a = new Arc(i, j, capacity[i][j]);
					arcs.add(a);
					IloNumVar f[][] = new IloNumVar[n][n];
					for (int u = 0; u < n; u++)
						for (int v = 0; v < n; v++)
							f[u][v] = cplex.numVar(0, Double.MAX_VALUE, "f" + k + "_" + u + "_" + v);
					fa.put(a, f);
					k++;
				}
		// number of arcs
		int m = arcs.getNumberOfArcs();

		// the l(a) variables, load of arc a
		IloNumVar l[] = new IloNumVar[m];
		for (int i = 0; i < m; i++)
			l[i] = cplex.numVar(0, Double.MAX_VALUE, "l_" + i);

		// the Phi(a) variables
		IloNumVar phi[] = new IloNumVar[m];
		for (int i = 0; i < m; i++)
			phi[i] = cplex.numVar(0, Double.MAX_VALUE, "Phi_" + i);

		// objective: minimize the sum of all Phi(a)
		IloLinearNumExpr obj = cplex.linearNumExpr();
		for (int i = 0; i < m; i++)
			obj.addTerm(1, phi[i]);
		cplex.addMinimize(obj);

		// constraints
		// flow conservation

		// for all nodes v
		for (int v = 0; v < n; v++) {
			// list of arcs that arrive at v and start at v
			List<Arc> toV = arcs.getAllArcsTo(v);
			List<Arc> fromV = arcs.getAllArcsFrom(v);
			// for each source and destination (s,t) s!=t
			for (int s = 0; s < n; s++) {
				for (int t = 0; t < n; t++) {
					if (s != t) {
						double dst = demands[s][t];
						IloLinearNumExpr ev = cplex.linearNumExpr();
						// sum( fuv (s,t)
						for (Arc a : toV) {
							IloNumVar f[][] = fa.get(a);
							ev.addTerm(1, f[s][t]);
						}
						// - sum( fvu (s,t)
						for (Arc a : fromV) {
							IloNumVar f[][] = fa.get(a);
							ev.addTerm(-1, f[s][t]);
						}
						// if v is a producer, consumer or transient node
						if (v == s)
							cplex.addEq(ev, -1 * dst);
						else if (v == t)
							cplex.addEq(ev, dst);
						else
							cplex.addEq(ev, 0);
					}
				}

			}

		}

		// link loads are the sum of flows traveling over it, l(a) =
		// sum(fa(s,t)) for all s,t, s!=t
		for (int i = 0; i < arcs.getNumberOfArcs(); i++) {
			Arc a = arcs.getArc(i);
			IloLinearNumExpr la = cplex.linearNumExpr();
			IloNumVar f[][] = fa.get(a);
			for (int s = 0; s < n; s++) {
				for (int t = 0; t < n; t++) {
					if (s != t) {
						la.addTerm(1, f[s][t]);
					}
				}
			}
			cplex.addEq(l[i], la);
		}

		// Convex piecewise linear function Phi
		// As the problem is a minimization problem, the penalizing function can
		// be defined as a set of constraints for each arc
		double[] points = new double[6];
		points[0] = 0.0;
		points[1] = 2.0 / 3;
		points[2] = 16.0 / 3;
		points[3] = 178.0 / 3;
		points[4] = 1468.0 / 3;
		points[5] = 16318.0 / 3;
		double[] slopes = new double[] { 1, 3, 10, 70, 500, 5000 };

		for (int i = 0; i < arcs.getNumberOfArcs(); i++) {
			Arc a = arcs.getArc(i);
			for (int j = 0; j < points.length; j++) {
				IloLinearNumExpr exp = cplex.linearNumExpr();
				exp.addTerm(1, phi[i]);
				exp.addTerm(-1 * slopes[j], l[i]);
				double b = -1 * points[j] * a.getCapacity();
				cplex.addGe(exp, b);
			}
		}

		// Saves the model
		// cplex.exportModel("lpex1.lp");

		// Solve
		cplex.solve();
		double res = cplex.getObjValue();
		
		if (this.saveLoads) {
			double[][] u = new double[topology.getDimension()][topology.getDimension()];
			for (int i = 0; i < m; i++) {
				double utilization = cplex.getValue(l[i]); 
				int src = arcs.getArc(i).getFromNode();
				int dst = arcs.getArc(i).getToNode();
				u[src][dst] = utilization;
			}
			this.loads = new NetworkLoads(u,topology);
			this.loads.printLoads();
			Simul simul = new Simul(topology);
			double congestion = simul.congestionMeasure(loads, this.demands);
			System.out.println(congestion);
			this.loads.setCongestion(congestion);
		}
		
		cplex.end();
		return res;
	}

	public boolean isSaveLoads() {
		return saveLoads;
	}

	/**
	 * Defines if besides the congestion value, network link loads are also
	 * saved as an instance of NetworkLoads
	 * 
	 * @param boolean
	 *            saveLoads
	 */
	public void setSaveLoads(boolean saveLoads) {
		this.saveLoads = saveLoads;
	}

	public NetworkLoads getNetworkLoads() {
		return this.loads;
	}

}
