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
package pt.uminho.algoritmi.netopt.ospf.simulation.loadballancer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import pt.uminho.algoritmi.netopt.SystemConf;
import pt.uminho.algoritmi.netopt.ospf.graph.MatDijkstra;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.Simul.LoadBalancer;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetNode;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetNode.NodeType;
import pt.uminho.algoritmi.netopt.ospf.utils.IntPair;

/**
 * 
 *
 *  For each destination t computes the splitting traffic matrix for each path node. 
 *  The h matrix contains entries h^t_u,v of SP extra lengths to destination t from u
 *  passing by v.
 *
 */

public class GammaLoadBalancer implements ILoadBalancer {

	
	private int destination;
	private double[][] hs; 			// e^h
	private double[] gs; 			// gamma node
	private double[][] gamma; 		// e^h * gamma
	private double[][] splits; 		// splitting ratios
	private ArrayList<IntPair> nsp;	// Non Shortest Path links to destination t
	private NetworkTopology topology;
	private double[] pvalues;

	private double DEFAULT_P_VALUE;
	private boolean APPLY_THRESHOLD;
	private double THRESHOLD;
	
	// penalizing function
	private IHFunction function;
	private LoadBalancer lb;
	//private boolean deft = false;	
	private boolean debug = false;

	public GammaLoadBalancer(NetworkTopology topology, int destination) {
		this(topology, destination, LoadBalancer.DEFT, null);
	}

	public GammaLoadBalancer(NetworkTopology topology, int destination, LoadBalancer lb) {
		this(topology, destination, lb, null);
	}

	public GammaLoadBalancer(NetworkTopology topology, int destination, LoadBalancer lb, double[] pv) {

		this.topology = topology;
		this.destination = destination;
		this.lb = lb;
		int size = topology.getDimension();

		// default configuration values
		this.DEFAULT_P_VALUE = SystemConf.getPropertyDouble("pvalue.default", 1.0);
		this.APPLY_THRESHOLD = SystemConf.getPropertyBoolean("deft.applythreshold", false);
		this.THRESHOLD = SystemConf.getPropertyDouble("deft.threshold", 0.01);
		
		// penalizing function
		function = new Exponential();
			
		
		// set node-p values to default if none are provided 
		if (pv == null || pv.length != size) {
			pvalues = new double[size];
			for (int i = 0; i < size; i++)
				pvalues[i] = DEFAULT_P_VALUE;
		} else {
			pvalues = pv;
		}

		// initialize computation matrices
		this.hs = new double[size][size];
		this.gamma = new double[size][size];
		this.splits = new double[size][size];
		//usually don't need to be initialized, however....
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				hs[i][j] = 0.0;
				gamma[i][j] = 0.0;
				splits[i][j] = 0.0;
			}
		}

		this.nsp = new ArrayList<IntPair>();
	}

	/*
	 * Computes the penalization of the extra length (h) of a path from all nodes
	 * to destination t. On legacy nodes only outgoing links on shortest paths
	 * are used to forward traffic. 
	 */
	
	private double[][] computeEH() {

		MatDijkstra sp = topology.getShortestPathGraph();
		for (int u = 0; u < topology.getDimension(); u++) {
			NetNode node = topology.getNetGraph().getNodeAt(u);
			for (int v = 0; v < topology.getDimension(); v++) {
				if (u != destination && u != v && topology.getNetGraph().existEdge(u, v)
						&& topology.getNetGraph().getEdge(u, v).isUP()) {
					
					double dut = sp.getDist(u, this.destination);
					double dvt = sp.getDist(v, this.destination);
					if (dvt < dut) {
						double h = dvt + sp.getArcWeight(u, v) - dut;
						double exp = function.f(h,pvalues[u]);
						// hs_uv could be set to 1 when next hops v are on the shortest path
						// however this allows to use distinct settings as defined by the penalizing function 
						if (h == 0) {
							hs[u][v] = exp;
						}
						// non shortest path edges
						// if node u is SDN/SR enable
						// nsp links can be used to forward traffic
						else if (node.getNodeType().equals(NodeType.SDN_SR)) {
							hs[u][v] = exp;
							nsp.add(new IntPair(u, v));
						}

					}
				}
			}
		}

		if (debug) {
			DecimalFormat df = new DecimalFormat("#.00");
			for (int i = 0; i < hs.length; i++) {
				for (int j = 0; j < hs.length; j++) {
					System.out.print(df.format(hs[i][j]) + " ");
				}
				System.out.println();
			}
			System.out.println("NSP Edges:");
			for (int i = 0; i < nsp.size(); i++)
				System.out.print(nsp.get(i).toString() + " ; ");
			System.out.println();
		}

		return hs;
	}

	/**
	 * 
	 * @param sp
	 * @return the array of gamma values
	 */
	public double[][] computeGamma() {

		computeEH();
		
		if (lb.equals(LoadBalancer.PEFT)) { // for PEFT
			// solves the system taking hs as coefficients
			RealMatrix coefficients = new Array2DRowRealMatrix(this.hs, true);
			coefficients.setEntry(destination, destination, 1);
			for (int i = 0; i < hs.length; i++)
				if (i != destination)
					coefficients.setEntry(i, i, -1);

			
						
			DecompositionSolver solver = new LUDecomposition(coefficients).getSolver();
			double[] c = new double[hs.length];
			for (int i = 0; i < c.length; i++)
				c[i] = 0;
			c[destination] = 1;
			RealVector constants = new ArrayRealVector(c, false);
			RealVector solution = solver.solve(constants);
			gs = solution.toArray();
			for (int i = 0; i < gs.length; i++) {
				double sum = 0.0;
				double[] values = new double[gs.length];
				for (int j = 0; j < gs.length; j++) {
					gamma[i][j] = hs[i][j] * gs[j];
					values[j] = gamma[i][j];
					sum += gamma[i][j];
				}
				if (sum != 0.0)
					if (this.APPLY_THRESHOLD) {
						double res[] = fraction(values, this.THRESHOLD);
						for (int j = 0; j < gs.length; j++) {
							splits[i][j] = res[j];
						}
					} else {
						for (int j = 0; j < gs.length; j++) {
							splits[i][j] = gamma[i][j] / sum;
						}
					}

			}

		} else { // DEFT
			for (int i = 0; i < hs.length; i++) {
				double sum = 0.0;
				double[] values = new double[hs.length];
				for (int j = 0; j < hs.length; j++) {
					sum += hs[i][j];
					values[j] = hs[i][j];
				}
				if (sum != 0.0)
					if (this.APPLY_THRESHOLD) {
						double res[] = fraction(values, this.THRESHOLD);
						for (int j = 0; j < hs.length; j++) {
							splits[i][j] = res[j];
						}
					} else {
						for (int j = 0; j < hs.length; j++) {
							splits[i][j] = hs[i][j] / sum;
						}
					}
			}
		}

		if (debug) {
			System.out.println("**********Split table*******\n*************************");
			DecimalFormat df = new DecimalFormat("#.0000");
			for (int i = 0; i < hs.length; i++) {
				for (int j = 0; j < hs.length; j++) {
					System.out.print(df.format(splits[i][j]) + " ");
				}
				System.out.println();
			}
			System.out.println("\n");
		}

		return splits;
	}

	public boolean isOnShortestPath(int u, int v) {
		return hs[u][v] == 1;
	}

	public double getSplit(int from, int to) {
		return splits[from][to];
	}

	public int countNSPLinks() {
		return nsp.size();
	}

	/**
	 * 
	 * @param srcNode
	 * @param SRClosed: if adjacent nodes must be SR enabled
	 * @return 
	 */
	public List<IntPair> getNSPEdgesEndWithSStart(int srcNode,boolean SRClosed) {
		ArrayList<IntPair> l = new ArrayList<IntPair>();
		if(SRClosed && !topology.getNetGraph().getNodeByID(srcNode).getNodeType().equals(NodeType.SDN_SR)){
			return l;
		}
		Iterator<IntPair> it = nsp.iterator();
		while (it.hasNext()) {
			IntPair p = it.next();
			if (p.getX() == srcNode){
				if(SRClosed){
					if(topology.getNetGraph().getNodeByID(p.getY()).getNodeType().equals(NodeType.SDN_SR))
						l.add(p);
				}else
				 l.add(p);
			}
		}
		return l;
	}

	@Override
	public double getSplitRatio(int flowSrc, int flowDst, int currentNode, int nextNode) {
		// flowSrc is not needed
		// flowDst was used to compute the splits
		if (flowDst != this.destination)
			return 0;
		return getSplit(currentNode, nextNode);
	}

	
	
	/**
	 * 
	 * @param values
	 * @param threshold
	 * @return fractions of traffic above threshold (or 0.0 if link is not to be used)
	 */
	public double[] fraction(double[] values, double threshold) {
		double[] res = new double[values.length];
		double[] v = Arrays.copyOf(values, values.length);
		Arrays.sort(v);
		double sum = 0;
		for (double d : v)
			sum += d;
		boolean stop = false;
		int k=0;
		while (!stop && k<v.length) {
			if (v[k] / sum >= threshold)
				stop = true;
			else {
				sum -= v[k];
				k++;
			}
		}
		for (int i = 0; i < values.length; i++)
			if (values[i] >= v[k])
				res[i] = values[i] / sum;
			else
				res[i] = 0;
		return res;
	}

}
