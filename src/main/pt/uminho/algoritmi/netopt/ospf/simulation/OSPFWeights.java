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
package pt.uminho.algoritmi.netopt.ospf.simulation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import pt.uminho.algoritmi.netopt.ospf.graph.Graph;
import pt.uminho.algoritmi.netopt.ospf.simulation.exception.DimensionErrorException;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetGraph;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.IntegerSolution;
import pt.uminho.algoritmi.netopt.ospf.utils.MathUtils;


@SuppressWarnings("serial")
public class OSPFWeights implements Serializable {

	private String creationMethod;
	private double[][] weights;

	// list of weights always based on a NetworkTopology!
	public OSPFWeights(int dimension) {
		this.weights = new double[dimension][dimension]; // oriented
	}

	
	
	
	public OSPFWeights(String filename, int dimension) throws Exception {
		readOSPFWeights(dimension, filename);
	}

	public OSPFWeights(String filename, NetworkTopology topo) throws Exception {
		readOSPFWeightsList(topo, filename);
	}
	
	public OSPFWeights(double[][] weights) {
		this.weights=weights;
	}



	public int getDimension() {
		return this.weights.length;
	}

	public double getWeight(int n1, int n2) {
		return weights[n1][n2];
	}

	public double[][] getWeights() {
		return this.weights;
	}

	public Double[][] getWeightsObj() {
		int dimension = this.weights.length;

		Double[][] aux = new Double[dimension][dimension];

		for (int s = 0; s < dimension; s++)
			for (int t = 0; t < dimension; t++) {
				Double d = MathUtils.truncate2(weights[s][t]);

				aux[s][t] = d;
			}
		return aux;
	}

	public void setWeight(int n1, int n2, double w) {
		this.weights[n1][n2] = w;
	}

	public void setWeights(int[] weights, NetworkTopology topology)
			throws DimensionErrorException {

		if (weights.length != topology.getNumberEdges())
			throw new DimensionErrorException(""+weights.length);

		int dimension = topology.getDimension();

		int w = 0;
		for (int i = 0; i < dimension; i++)
			for (int j = 0; j < dimension; j++) {
				if (!topology.getGraph().getConnection(i, j).equals(Graph.Status.NOCONNECTION)) {
					this.weights[i][j] = weights[w];
					w++;
				}
			}
	}
	
	
	public int[] asIntArray(){
		
		ArrayList<Integer> array=new ArrayList<Integer>();
		int dimension = weights.length;
		for (int i = 0; i < dimension; i++)
			for (int j = 0; j < dimension; j++) {
				if (this.weights[i][j]>0) {
					array.add((int) (weights[i][j] ));
				}
			}
		
		int[] ret = new int[array.size()];
	    Iterator<Integer> iterator = array.iterator();
	    for (int i = 0; i < ret.length; i++)
	    {
	        ret[i] = iterator.next().intValue();
	    }
	    return ret;
	}
	

	protected void allocateWeights(int n) {
		this.weights = new double[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++)
				this.weights[i][j] = 0.0;
		}
	}

	public String getCreationMethod() {
		return creationMethod;
	}

	public void setCreationMethod(String creationMethod) {
		this.creationMethod = creationMethod;
	}

	public void setUnitWeights(NetworkTopology topology) {
		int dimension = weights.length;
		this.creationMethod = "Unit weights";

		for (int i = 0; i < dimension; i++)
			for (int j = 0; j < dimension; j++)
				if (topology.getGraph().getConnection(i, j).equals(Graph.Status.UP)) {
					weights[i][j] = 1;
				}
	}

	// assumes BriteGraph exists
	// sets weights based on the link length
	public void setL2Weights(int minW , int maxW, NetworkTopology topology) {
		NetGraph bgr = topology.getNetGraph();
		this.creationMethod = "L2 Weights";
		
		double maxlen = bgr.maxLinkLength();
		double minlen = bgr.minLinkLength();
		
		int rangeW=maxW-minW;
		
		for (int e = 0; e < bgr.getNEdges(); e++) {
			double w = (bgr.getEdge(e).getLength() - minlen) / (maxlen - minlen);
			int iw = (int) (w * rangeW) + minW;
			this.setWeight(bgr.getEdge(e).getFrom(), bgr.getEdge(e).getTo(), iw);
			this.setWeight(bgr.getEdge(e).getTo(), bgr.getEdge(e).getFrom(), iw);
		}
	}

	// sets weights based on the inverse of capacity
	public void setInvCapWeights(int minW, int maxW, NetworkTopology topology) {
		this.creationMethod = "InvCap weights";

		NetGraph bgr = topology.getNetGraph();
		int rangeW = maxW-minW; 
		double minb = 1.0 / bgr.maxLinkBandwidth();
		double maxb = 1.0 / bgr.minLinkBandwidth();
		double range = maxb - minb;

		for (int e = 0; e < bgr.getNEdges(); e++) {
			double w = ((1.0 / bgr.getEdge(e).getBandwidth()) - minb) / range;
			int iw = (int) (w * rangeW) + minW;
			this.setWeight(bgr.getEdge(e).getFrom(), bgr.getEdge(e).getTo(), iw);
			this.setWeight(bgr.getEdge(e).getTo(), bgr.getEdge(e).getFrom(), iw);
		}
	}

	
	public void setRandomWeights(int minW , int maxW, NetworkTopology topology) {
		int dimension = this.weights.length;
		this.creationMethod = "Random weights";
		int rangeW=maxW-minW;
		for (int i = 0; i < dimension; i++)
			for (int j = 0; j < dimension; j++)
				if (topology.getGraph().getConnection(i, j).equals(Graph.Status.UP)) {
					this.setWeight(i, j, MathUtils.irandom(rangeW) +minW );
				}
	}

	public void saveOSPFWeights(String file) throws Exception {
		FileWriter f = new FileWriter(file);
		BufferedWriter W = new BufferedWriter(f);
		int dimension = this.weights.length;

		for (int i = 0; i < dimension; i++) {
			for (int j = 0; j < dimension; j++)
				W.write(weights[i][j] + " ");
			W.write("\n");
		}
		W.flush();
		W.close();
		f.close();
	}

	public void readOSPFWeights(int n, String filename) throws Exception {
		allocateWeights(n);

		FileReader f = new FileReader(filename);
		BufferedReader B = new BufferedReader(f);

		for (int i = 0; i < n; i++) {
			String str = B.readLine();
			StringTokenizer st = new StringTokenizer(str);
			if (st.countTokens() != n){
				B.close();
				f.close();
				throw new Exception("Error in file format");
			}
			for (int j = 0; j < n; j++) {
				double wei = Double.valueOf(st.nextToken()).doubleValue();
				weights[i][j] = wei;
			}
		}
		B.close();
		f.close();
	}

	
	public void readOSPFWeightsList(NetworkTopology topology, String filename) throws Exception {
		
		FileReader f = new FileReader(filename);
		BufferedReader B = new BufferedReader(f);
		String str = B.readLine();
		StringTokenizer st = new StringTokenizer(str);
		ArrayList<Integer> list = new ArrayList<Integer>();
		while(st.hasMoreTokens()){
				int value = Double.valueOf(st.nextToken()).intValue();
				list.add(value);
		}
		B.close();
		f.close();
		if (list.size() != topology.getNumberEdges())
			throw new DimensionErrorException(""+list.size());

		int dimension = topology.getDimension();
		allocateWeights(dimension);
		int w = 0;
		for (int i = 0; i < dimension; i++)
			for (int j = 0; j < dimension; j++) {
				if (!topology.getGraph().getConnection(i, j).equals(Graph.Status.NOCONNECTION)) {
					this.weights[i][j] = list.get(w);
					w++;
				}
			}
	}

	
	public void printWeights() {
		for (int i = 0; i < this.weights.length; i++) {
			for (int j = 0; j < this.weights.length; j++) {
				System.out.print(this.weights[i][j] + " ");
			}
			System.out.print("\n");
		}
	}

	/**
	 * 
	 * @param topology
	 * @return ISolution 
	 * @throws DimensionErrorException
	 * 
	 * Vitor: Produce a ISolution that can be used on a new population 
	 */
    
	public IntegerSolution toSolution(NetworkTopology topology,int numberObjectives) throws DimensionErrorException {
		ArrayList<Integer> genome = new ArrayList<Integer>();
		for (int i = 0; i < getDimension(); i++)
			for (int j = 0; j < getDimension(); j++) {
				if (!topology.getGraph().getConnection(i, j).equals(Graph.Status.NOCONNECTION)) {
					genome.add((int) this.weights[i][j]);
				}
			}
		
		if(genome.size()!=topology.getNumberEdges())
			throw new DimensionErrorException("Error @ toSolution: "+genome.size()+"!="+topology.getNumberEdges());
		
		IntegerSolution s = new IntegerSolution(genome,numberObjectives);
		return s;
	}
	
	
	
	public OSPFWeights copy(){
		double[][] d = new double[this.weights.length][this.weights.length];
		for(int i=0; i<this.weights.length;i++)
			for(int j=0; j<this.weights.length;j++)
				d[i][j]=this.weights[i][j];
		
		return new OSPFWeights(d);
	}
}
