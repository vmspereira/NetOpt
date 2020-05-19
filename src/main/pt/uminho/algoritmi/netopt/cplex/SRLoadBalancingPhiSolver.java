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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.Status;
import pt.uminho.algoritmi.netopt.cplex.utils.Arc;
import pt.uminho.algoritmi.netopt.cplex.utils.Arcs;
import pt.uminho.algoritmi.netopt.cplex.utils.PathConfiguration;
import pt.uminho.algoritmi.netopt.cplex.utils.SourceDestinationPair;
import pt.uminho.algoritmi.netopt.ospf.graph.Graph;
import pt.uminho.algoritmi.netopt.ospf.simulation.Demands;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkLoads;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.OSPFWeights;
import pt.uminho.algoritmi.netopt.ospf.simulation.Simul;
import pt.uminho.algoritmi.netopt.ospf.simulation.exception.DimensionErrorException;
import pt.uminho.algoritmi.netopt.ospf.simulation.loadballancer.ECMPLoadBalancer;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetNode;
import pt.uminho.algoritmi.netopt.ospf.simulation.simulators.SRSimul;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.LabelPath;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.SRConfiguration;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.SRNodeConfiguration;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.SRSimulator;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.Segment;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.Segment.SegmentType;

public class SRLoadBalancingPhiSolver {

	/**
	 * Given a SR paths configuration optimizes Phi by correcting traffic load
	 * balancing between parallel SR paths.
	 */

	NetworkTopology topology;
	OSPFWeights weights;
	SRConfiguration srConf;
	Demands demands;

	NetworkLoads loads;
	boolean saveLoads;
	boolean updateConfiguration;
	Status status;
	boolean debug = false;

	public SRLoadBalancingPhiSolver(NetworkTopology topo, OSPFWeights weights, SRConfiguration srConf,
			Demands demands) {
		this.topology = topo;
		this.weights = weights;
		this.demands = demands;
		// should be a copy
		this.srConf = srConf;
		this.saveLoads = false;
		this.updateConfiguration = false;
	}

	public SRLoadBalancingPhiSolver(NetworkTopology topo, OSPFWeights weights, Demands d)
			throws DimensionErrorException {
		topo.applyWeights(weights);
		SRSimul simul = new SRSimul(topo);
		this.topology = topo;
		this.weights = weights;
		this.demands = d;
		this.srConf = simul.getSRConfigurationLP();
		this.saveLoads = false;
		this.updateConfiguration = false;
	}

	public double optimize() throws IloException, DimensionErrorException {
		return this.optimize(this.topology, this.demands, this.weights, this.srConf);
	}

	private double optimize(NetworkTopology topology, Demands demands, OSPFWeights weights, SRConfiguration srConf)
			throws IloException, DimensionErrorException {
		// containers
		double[][] capacity = topology.getNetGraph().createGraph().getCapacitie();
		HashMap<SourceDestinationPair, List<PathConfiguration>> paths = new HashMap<SourceDestinationPair, List<PathConfiguration>>();
		HashMap<Arc, IloLinearNumExpr> fa = new HashMap<Arc, IloLinearNumExpr>();
		Arcs arcs = new Arcs();

		// for debug
		double[][] loads = new double[capacity.length][capacity.length];

		// cplex model
		IloCplex cplex = new IloCplex();
		cplex.setName("Optimization of traffic load balancing between parallel SR paths");
		if (!debug)
			cplex.setOut(null);
		// number of nodes
		int n = topology.getDimension();

		// Variables ********************************************

		// for each arc a matrix of variables fa[s][t] identify the amount of
		// traffic with source s and destination t travels over a.
		// Amount of traffic from s to t over a
		for (int i = 0; i < n; i++)
			for (int j = 0; j < n; j++)
				if (capacity[i][j] > 0) {
					Arc a = new Arc(i, j, capacity[i][j]);
					arcs.add(a);
					fa.put(a, cplex.linearNumExpr());
				}

		// the l(a) variables, load of arc a
		HashMap<Arc, IloNumVar> link_load = new HashMap<Arc, IloNumVar>();
		for (int i = 0; i < arcs.getNumberOfArcs(); i++) {
			Arc a = arcs.getArc(i);
			link_load.put(a, cplex.numVar(0, Double.MAX_VALUE, "load_" + a.getFromNode() + "_" + a.getToNode() + ""));
		}

		// distributes traffic over links
		Collection<SRNodeConfiguration> conf = srConf.getNodesConfigurations();
		for (SRNodeConfiguration nc : conf) {
			Collection<ArrayList<LabelPath>> u = nc.getConfiguration().values();
			for (ArrayList<LabelPath> a : u) {
				for (LabelPath b : a) {
					int s = b.getSource().getNodeId();
					int t = b.getDestination().getNodeId();
					SourceDestinationPair st = new SourceDestinationPair(s, t);
					int k;
					List<PathConfiguration> lps;
					if (!paths.containsKey(st)) {
						lps = new ArrayList<PathConfiguration>();
						paths.put(st, lps);
						k = 0;
					} else {
						lps = paths.get(st);
						k = lps.size();
					}
					IloNumVar alpha = cplex.numVar(0, 1, IloNumVarType.Float, "a_" + s + "_" + t + "_" + k);
					lps.add(new PathConfiguration(alpha, b));
					// adds the aggregated flows
					try {
						topology.applyWeights(weights);
						double[][] l = new double[topology.getDimension()][topology.getDimension()];
						Iterator<Segment> it = b.getIterator();
						while (it.hasNext()) {
							Segment segment = it.next();
							if (segment.getType().equals(SegmentType.ADJ)) {
								l[segment.getSrcNodeId()][segment.getDstNodeId()] = demands.getDemands(s, t);

							} else if (segment.getType().equals(SegmentType.NODE)) {
								int dest = segment.getDstNodeId();
								Graph g = topology.getShortestPathGraph().getArcsShortestPath(dest);
								ECMPLoadBalancer lb = new ECMPLoadBalancer(g);
								ArrayList<Integer> nodes = new ArrayList<Integer>(
										topology.getShortestPathGraph().getNodesForDest(dest));
								Comparator<Integer> comparator = new Comparator<Integer>() {
									double[][] dists = topology.getShortestPathGraph().getShortestPathDistances();

									@Override
									public int compare(Integer arg0, Integer arg1) {
										double d0 = dists[arg0][dest];
										double d1 = dists[arg1][dest];
										if (d0 - d1 > 0)
											return -1;
										else if (d0 == d1)
											return 0;
										else
											return 1;
									}
								};
								nodes.sort(comparator);
								// finds the segment src node in the spanning
								// tree
								while (nodes.get(0) != segment.getSrcNodeId())
									nodes.remove(0);
								while (nodes.size() > 0) {
									int v = nodes.get(0);
									double sum = 0.0;
									if (v == segment.getSrcNodeId())
										sum += demands.getDemands(s, t);
									else {
										for (int u1 = 0; u1 < topology.getDimension(); u1++)
											if (topology.getNetGraph().existEdge(u1, v)
													&& topology.getNetGraph().getEdge(u1, v).isUP())
												sum += l[u1][v];
									}
									// for each arc leaving from this node
									for (int w = 0; w < topology.getDimension(); w++) {
										if (topology.getNetGraph().existEdge(v, w)
												&& topology.getNetGraph().getEdge(v, w).isUP()) {
											l[v][w] = lb.getSplitRatio(v, dest, v, w) * sum;
										}
									}
									nodes.remove(0);
								}
							}
						}

						// adds the obtained loads
						for (int x = 0; x < n; x++)
							for (int y = 0; y < n; y++)
								if (l[x][y] > 0) {
									Arc arc = arcs.getArc(x, y);
									fa.get(arc).addTerm(l[x][y], alpha);
									if (debug)
										loads[arc.getFromNode()][arc.getToNode()] += l[x][y] * b.getFraction();
								}

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

		// the Phi(a) variables
		HashMap<Arc, IloNumVar> phi = new HashMap<Arc, IloNumVar>();
		for (int i = 0; i < arcs.getNumberOfArcs(); i++) {
			Arc a = arcs.getArc(i);
			phi.put(a, cplex.numVar(0, Double.MAX_VALUE, "phi_" + a.getFromNode() + "_" + a.getToNode() + ""));
		}

		if (debug) {
			// verify the if load matrices are equivalent
			SRSimul simul = new SRSimul(topology);
			simul.setConfigureSRPath(true);
			simul.computeLoads(weights, demands);
			double[][] lda1 = simul.getLoads().getLoads();
			DecimalFormat df = new DecimalFormat("##.##");
			for (int i = 0; i < n; i++) {
				System.out.println();
				for (int j = 0; j < n; j++) {
					System.out.print(" " + df.format((lda1[i][j] - loads[i][j])));
				}
			}
		}

		// Objective ***************************************************** //
		// objective: minimize the sum of all Phi(a)
		IloLinearNumExpr obj = cplex.linearNumExpr();
		for (int i = 0; i < arcs.getNumberOfArcs(); i++) {
			Arc a = arcs.getArc(i);
			obj.addTerm(1, phi.get(a));
		}
		cplex.addMinimize(obj);

		// Constraints ************************************************** //

		// The sum of fractions of traffic with a same source and destination is
		// equal to one
		Iterator<SourceDestinationPair> it = paths.keySet().iterator();
		while (it.hasNext()) {
			SourceDestinationPair pair = it.next();
			List<PathConfiguration> lpc = paths.get(pair);
			IloLinearNumExpr exp = cplex.linearNumExpr();
			for (PathConfiguration pc : lpc) {
				exp.addTerm(1, pc.getFractionVar());
			}
			cplex.addEq(exp, 1);
		}

		// Link loads are the sum of flows traveling over it,
		// l(a) = sum(fa(s,t)) for all s,t, s!=t
		for (int i = 0; i < arcs.getNumberOfArcs(); i++) {
			Arc a = arcs.getArc(i);
			cplex.addEq(link_load.get(a), fa.get(a));
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
				exp.addTerm(1, phi.get(a));
				exp.addTerm(-1 * slopes[j], link_load.get(a));
				double b = -1 * points[j] * a.getCapacity();
				cplex.addGe(exp, b);
			}
		}

		cplex.solve();
		double res = cplex.getObjValue();
		status = cplex.getStatus();

		if (this.saveLoads) {
			double[][] l = new double[topology.getDimension()][topology.getDimension()];
			for (int i = 0; i < arcs.getNumberOfArcs(); i++) {
				Arc a = arcs.getArc(i);
				l[a.getFromNode()][a.getToNode()] = cplex.getValue(link_load.get(a));
			}
			this.loads = new NetworkLoads(l, topology);
			Simul simul = new Simul(topology);
			double congestion = simul.congestionMeasure(this.loads, this.demands);
			this.loads.setCongestion(congestion);
		}

		if (updateConfiguration) {
			DecimalFormat df = new DecimalFormat("##.####");
			for (int i = 0; i < n; i++)
				for (int j = 0; j < n; j++) {
					if (i != j) {
						NetNode source = topology.getNetGraph().getNodeAt(i);
						NetNode destination = topology.getNetGraph().getNodeAt(j);
						ArrayList<LabelPath> p = srConf.getSRPaths(source, destination);
						if (p.size() > 0) {
							// there was at least one configured SR path from i
							// to j
							// System.out.println();
							SourceDestinationPair key = new SourceDestinationPair(i, j);
							List<PathConfiguration> list = paths.get(key);
							for (int k = 0; k < list.size(); k++) {
								PathConfiguration pc = list.get(k);
								LabelPath lp = p.get(k);
								double old_alpha = lp.getFraction();
								double new_alpha = cplex.getValue(pc.getFractionVar());
								lp.setFraction(new_alpha);
								if (debug) {
									System.out.print(pc.getFractionVar().getName());
									System.out.println("::\t" + df.format(old_alpha) + "=>" + df.format(new_alpha));
								}
							}

						}
					}
				}
		}
		cplex.end();
		cplex.close();
		return res;
	}

	public void setSaveLoads(boolean saveLoads) {
		this.saveLoads = saveLoads;
	}

	public void setUpdateConfiguration(boolean b) {
		updateConfiguration = b;
	}

	public SRConfiguration getSRConfiguration() {
		return this.srConf;
	}

	public void setDebug(boolean b) {
		this.debug = b;
	}

	public static void main(String[] args) {
		String nodesFile = args[0];
		String edgesFile = args[1];
		String demandsFile = args[2];
		String weightsFile = args[3];

		try {
			NetworkTopology topology = new NetworkTopology(nodesFile, edgesFile);
			Demands demands = new Demands(topology.getDimension(), demandsFile);
			OSPFWeights w = new OSPFWeights(topology.getDimension());
			w.readOSPFWeights(topology.getDimension(), weightsFile);
			topology.applyWeights(w);
			SRSimul simul = new SRSimul(topology);
			simul.setConfigureSRPath(true);
			simul.computeLoads(w, demands);
			double cong_before = simul.getLoads().getCongestion();
			double mlu_before = simul.getLoads().getMLU();

			SRConfiguration srConf = simul.getSRconfiguration();
			SRSimulator sr = new SRSimulator(topology);
			sr.setWeights(w);
			sr.apply(srConf, demands);
			double b_mlu_cnf = sr.getMLU();
			double b_cong_cnf = sr.getCongestionValue();
			SRLoadBalancingPhiSolver solver = new SRLoadBalancingPhiSolver(topology, w, srConf, demands);
			solver.setSaveLoads(true);
			solver.optimize();
			double congestion_after = solver.getNetworLoads().getCongestion();
			double mlu_after = solver.getNetworLoads().getMLU();

			sr.clear();
			sr.apply(srConf, demands);
			double a_cong_cnf = sr.getCongestionValue();
			double a_mlu_cnf = sr.getMLU();

			System.out.println("=======================\n" + "Initial congestion:" + cong_before + " (" + b_cong_cnf
					+ "\nInitial MLU:\t" + mlu_before + " (" + b_mlu_cnf + "\nCongestion after:" + congestion_after
					+ " (" + a_cong_cnf + "\nMLU after:\t\t" + mlu_after + " (" + a_mlu_cnf);

			System.out.println("=======================\n");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public NetworkLoads getNetworLoads() {
		return loads;
	}

	public boolean hasSolution() {
		return this.status == IloCplex.Status.Optimal || this.status == IloCplex.Status.Feasible;
	}

}
