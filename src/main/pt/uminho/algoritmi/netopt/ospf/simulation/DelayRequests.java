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
import pt.uminho.algoritmi.netopt.ospf.utils.MathUtils;



@SuppressWarnings("serial")
public class DelayRequests implements Serializable{
	double[][] delayReqs; // delay requirements (end to end nodes)
	private String filename;

	public DelayRequests(int n) {
		allocateDelayRequests(n);
	}

	public DelayRequests(int dimension, String filename) throws Exception {
		this.filename = filename;
		readDelayReqs(dimension, filename);
	}

	public DelayRequests(double[][] dr) {
		int dimension = dr.length;

		this.delayReqs = new double[dimension][dimension];
		for (int i = 0; i < dimension; i++) {
			for (int j = 0; j < dimension; j++) {
				this.delayReqs[i][j] = dr[i][j];
			}
		}
	}

	public int getDimension() {
		return this.delayReqs.length;
	}

	protected void allocateDelayRequests(int n) {
		this.delayReqs = new double[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++)
				this.delayReqs[i][j] = 0.0;
		}
	}

	public void setDelayRequest(int i, int j, double value) {
		this.delayReqs[i][j] = value;
	}

	public double getDelayReq(int i, int j) {
		return this.delayReqs[i][j];
	}

	public double[][] getDelayRequests() {
		return this.delayReqs;
	}

	public Double[][] getDelayRequestsObj() {
		int dimension = this.delayReqs.length;

		Double[][] aux = new Double[dimension][dimension];

		for (int s = 0; s < dimension; s++)
			for (int t = 0; t < dimension; t++) {
				Double d = MathUtils.truncate2(delayReqs[s][t]);

				aux[s][t] = d;
			}
		return aux;
	}

	public void printDelayReqs() {
		int n = this.delayReqs.length;

		System.out.println("Delay requirements:");
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++)
				System.out.print(delayReqs[i][j] + " ");
			System.out.println("");
		}
	}

	public void saveDelayReqs(String file) throws Exception {
		FileWriter f = new FileWriter(file);
		BufferedWriter W = new BufferedWriter(f);

		int n = this.delayReqs.length;

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++)
				W.write(delayReqs[i][j] + " ");
			W.write("\n");
		}
		W.flush();
		W.close();
		f.close();
	}

	// read delay requirements from a file (double x double matrix)
	public void readDelayReqs(int n, String filename) throws Exception {
		FileReader f = new FileReader(filename);
		BufferedReader b = new BufferedReader(f);

		delayReqs = new double[n][n];
		for (int s = 0; s < n; s++)
			for (int t = 0; t < n; t++)
				delayReqs[s][t] = 0.0;

		for (int i = 0; i < n; i++) {
			String str = b.readLine();
			StringTokenizer st = new StringTokenizer(str);
			if (st.countTokens() != n){
				b.close();
				f.close();
				throw new Exception("Error in file format delays");
			}
			for (int j = 0; j < n; j++) {
				double dr = Double.valueOf(st.nextToken()).doubleValue();
				delayReqs[i][j] = dr;
			}
		}
		
		b.close();
		f.close();
	}

	public void generateDelayReqs(SPFElement[][] sol, double dravg) {
		int dimension = delayReqs.length;

		for (int s = 0; s < dimension; s++)
			for (int t = 0; t < dimension; t++)
				if (s != t && sol[s][t] !=null) // predecessors
				{
					this.setDelayRequest(s, t, dravg * 0.75 + Math.random()
							* (dravg * 0.5));
				}
	}

	// calcular sumDelays? antigo classe OSPF
	// needs attention ---> sumDelays?! confirmar no OSPF antigo

	// Miguel: sumDelays nao e aqui

	/**
	 * Sets delay requirements randomly using a parameter sD to scale the values
	 */
	public void setRandomDelayReqs(double sDravg, NetworkTopology topology) {
		int dimension = delayReqs.length;

		SPFElement[][] sol = topology.getEuclidianDistanceSPGraph().getSolPreds();
		double sumEndtoEndDelays = topology.sumEndtoEndDelays();
		boolean fullDebug = false;

		int num_dr = 0;
		for (int s = 0; s < dimension; s++)
			for (int t = 0; t < dimension; t++)
				if (s != t && sol[s][t] !=null) // predecessors
					num_dr++;

		double dravg = sDravg * sumEndtoEndDelays / num_dr;

		// changes instance variable
		generateDelayReqs(sol, dravg);

		if (fullDebug) {
			System.out.println("Delay reqs");
			for (int i = 0; i < dimension; i++) {
				for (int j = 0; j < dimension; j++)
					System.out.print(this.getDelayReq(i, j) + " ");
				System.out.println("");
			}
		}
	}

	public double sumDelayReqs() {
		int n = delayReqs.length;

		double sum = 0.0;

		for (int s = 0; s < n; s++)
			for (int t = 0; t < n; t++)
				if (delayReqs[s][t] > 0)
					sum += delayReqs[s][t];
		return sum;
	}
	
	

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append("DelayRequests:\n");
		for (int s = 0; s < delayReqs.length; s++) {
			for (int t = 0; t < delayReqs.length; t++) {
				str.append(delayReqs[s][t]);
			}
			str.append("\n");
		}
		return str.toString();
	}

	public DelayRequests copy() {
		double[][] d = new double[delayReqs.length][delayReqs.length];
		for(int i =0; i<delayReqs.length;i++)
			for(int j =0; j<delayReqs.length;j++)
				d[i][j]=delayReqs[i][j];
		return new DelayRequests(d);
	}
}
