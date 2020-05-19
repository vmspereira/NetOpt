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

public class MCFMLUSolver {

	/**
	 * Implements the Multi-Commodity Flow (MCF) optimization taking as
	 * objective the Maximum Link Utilization (MLU)
	 */

	private NetworkTopology topology;
	private Demands demands;
	private NetworkLoads loads;
	private boolean saveLoads;

	public MCFMLUSolver(NetworkTopology topology, Demands demands) {
		this.topology = topology;
		this.demands = demands;
		this.setSaveLoads(false);
	}

	public double optimize() throws IloException {
		return this.optimize(topology, demands);
	}

	public double optimize(NetworkTopology topology, Demands demands) throws IloException {

		double[][] cp = topology.getNetGraph().createGraph().getCapacitie();
		double[][] d = demands.getDemands();
		double res = optimize(cp, d);
		return res;
	}

	/**
	 * 
	 * @param Capacity
	 *            matrix
	 * @param Demands
	 *            matrix
	 * @return the congestion measure Phi
	 * @throws IloException
	 */
	public double optimize(double[][] capacity, double[][] dem) throws IloException {

		IloCplex cplex = new IloCplex();
		cplex.setName("Multi commodity flow MLU optimization");
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

		// objective: minimize the mlu
		IloNumVar mlu = cplex.numVar(0, Double.MAX_VALUE, "mlu");

		IloLinearNumExpr obj = cplex.linearNumExpr();
		obj.addTerm(1, mlu);
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
						double dst = dem[s][t];
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

		// link loads are the sum of flows traveling over it,
		// l(a) = sum(fa(s,t)) for all s,t, s!=t
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

		// all links utilization are less or equal to mlu
		// l(a) - c(a) * mlu <=0
		for (int i = 0; i < arcs.getNumberOfArcs(); i++) {
			Arc a = arcs.getArc(i);
			IloLinearNumExpr exp = cplex.linearNumExpr();
			exp.addTerm(1, l[i]);
			exp.addTerm(-1 * a.getCapacity(), mlu);
			cplex.addLe(exp, 0);
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
			Simul simul = new Simul(topology);
			double congestion = simul.congestionMeasure(loads, this.demands);
			this.loads.setCongestion(congestion);
		}
		
		
		cplex.end();
		cplex.close();
		return res;
	}

	public boolean isSaveLoads() {
		return saveLoads;
	}

	public void setSaveLoads(boolean saveLoads) {
		this.saveLoads = saveLoads;
	}

	public NetworkLoads getNetworkLoads() {
		return this.loads;
	}

	public static void main(String[] args) {
		String nodesFile = args[0];
		String edgesFile = args[1];
		String demandsFile = args[2];

		try {
			NetworkTopology topology = new NetworkTopology(nodesFile, edgesFile);
			Demands demands = new Demands(topology.getDimension(), demandsFile);
			MCFMLUSolver solver = new MCFMLUSolver(topology, demands);
			solver.setSaveLoads(true);
			double res = solver.optimize();
			double congestion =solver.getNetworkLoads().getCongestion();
			System.out.println("Congestion = " + congestion);
			System.out.println("MLU = " + res);

		} catch (Exception e) {
		}

	}

}
