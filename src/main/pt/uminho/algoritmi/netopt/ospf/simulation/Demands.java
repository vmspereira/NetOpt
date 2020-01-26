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
import java.util.StringTokenizer;

import pt.uminho.algoritmi.netopt.ospf.graph.SPFElement;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetGraph;
import pt.uminho.algoritmi.netopt.ospf.utils.MathUtils;



@SuppressWarnings("serial")
public class Demands implements Serializable {
	double[][] demands; // demands
	private String filename;

	public Demands(int dimension) {
		allocateDemands(dimension);
	}

	public Demands(double[][] demand) {
		int n = demand.length;
		this.demands = new double[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++)
				this.demands[i][j] = demand[i][j];
		}
	}
	
	public Demands(int dimension, String filename) throws Exception {
		this.filename = filename;
		readDemands(dimension, filename);
	}

	public double[][] getDemands() {
		return this.demands;
	}

	public int getDimension() {
		return this.demands.length;
	}

	public Double[][] getDemandsObj() {
		int dimension = this.demands.length;

		Double[][] aux = new Double[dimension][dimension];

		for (int s = 0; s < dimension; s++)
			for (int t = 0; t < dimension; t++) {
				Double d = MathUtils.truncate2(demands[s][t]);

				aux[s][t] = d;
			}
		return aux;
	}

	public double getDemands(int i, int j) {
		return this.demands[i][j];
	}

	protected void allocateDemands(int n) {
		this.demands = new double[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++)
				this.demands[i][j] = 0.0;
		}
	}

	/**
	 * Reads demands from a file Network topology already read.Know the
	 * dimension
	 */
	public void readDemands(int n, String filename) throws Exception {
		FileReader f = new FileReader(filename);
		BufferedReader B = new BufferedReader(f);

		allocateDemands(n);

		for (int i = 0; i < n; i++) {
			String str = B.readLine();
			StringTokenizer st = new StringTokenizer(str);
			if (st.countTokens() != n){
				B.close();
				f.close();
				throw new Exception("Error in file format demands");
			}
			for (int j = 0; j < n; j++) {
				double dem = Double.valueOf(st.nextToken()).doubleValue();
				demands[i][j] = dem;
			}
		}
		B.close();
		f.close();
	}

	// Generates demands from the graph (Fortz and Thorup 2000)
	// D - parameter to control congestion of the network
	public void generateDemands(SPFElement[][] sol, double D, NetGraph bgr) {
		int n = this.demands.length;

		for (int s = 0; s < n; s++)
			for (int t = 0; t < n; t++)
				if (s != t && sol[s][t] != null) // predecessors
				{
					// DijkstraNode dn = (DijkstraNode)sol.get(k);
					// int s = dn.getSource();
					// int t = dn.getNode();
					double sx = bgr.getNodeAt(s).getXpos();
					double sy = bgr.getNodeAt(s).getYpos();
					double tx = bgr.getNodeAt(t).getXpos();
					double ty = bgr.getNodeAt(t).getYpos();
					double dist = Math.sqrt((sx - tx) * (sx - tx) + (sy - ty)
							* (sy - ty));
					if (dist > 0)
						demands[s][t] = Math.random() * (D / dist);
				}
	}

	/** Sets demands randomly using a parameter sD to scale the values */
	public void setRandomDemands(double sD, NetworkTopology topology) {
		double D = Dvalue(sD, topology);
		SPFElement[][] sol = topology.getEuclidianDistanceSPGraph().getSolPreds();
		generateDemands(sol, D, topology.getNetGraph());
	}

	// method to calculate D value - used for demand generation
	// given a aimed average congestion (0-1)
	public double Dvalue(double aimedCong, NetworkTopology topology) {
		int numlinks = topology.getGraph().countEdges();
		double avcap = topology.getGraph().averageCapacity();

		topology.setUnitWeights();
		topology.shortestDistances();

		// Vector sol = sp.getSolution();
		double[][] dists = topology.getShortestPathGraph().getShortestPathDistances();
		SPFElement[][] preds = topology.getShortestPathGraph().getSolPreds();

		double sum = 0.0;
		// for(int k= 0; k < sol.size(); k++)
		for (int s = 0; s < preds.length; s++)
			for (int t = 0; t < preds.length; t++)
				if (s != t && preds[s][t] != null) // predecessors
				{
					// DijkstraNode dn = (DijkstraNode)sol.get(k);
					// double hops = dn.getDist();
					// int s = dn.getSource();
					// int t = dn.getNode();
					double hops = dists[s][t];

					double sx = topology.getNetGraph().getNodeAt(s).getXpos();
					double sy = topology.getNetGraph().getNodeAt(s).getYpos();
					double tx = topology.getNetGraph().getNodeAt(t).getXpos();
					double ty = topology.getNetGraph().getNodeAt(t).getYpos();
					double dist = Math.sqrt((sx - tx) * (sx - tx) + (sy - ty)
							* (sy - ty));
					if (dist > 0)
						sum += (hops / dist);
				}
		return (2.0 * aimedCong * avcap * numlinks / sum);
	}

	public void printDemands() {
		int n = this.demands.length;

		System.out.println("Demands:");
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++)
				System.out.print(demands[i][j] + " ");
			System.out.println("");
		}
	}

	public void saveDemands(String file) throws Exception {
		FileWriter f = new FileWriter(file);
		BufferedWriter W = new BufferedWriter(f);

		int n = this.demands.length;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++)
				W.write(demands[i][j] + " ");
			W.write("\n");
		}
		W.flush();
		W.close();
		f.close();
	}

	public double sumDemands() {
		int n = this.demands.length;
		double sum = 0.0;

		for (int s = 0; s < n; s++)
			for (int t = 0; t < n; t++)
				if (demands[s][t] > 0)
					sum += demands[s][t];
		return sum;
	}
	
	
	public double totalDemandsToDestination(int destination){
		
		int n = this.demands.length;
		double sum = 0.0;
		for (int s = 0; s < n; s++)
				sum += demands[s][destination];
		return sum;
		
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	@Override
	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append("Demands:\n");
		for (int s = 0; s < demands.length; s++) {
			for (int t = 0; t < demands.length; t++) {
				str.append(demands[s][t]);
			}
			str.append("\n");
		}
		return str.toString();
	}

	public void setDemands(int i, int j, double value) {
		this.demands[i][j] = value;
	}

	public void add(Demands demand) {
		for (int i = 0; i < demands.length; i++) {
			for (int j = 0; j < demands.length; j++) {
				demands[i][j] += demand.getDemands(i, j);
			}
		}
	}
	
	public void add(int i, int j, double value) {
			demands[i][j] += value;
	}
	
	public void subtract(int i, int j, double value) {
		demands[i][j] -= value;
	}
	
	public void subtract(Demands demand) {
		for (int i = 0; i < demands.length; i++) {
			for (int j = 0; j < demands.length; j++) {
				demands[i][j] -= demand.getDemands(i, j);
			}
		}
	}
	
	
	
	public void mutiply(double factor) {
		for (int i = 0; i < demands.length; i++) {
			for (int j = 0; j < demands.length; j++) {
				demands[i][j] *= factor;
			}
		}
	}

	public double getTotal() {
		double total = 0.0;
		for (int i = 0; i < demands.length; i++) {
			for (int j = 0; j < demands.length; j++) {
				total+=demands[i][j];
			}
		}
		return total;
	}

	public Demands copy() {
		int n= this.demands.length;
		double[][] copy = new double[n][n];
		for(int i=0;i<n;i++)
			for(int j=0; j<n;j++)
				copy[i][j]=demands[i][j];
		return new Demands(copy);
	}
	
	
	
	public boolean equals(Demands d){
		if(d.demands.length!=this.demands.length)
			return false;
		for(int i=0;i<demands.length;i++)
			for(int j=0;j<demands.length;j++)
				if(this.demands[i][j]!=d.demands[i][j]){
					System.out.println(this.demands[i][j]-d.demands[i][j]);
					return false;
				}
		return true;
	}

	
	
	public void divide(int divider) {
		for(int i=0;i<demands.length;i++)
			for(int j=0;j<demands.length;j++)
				this.demands[i][j]/=divider;
				
	}

}
