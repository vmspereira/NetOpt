/**
* Copyright 2012-2017,
* Centro Algoritmi
* University of Minho
*
* This is free software: you can redistribute it and/or modify
* it under the terms of the GNU Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This code is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Public License for more details.
*
* You should have received a copy of the GNU Public License
* along with this code.  If not, see <http://www.gnu.org/licenses/>.
* 
* @author Vítor Pereira
*/
package pt.uminho.algoritmi.netopt.ospf.simulation;

import pt.uminho.algoritmi.netopt.ospf.simulation.simulators.ISimulator;
import pt.uminho.algoritmi.netopt.ospf.simulation.simulators.PDEFTSimul;
import pt.uminho.algoritmi.netopt.ospf.simulation.simulators.SRSimul;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import pt.uminho.algoritmi.netopt.ospf.simulation.Simul.LoadBalancer;
import pt.uminho.algoritmi.netopt.ospf.simulation.exception.DimensionErrorException;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetEdge;;

/**
 * 
 * @author Vítor Pereira
 *
 *         For a given topology (with legacy and SDN/SR nodes), and a weights
 *         configuration, EdgesLoad computes the fraction of aggregated
 *         source/destination traffic that travels in each edge. It also
 *         produces, for each edge, the partial traffic demand matrix traveling
 *         over it.
 * 
 */

public class EdgesLoad {

	public enum EdgeNodeIn {
		LEFT_RIGHT, BOTH;
	}

	private Map<Integer, List<PartialEdgeLoad>> edgesLoad;

	public EdgesLoad() {
		edgesLoad = new HashMap<Integer, List<PartialEdgeLoad>>();
	}

	public Map<Integer, List<PartialEdgeLoad>> getEdgesLoad() {
		return edgesLoad;
	}

	public void computeLoads(NetworkTopology topology, int[] weights, LoadBalancer lb, boolean sr)
			throws DimensionErrorException {

		OSPFWeights w = new OSPFWeights(topology.getDimension());
		w.setWeights(weights, topology);
		computeLoads(topology, w, lb, sr);
	}

	/**
	 * 
	 * @param topology
	 * @param weights
	 * @param loadBalancer
	 * 
	 *            Computes the fraction of aggregated source/destination traffic
	 *            that travels in each edge.
	 * @throws DimensionErrorException
	 */
	public void computeLoads(NetworkTopology topology, OSPFWeights weights, LoadBalancer lb, boolean sr)
			throws DimensionErrorException {

		int dim = topology.getDimension();
		topology.applyWeights(weights);
		topology.shortestDistances();
		ISimulator simul;
		if (!sr)
			switch (lb) {
			case DEFT:
				simul = new PDEFTSimul(topology, true);
				break;
			case PEFT:
				simul = new PDEFTSimul(topology, false);
				break;
			case ECMP:
			default:
				simul = new Simul(topology);
				break;
			}
		else
			simul = new SRSimul(topology, lb);

		double[][] demands = new double[dim][dim];
		for (int i = 0; i < dim; i++)
			for (int j = 0; j < dim; j++)
				demands[i][j] = 0.0;

		for (int src = 0; src < dim; src++) {
			for (int dst = 0; dst < dim; dst++) {
				if (src != dst) {
					// compute loads
					demands[src][dst] = 100.0;
					double[][] load = simul.partialLoads(dst, new Demands(demands));

					demands[src][dst] = 0.0;
					// map load to edges
					for (int i = 0; i < dim; i++) {
						for (int j = 0; j < dim; j++) {
							if (load[i][j] != 0.0) {
								int e = topology.getNetGraph().getEdge(i, j).getEdgeId();
								if (!edgesLoad.containsKey(e)) {
									edgesLoad.put(e, new ArrayList<PartialEdgeLoad>());
								}
								PartialEdgeLoad l = new PartialEdgeLoad(src, dst, load[i][j], i, j);
								edgesLoad.get(e).add(l);
							}
						}
					}
				}
			}
		}
	}

	public class PartialEdgeLoad {
		// source node
		int src;
		// destination node
		int dst;
		// percentage of traffic from src to dst
		double percentage;
		// in node at failing link
		int edgeIN;
		int edgeOUT;

		public PartialEdgeLoad(int src, int dst, double percentage, int in, int out) {
			this.src = src;
			this.dst = dst;
			this.percentage = percentage;
			this.edgeIN = in;
			this.edgeOUT = out;
		}

		public int getSource() {
			return src;
		}

		public int getDestination() {
			return dst;
		}

		public double getTrafficPercentage() {
			return this.percentage;
		}

		public String toString() {
			return "[src=" + src + " dst=" + dst + " traffic percentage=" + percentage + "%]";
		}

		public int getINNode() {
			return this.edgeIN;
		}

		public int getOUTNode() {
			return this.edgeOUT;
		}
	}

	/**
	 * 
	 * @param demands
	 * @param edge
	 *            that is used to forward traffic
	 * @param If
	 *            BOTH traffic from the two extremities of the link are gather
	 *            in a single TM. If LEFT_RIGHT traffic from both ends are
	 *            separated into two TM.
	 * @return Returns the partial demands that travel across an edge. Assumes
	 *         that the partial fractional load computations has been performed.
	 */
	public Demands[] getEdgeToEdgePartialDemand(Demands demand, NetEdge edge, EdgeNodeIn in) {
		Demands d0 = demand.copy();
		Demands d1 = new Demands(demand.getDimension());
		Demands d2 = new Demands(demand.getDimension());

		if (this.edgesLoad.containsKey(edge.getEdgeId())) {
			Iterator<PartialEdgeLoad> it = this.edgesLoad.get(edge.getEdgeId()).iterator();
			while (it.hasNext()) {
				PartialEdgeLoad next = it.next();
				int src = next.getSource();
				int dst = next.getDestination();
				double percentage = next.getTrafficPercentage() / 100;
				double d = percentage * demand.getDemands(src, dst);
				d0.subtract(src, dst, d);
				// traffic from src to dst
				if (in.equals(EdgeNodeIn.BOTH))
					d1.add(src, dst, d);
				else {
					if (next.getINNode() == edge.getTo())
						d1.add(src, dst, d);
					else
						d2.add(src, dst, d);
				}
			}
		}
		Demands[] result;
		if (in.equals(EdgeNodeIn.BOTH)) {
			result = new Demands[2];
			result[0] = d0;
			result[1] = d1;
		} else {
			result = new Demands[3];
			result[0] = d0;
			result[1] = d1;
			result[2] = d2;
		}
		return result;
	}

	/**
	 * 
	 * @param demand
	 * @param edge
	 * @return Returns the partial demands that travel across an edge. Assumes
	 *         that the partial fractional load computations has been performed.
	 */
	public Demands[] getPointofRecoveryPartialDemand(Demands demand, NetEdge edge, EdgeNodeIn in) {
		Demands d0 = demand.copy();
		Demands d1 = new Demands(demand.getDimension());
		Demands d2 = new Demands(demand.getDimension());

		if (this.edgesLoad.containsKey(edge.getEdgeId())) {
			Iterator<PartialEdgeLoad> it = this.edgesLoad.get(edge.getEdgeId()).iterator();
			while (it.hasNext()) {
				PartialEdgeLoad next = it.next();
				int src = next.getSource();
				int dst = next.getDestination();
				int innode = next.getINNode();
				double percentage = next.getTrafficPercentage() / 100;
				double d = percentage * demand.getDemands(src, dst);
				d0.subtract(src, dst, d);
				// traffic from src to PLR
				d0.add(src, innode, d);
				// traffic from PLR to dst
				if (in.equals(EdgeNodeIn.BOTH))
					d1.add(innode, dst, d);
				else if (next.getINNode() == edge.getTo())
					d1.setDemands(innode, dst, d);
				else
					d2.setDemands(innode, dst, d);
			}
		}
		Demands[] result;
		if (in.equals(EdgeNodeIn.BOTH)) {
			result = new Demands[2];
			result[0] = d0;
			result[1] = d1;
		} else {
			result = new Demands[3];
			result[0] = d0;
			result[1] = d1;
			result[2] = d2;
		}
		return result;
	}

	public String getString(NetEdge edge) {
		StringBuffer bf = new StringBuffer();
		if (this.edgesLoad.containsKey(edge.getEdgeId())) {
			Iterator<PartialEdgeLoad> it = this.edgesLoad.get(edge.getEdgeId()).iterator();
			while (it.hasNext()) {
				PartialEdgeLoad next = it.next();
				bf.append(next.toString());
			}
		}
		return bf.toString();
	}

	public EdgesLoad copy() {
		EdgesLoad el = new EdgesLoad();

		return el;
	}

}
