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
/**
 * Compares OSPF Shortest Paths based on two sets of weights w1 and w2
 * <p>The measure, for each pair of nodes (origin, destination) and respective 
 * Shortest Paths SPw1 and SPw2
 * calculates the ratio between the number of common links and MAX(number of links SPw1, number of links SPw2)  
 */


package pt.uminho.algoritmi.netopt.ospf.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import pt.uminho.algoritmi.netopt.ospf.graph.Graph;
import pt.uminho.algoritmi.netopt.ospf.graph.MatDijkstra;
import pt.uminho.algoritmi.netopt.ospf.graph.WGraph;
import pt.uminho.algoritmi.netopt.ospf.simulation.OSPFWeights;
import pt.uminho.algoritmi.netopt.ospf.simulation.exception.DimensionErrorException;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetEdge;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetGraph;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetPath;

import java.io.Serializable;


public class DefaultSPComparator  implements SPComparator, Serializable{

	
	private static final long serialVersionUID = 1L;
	
	NetGraph bg;
	OSPFWeights weights1;
	OSPFWeights weights2;

	//weights1 sp
	MatDijkstra sp1;
	//weights2 sp
	MatDijkstra sp2;

	// End to End Shortest Path change measure 
	Double[][] comparison;
	
	// Average value of comparison
	Double average;
	
	public DefaultSPComparator(NetGraph graph, OSPFWeights w1, OSPFWeights w2) {
		this.bg = graph;
		this.weights1 = w1;
		this.weights2 = w2;
		this.sp1 = null;
		this.sp2 = null;
		this.comparison = null;
		this.average = null;
		
		
		
	}

	
	public NetGraph getBriteGraph(){
		return this.bg;
	}
	
	/*
	 * Calculates shortest Paths
	 */
	public double compare() throws DimensionErrorException {

		WGraph g1 = bg.createWGraph();
		WGraph g2 = bg.createWGraph();
		applyWeights(g1, weights1);
		applyWeights(g2, weights2);
		sp1 = new MatDijkstra(g1);
		sp2 = new MatDijkstra(g2);
		sp1.execute();
		sp2.execute();
		double sum = 0;
		double count = 0;
		int n = g1.getDimension();
		comparison = new Double[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (i != j) {
				   
					List<Vector<Integer>> v1 = sp1.getAllPaths(i, j);
					List<Vector<Integer>> v2 = sp2.getAllPaths(i, j);
					NetPath p1=new NetPath(v1);
					NetPath p2=new NetPath(v2);
					ArrayList<NetEdge> l=p2.intersect(p1);
					int max;
					if(p1.getSize()>p2.getSize())
						max=p1.getSize();
					else
						max=p2.getSize();
					Double d;
					//if max=0 then there is no path from node i to node j else
					//the change measure is the number of common links divided
					//by the the greatest number of link on both paths
					if(max==0) 
						d=null;
					else  
						d=(double)l.size()/max;
					comparison[i][j] = d;
					if (d != null) {
						sum += d;
						count++;
					}
				}else{
					comparison[i][j] = null;
				}
			}
		}
		this.average = sum / count;
		return average;
	}

	
	private void applyWeights(WGraph graph, OSPFWeights weights)
			throws DimensionErrorException {

		if (weights.getDimension() != graph.getDimension()) {
			throw new DimensionErrorException();
		}
		for (int i = 0; i < graph.getDimension(); i++)
			for (int j = 0; j < graph.getDimension(); j++)
				if (!graph.getConnection(i, j).equals(Graph.Status.NOCONNECTION)) {
					graph.setWeight(i, j, weights.getWeight(i, j));
				}
	}

	public Double getAverage() {
		return this.average;
	}

	public Double getComparison(int i, int j) {
		return this.comparison[i][j];
	}

	public int getDimension() {
		return bg.getNNodes();
	}
	
	public MatDijkstra getSP1(){
		return this.sp1;
	}
	
	public MatDijkstra getSP2(){
		return this.sp2;
	}
	
	
	/**
	 * 
	 * @return penalty
	 * @throws DimensionErrorException
	 * Comparison is set between 0 and 1, being 0 the worst value
	 * Penalty of average or average of penalties?
	 */
	public double getPathChangePenalty() throws DimensionErrorException{
		if(average==null)
			compare();
		//TODO
		/**
		 * Penalties definition
		 */
		// for now just return a penalty value between 0 and 1  
		return 1-average;
	}

}
