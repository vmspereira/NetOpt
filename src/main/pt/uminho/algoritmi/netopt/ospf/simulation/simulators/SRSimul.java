/*******************************************************************************
 * Copyright 2012-2017,
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
package pt.uminho.algoritmi.netopt.ospf.simulation.simulators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import pt.uminho.algoritmi.netopt.ospf.graph.Graph;
import pt.uminho.algoritmi.netopt.ospf.simulation.Demands;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkLoads;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.OSPFWeights;
import pt.uminho.algoritmi.netopt.ospf.simulation.PValues;
import pt.uminho.algoritmi.netopt.ospf.simulation.Simul;
import pt.uminho.algoritmi.netopt.ospf.simulation.exception.DimensionErrorException;
import pt.uminho.algoritmi.netopt.ospf.simulation.loadballancer.ECMPLoadBalancer;
import pt.uminho.algoritmi.netopt.ospf.simulation.loadballancer.GammaLoadBalancer;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetEdge;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetNode;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.Flow;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.LabelPath;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.SRConfiguration;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.SRNodeConfiguration;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.Segment;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.Segment.SegmentType;
import pt.uminho.algoritmi.netopt.ospf.utils.IntPair;

@SuppressWarnings("serial")
public class SRSimul extends Simul {

	// defines whether to use DEFT/PEFT
	private SRConfiguration SRconfig;
	private boolean configureSRPath;

	public SRSimul(NetworkTopology topology, LoadBalancer lb) {
		super(topology);
		this.setLoadBalancer(lb);
		configureSRPath = false;
	}

	public SRSimul(NetworkTopology topology) {
		this(topology, LoadBalancer.DEFT);
	}

	/**
	 * Computes loads for given configuration an SR Configuration and link
	 * weights configuration. If edgereroute is true when a SR is invalid,
	 * traffic is routed from source to destination by SP, otherwise traffic
	 * follows SP from the point where the invalid configuration is found to the
	 * next valid node.
	 * 
	 * @param demands
	 * @param configuration
	 * @return loads
	 * @throws DimensionErrorException
	 */
	public double[][] totalLoads(Demands demands, OSPFWeights weights, SRConfiguration configuration,
			boolean edgeReroute) throws DimensionErrorException {
		topology.applyWeights(weights);
		topology.shortestDistances();
		int dimension = this.topology.getDimension();

		LoadBalancer lb = this.getLoadBalancer();

		this.setLoadBalancer(LoadBalancer.ECMP);
		// distributes traffic traveling along SP
		Demands dem = configuration.getShortestPathDemands(demands, this.topology, edgeReroute);
		double[][] tLoads = new double[dimension][dimension];
		// SP loads (Node-Segments and failing ADJ-Segments traffic)
		for (int d = 0; d < dimension; d++) {
			if (topology.getGraph().inDegree(d) > 0) {
				double[][] ploads = super.partialLoads(d, dem);
				for (int i = 0; i < dimension; i++)
					for (int j = 0; j < dimension; j++)
						tLoads[i][j] += ploads[i][j];
			}
		}
		// Adj. segments load on non failing links

		for (SRNodeConfiguration nc : configuration.getNodesConfigurations()) {
			Collection<ArrayList<LabelPath>> u = nc.getConfiguration().values();
			for (ArrayList<LabelPath> a : u) {
				for (LabelPath b : a) {
					if (!b.isShortestPath()) {
						double d = b.getFraction()
								* demands.getDemands(b.getSource().getNodeId(), b.getDestination().getNodeId());
						for (Segment s : b.getLabels()) {
							if (s.getType().equals(SegmentType.ADJ)) {
								NetEdge e = topology.getNetGraph().getEdge(s.getSrcNodeId(), s.getDstNodeId());
								if (e.isUP()) {
									tLoads[s.getSrcNodeId()][s.getDstNodeId()] += d;
								}
							}
						}
					}
				}
			}
		}

		// set load balancer to the previous configuration
		this.setLoadBalancer(lb);
		return tLoads;
	}

	public double[][] totalLoads(Demands demands, SRConfiguration configuration) {

		int dimension = this.topology.getDimension();
		double[][] tLoads = new double[dimension][dimension];

		Collection<SRNodeConfiguration> n = configuration.getNodesConfigurations();
		for (SRNodeConfiguration nc : n) {
			Collection<ArrayList<LabelPath>> u = nc.getConfiguration().values();
			for (ArrayList<LabelPath> a : u) {
				for (LabelPath path : a) {
					int s = path.getSource().getNodeId();
					int t = path.getDestination().getNodeId();
					Flow flow = new Flow(s, t, Flow.FlowType.SALP);
					flow.setFraction(path.getFraction());
					double d = path.getFraction() * demands.getDemands(s, t);
					flow.setDemand(d);
					// adds the flow
					// correctPath(flow,path);
					double[][] l = new double[this.topology.getDimension()][this.topology.getDimension()];
					Iterator<Segment> it = path.getIterator();
					while (it.hasNext()) {
						Segment segment = it.next();
						if (segment.getType().equals(SegmentType.ADJ)) {
							l[segment.getSrcNodeId()][segment.getDstNodeId()] += flow.getDemand();

						} else if (segment.getType().equals(SegmentType.NODE)) {
							int dest = segment.getDstNodeId();
							Graph g = topology.getShortestPathGraph().getArcsShortestPath(dest);
							ECMPLoadBalancer lb1 = new ECMPLoadBalancer(g);
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
							// finds the segment src node in the spanning tree
							while (nodes.get(0) != segment.getSrcNodeId())
								nodes.remove(0);
							while (nodes.size() > 0) {
								int v = nodes.get(0);
								double sum = 0.0;
								if (v == segment.getSrcNodeId())
									sum += flow.getDemand();
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
										l[v][w] = lb1.getSplitRatio(v, dest, v, w) * sum;
									}
								}
								nodes.remove(0);
							}
						}
					}

					for (int i = 0; i < topology.getDimension(); i++)
						for (int j = 0; j < topology.getDimension(); j++)
							tLoads[i][j] += l[i][j];

				}
			}
		}
		return tLoads;
	}

	/**
	 * compute loads in the graph using partialLoads method; sums all loads; to
	 * get results do getLoads
	 */
	@Override
	public double[][] totalLoads(Demands demands) {
		return this.totalLoads(null, demands);
	}

	public double[][] totalLoads(double[] pvalues, Demands demands) {
		int dimension = topology.getDimension();
		if (this.configureSRPath) {
			this.SRconfig = new SRConfiguration();
		}
		double[][] tLoads = new double[dimension][dimension];

		for (int d = 0; d < dimension; d++) {
			if (topology.getGraph().inDegree(d) > 0) {
				double[][] ploads = this.partialLoads(d, demands, pvalues);
				for (int i = 0; i < topology.getDimension(); i++)
					for (int j = 0; j < topology.getDimension(); j++)
						tLoads[i][j] += ploads[i][j];
			}
		}

		// double[][] tLoads = this.totalLoads(demands,
		// this.getSRconfiguration());
		return tLoads;
	}

	public SRConfiguration getSRConfigurationLP() {
		int dimension = topology.getDimension();
		SRConfiguration config = new SRConfiguration();
		for (int d = 0; d < dimension; d++)
			this.partialSRConfig(d, config);
		return config;
	}

	
	@Override
	public double[][] partialLoads(int dest, Demands demands) {
		return this.partialLoads(dest, demands, null);
	}

	/**
	 * 
	 * @param dest
	 * @param demands
	 * @param pvalues
	 * @return
	 */
	public double[][] partialLoads(int dest, Demands demands, double[] pvalues) {

		// traffic demands to be routed by SP/ECMP
		double[] spDemands = new double[this.topology.getDimension()];
		// initialize partial loads
		double[][] ploads = new double[topology.getDimension()][topology.getDimension()];
		for (int i = 0; i < topology.getDimension(); i++) {
			spDemands[i] = 0.0;
			for (int j = 0; j < topology.getDimension(); j++)
				ploads[i][j] = 0.0;
		}

		// node on SP sorted by distance to destination
		ArrayList<Integer> nodes = new ArrayList<Integer>(topology.getShortestPathGraph().getNodesForDest(dest));
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
		GammaLoadBalancer gam = new GammaLoadBalancer(this.topology, dest, this.getLoadBalancer(), pvalues);
		gam.computeGamma();

		Iterator<Integer> it = nodes.iterator();

		// for all nodes on SP
		while (it.hasNext()) {

			int src = it.next();
			List<Integer> nodesOnPaths = topology.getShortestPathGraph().getNodesOnShortestPaths(src, dest, false);

			// list of all nsp edges on the paths to destination
			ArrayList<IntPair> nspEdges = new ArrayList<IntPair>();
			for (int node : nodesOnPaths) {
				List<IntPair> nse = gam.getNSPEdgesEndWithSStart(node, true);
				nspEdges.addAll(nse);
			}

			// For each nsp edge, an alternative path will be available to
			// forward traffic
			// To compute the amount of traffic to be forward by nsp paths from
			// source to destination.

			Iterator<IntPair> nspEdgesIterator = nspEdges.iterator();
			// sum of splits
			double ssplit = 0.0;
			while (nspEdgesIterator.hasNext()) {

				IntPair p = nspEdgesIterator.next();
				double sumBefore = 0;

				if (p.getX() == src) {
					sumBefore = 1;
				} else {
					List<Vector<Integer>> spBefore = topology.getShortestPathGraph().getAllPaths(src, p.getX());
					for (int j = 0; j < spBefore.size(); j++) {
						Vector<Integer> v = spBefore.get(j);
						double f = 1.0;
						for (int i = 0; i < v.size(); i++) {
							if (v.get(i) != p.getX()) {
								f *= gam.getSplit(v.get(i), v.get(i + 1));
							}
						}
						sumBefore += f;
					}
				}

				double split = sumBefore * gam.getSplit(p.getX(), p.getY());

				// Although threshold imposition is ALREADY DONE IN GAMMA
				// here the threshold is applied to the path traffic fration
				if (!this.filterDeftThreshold || split > this.deftThreshold) {

					ssplit += split;

					NetNode dstNode = this.topology.getNetGraph().getNodeAt(dest);
					NetNode srcNode = this.topology.getNetGraph().getNodeAt(src);
					LabelPath lsp = new LabelPath(srcNode, dstNode);
					lsp.setFraction(split);
					if (p.getX() != src) {
						Segment segment = new Segment(p.getXString(), SegmentType.NODE);
						segment.setSrcNodeId(srcNode.getNodeId());
						segment.setDstNodeId(p.getX());
						lsp.addSegment(segment);
					}
					Segment s = new Segment(p.toString(), SegmentType.ADJ);
					s.setDstNodeId(p.getY());
					s.setSrcNodeId(p.getX());
					lsp.addSegment(s);
					if (p.getY() != dest) {
						Segment segment = new Segment(dstNode.toString(), SegmentType.NODE);
						segment.setSrcNodeId(p.getY());
						segment.setDstNodeId(dstNode.getNodeId());
						lsp.addSegment(segment);
					}
					if (this.configureSRPath) {
						SRconfig.addLabelPath(srcNode, dstNode, lsp);
					}

					// Demand to be forward between src and dst by NSP p
					double dem = demands.getDemands(src, dest) * split;
					// Distributes traffic demand between src and dst by NSP p
					double[][] loads = new double[topology.getDimension()][topology.getDimension()];

					Iterator<Segment> seg = lsp.getIterator();
					while (seg.hasNext()) {
						Segment segment = seg.next();
						if (segment.getType().equals(SegmentType.ADJ)) {
							loads[segment.getSrcNodeId()][segment.getDstNodeId()] += dem;

						} else if (segment.getType().equals(SegmentType.NODE)) {
							int dst = segment.getDstNodeId();
							Graph g = topology.getShortestPathGraph().getArcsShortestPath(dst);
							ECMPLoadBalancer lb1 = new ECMPLoadBalancer(g);
							ArrayList<Integer> nd = new ArrayList<Integer>(
									topology.getShortestPathGraph().getNodesForDest(dst));

							nd.sort(comparator);
							// finds the segment src node in the spanning tree
							while (nd.get(0) != segment.getSrcNodeId())
								nd.remove(0);
							while (nd.size() > 0) {
								int v = nd.get(0);
								double sum = 0.0;
								if (v == segment.getSrcNodeId())
									sum += dem;
								else {
									for (int u1 = 0; u1 < topology.getDimension(); u1++)
										if (topology.getNetGraph().existEdge(u1, v)
												&& topology.getNetGraph().getEdge(u1, v).isUP())
											sum += loads[u1][v];
								}
								// for each arc leaving from this node
								for (int w = 0; w < topology.getDimension(); w++) {
									if (topology.getNetGraph().existEdge(v, w)
											&& topology.getNetGraph().getEdge(v, w).isUP()) {
										loads[v][w] = lb1.getSplitRatio(v, dest, v, w) * sum;
									}
								}
								nd.remove(0);
							}
						}
					}

					for (int i = 0; i < topology.getDimension(); i++)
						for (int j = 0; j < topology.getDimension(); j++)
							ploads[i][j] += loads[i][j];

				} // threshold
			}

			// After all traffic has been forwarded by NSP
			// the remaining demands will be forwarded by SP/ECMP

			double spSplit = 1.0 - ssplit;

			// only shortest path
			NetNode dstNode = this.topology.getNetGraph().getNodeAt(dest);
			NetNode srcNode = this.topology.getNetGraph().getNodeAt(src);
			LabelPath lsp = new LabelPath(srcNode, dstNode);
			lsp.setFraction(spSplit);
			Segment segment = new Segment(dstNode.toString(), SegmentType.NODE);
			segment.setDstNodeId(dstNode.getNodeId());
			segment.setSrcNodeId(srcNode.getNodeId());
			lsp.addSegment(segment);

			if (this.configureSRPath) {
				SRconfig.addLabelPath(srcNode, dstNode, lsp);
			}

			double[][] loads = new double[topology.getDimension()][topology.getDimension()];

			double dem = demands.getDemands(src, dest) * spSplit;

			int dst = segment.getDstNodeId();
			Graph g = topology.getShortestPathGraph().getArcsShortestPath(dst);
			ECMPLoadBalancer lb1 = new ECMPLoadBalancer(g);
			ArrayList<Integer> nd = new ArrayList<Integer>(topology.getShortestPathGraph().getNodesForDest(dst));

			nd.sort(comparator);
			// finds the segment src node in the spanning tree
			while (nd.get(0) != segment.getSrcNodeId())
				nd.remove(0);
			while (nd.size() > 0) {
				int v = nd.get(0);
				double sum = 0.0;
				if (v == segment.getSrcNodeId())
					sum += dem;
				else {
					for (int u1 = 0; u1 < topology.getDimension(); u1++)
						if (topology.getNetGraph().existEdge(u1, v) && topology.getNetGraph().getEdge(u1, v).isUP())
							sum += loads[u1][v];
				}
				// for each arc leaving from this node
				for (int w = 0; w < topology.getDimension(); w++) {
					if (topology.getNetGraph().existEdge(v, w) && topology.getNetGraph().getEdge(v, w).isUP()) {
						loads[v][w] = lb1.getSplitRatio(v, dest, v, w) * sum;
					}
				}
				nd.remove(0);
			}

			for (int i = 0; i < topology.getDimension(); i++)
				for (int j = 0; j < topology.getDimension(); j++)
					ploads[i][j] += loads[i][j];

		} // for each src node

		return ploads;
	}

	public void partialSRConfig(int dest, SRConfiguration config) {

		// node on SP sorted by distance to destination
		ArrayList<Integer> nodes = new ArrayList<Integer>(topology.getShortestPathGraph().getNodesForDest(dest));
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
		GammaLoadBalancer gam = new GammaLoadBalancer(this.topology, dest, this.getLoadBalancer());
		gam.computeGamma();
		Iterator<Integer> it = nodes.iterator();

		while (it.hasNext()) {

			int src = it.next();
			List<Integer> nodesOnPaths = topology.getShortestPathGraph().getNodesOnShortestPaths(src, dest, false);
			ArrayList<IntPair> nspEdges = new ArrayList<IntPair>();
			for (int node : nodesOnPaths) {
				List<IntPair> nse = gam.getNSPEdgesEndWithSStart(node, true);
				nspEdges.addAll(nse);
			}

			Iterator<IntPair> nspEdgesIterator = nspEdges.iterator();
			while (nspEdgesIterator.hasNext()) {

				IntPair p = nspEdgesIterator.next();
				NetNode dstNode = this.topology.getNetGraph().getNodeAt(dest);
				NetNode srcNode = this.topology.getNetGraph().getNodeAt(src);
				LabelPath lsp = new LabelPath(srcNode, dstNode);
				lsp.setFraction(0.0);
				if (p.getX() != src) {
					Segment segment = new Segment(p.getXString(), SegmentType.NODE);
					segment.setSrcNodeId(srcNode.getNodeId());
					segment.setDstNodeId(p.getX());
					lsp.addSegment(segment);
				}
				Segment s = new Segment(p.toString(), SegmentType.ADJ);
				s.setDstNodeId(p.getY());
				s.setSrcNodeId(p.getX());
				lsp.addSegment(s);
				if (p.getY() != dest) {
					Segment segment = new Segment(dstNode.toString(), SegmentType.NODE);
					segment.setSrcNodeId(p.getY());
					segment.setDstNodeId(dstNode.getNodeId());
					lsp.addSegment(segment);
				}
				
				config.addLabelPath(srcNode, dstNode, lsp);

			}
			NetNode dstNode = this.topology.getNetGraph().getNodeAt(dest);
			NetNode srcNode = this.topology.getNetGraph().getNodeAt(src);
			LabelPath lsp = new LabelPath(srcNode, dstNode);
			lsp.setFraction(1.0);
			Segment segment = new Segment(dstNode.toString(), SegmentType.NODE);
			segment.setDstNodeId(dstNode.getNodeId());
			segment.setSrcNodeId(srcNode.getNodeId());
			lsp.addSegment(segment);
			//System.out.println("adding SP:"+lsp.toString());
			config.addLabelPath(srcNode, dstNode, lsp);
		}
	}

	public double evalPValues(double[] pValues, int[] weights, Demands demands) {
		// NetworkLoads loads;
		topology.applyWeights(weights);
		// topology.shortestDistances();
		double res = 0.0;
		double[][] tl = totalLoads(pValues, demands);
		loads = new NetworkLoads(tl, topology);
		res = congestionMeasure(loads, demands);
		loads.setCongestion(res);
		return res;
	}

	public double evalWeights(int[] weights, Demands demands) {
		topology.applyWeights(weights);
		// topology.shortestDistances();
		double res = 0.0;
		loads = new NetworkLoads(totalLoads(demands), topology);
		res = congestionMeasure(loads, demands);
		loads.setCongestion(res);
		return res;
	}

	public double computeLoads(OSPFWeights weights, PValues p, Demands demands) throws DimensionErrorException {
		return computeLoads(weights.asIntArray(), p.getPValues(), demands);
	}

	public double computeLoads(int[] weights, double[] p, Demands demands) throws DimensionErrorException {
		return evalPValues(p, weights, demands);
	}

	public SRConfiguration getSRconfiguration() {
		return SRconfig;
	}

	public void setConfigureSRPath(boolean b) {
		configureSRPath = b;
	}

}
