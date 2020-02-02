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
import ilog.concert.*;

public class MCFMLUSolver2 {

	/**
	 * Implements the Multi-Commodity Flow (MCF) optimization taking as
	 * objective the Maximum Link Utilization (MLU)
	 */

	private NetworkTopology topology;
	private Demands demands;
	private NetworkLoads loads;
	private boolean saveLoads;

	final private double scalingFactor = 1024;

	public MCFMLUSolver2(NetworkTopology topology, Demands demands) {
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

		int nNodes = capacity.length;
		Arcs arcs = new Arcs();
		// number of nodes
		int n = capacity.length;
		for (int i = 0; i < n; i++)
			for (int j = 0; j < n; j++)
				if (capacity[i][j] > 0) {
					Arc a = new Arc(i, j, capacity[i][j]);
					int index = arcs.add(a);
					a.setIndex(index);
				}
		// number of arcs
		int nEdges = arcs.getNumberOfArcs();

		IloNumVar maxUtilization;
		IloNumVar[] load;
		IloNumVar[][] loadToDest;

		loadToDest = new IloNumVar[nNodes][];
		for (int dest = 0; dest < nNodes; dest++) {
			loadToDest[dest] = cplex.numVarArray(nEdges, 0, Double.MAX_VALUE);
		}

		load = cplex.numVarArray(nEdges, 0, Double.MAX_VALUE);

		maxUtilization = cplex.numVar(0, Double.MAX_VALUE, "mlu");
		// objective
		IloLinearNumExpr obj = cplex.linearNumExpr();
		obj.addTerm(1, maxUtilization);
		cplex.addMinimize(obj);

		// Sum partial loads = total maxLinkLoad
		for (int edge = 0; edge < nEdges; edge++) {
			IloLinearNumExpr expr = cplex.linearNumExpr();
			for (int dest = 0; dest < nNodes; dest++) {
				expr.addTerm(1.0, loadToDest[dest][edge]);
			}
			cplex.addEq(expr, load[edge]);
		}

		for (int dest = 0; dest < nNodes; dest++) {
			for (int node = 0; node < nNodes; node++) {
				IloLinearNumExpr expr = cplex.linearNumExpr();
				for (Arc edge : arcs.getAllArcsTo(node))
					expr.addTerm(-1, loadToDest[dest][edge.getIndex()]);
				for (Arc edge : arcs.getAllArcsFrom(node))
					expr.addTerm(1, loadToDest[dest][edge.getIndex()]);

				if (node != dest) {
					cplex.addEq(expr, dem[node][dest] / scalingFactor);
				}
			}
		}
		
		// simplify the problem a little: out edges of destination should have no maxLinkLoad in destination's partial maxLinkLoad graph
	    for (int dest = 0; dest < nNodes; dest++) {
	      for (Arc edge : arcs.getAllArcsFrom(dest)) {
	    	  cplex.addEq(loadToDest[dest][edge.getIndex()], 0);
	      }
	    }
	    
	    // Links flow, capacity and maxUsage
	    
	    for (Arc edge : arcs) {
	      IloLinearNumExpr expr = cplex.linearNumExpr();
	      expr.addTerm(edge.getCapacity() / scalingFactor, maxUtilization);
	      cplex.addGe(expr,load[edge.getIndex()]);
	    }

	    cplex.solve();
		double res = cplex.getObjValue();

		if (this.saveLoads) {
			double[][] u = new double[topology.getDimension()][topology.getDimension()];
			for (int i = 0; i < nEdges; i++) {
				double utilization = cplex.getValue(load[i]); 
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
			MCFMLUSolver2 solver = new MCFMLUSolver2(topology, demands);
			solver.setSaveLoads(true);
			double res = solver.optimize();
			double congestion = solver.getNetworkLoads().getCongestion();
			System.out.println("Congestion = " + congestion);
			System.out.println("MLU = " + res);

		} catch (Exception e) {
		}

	}

}
