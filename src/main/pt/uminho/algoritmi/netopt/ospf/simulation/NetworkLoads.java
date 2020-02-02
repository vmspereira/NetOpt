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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Serializable;
import java.text.DecimalFormat;

import pt.uminho.algoritmi.netopt.ospf.graph.CapWGraph;



@SuppressWarnings("serial")
public class NetworkLoads extends ResultSimul implements Serializable
{	
	protected double [][] loads; 
	protected double [][] u;  
	private String name="Network Loads";
	private NetworkTopology topology;
	private double congestion; 
	
	
	public NetworkLoads(NetworkTopology topo)
	{
		this.topology=topo.copy();
		this.loads = new double[topology.getDimension()][topology.getDimension()];
		this.u = new double[topology.getDimension()][topology.getDimension()];
		computeU(topology.getGraph());
		
	}
	
	public NetworkLoads(double[][] loads,NetworkTopology topo)
	{
		this.topology=topo.copy();
		this.loads = loads;
		this.u = new double[loads.length][loads.length];
		computeU(topology.getGraph());
		
	}
	
	public double[][] getLoads ()
	{
		return loads;
	}
	
	public void setLoads(double[][] loads)
	{
		this.loads = loads;
	}
	
	public double getLoads(int i,int j)
	{
		return this.loads[i][j];
	}
	
	
	public void addLoads(double[][] load) 
	{
		int dimension = loads.length;
		for(int i=0;i<dimension;i++)
			for(int j=0;j<dimension;j++)
				this.loads[i][j]+=load[i][j];
			
	}
	
	public double[][] getU ()
	{
		return u;
	}
	
	public double getU (int i, int j)
	{
		return this.u[i][j];
	}

	public void setCongestion(double congestion)
	{
		this.congestion = congestion;
	}
	
	public double getCongestion()
	{
		return this.congestion;
	}
	
	// compute congestion values: load/ capacity
	public void computeU (CapWGraph graph)
	{
		int dimension = loads.length;
		
		u = new double[dimension][dimension];
		for(int i=0; i < dimension; i++)
			for(int j=0; j < dimension; j++)	
				u[i][j] = loads[i][j]/ graph.getCapacity(i,j);
	}
	
	
	
	public void saveLoads (String file) throws Exception
	{
		int dimension = loads.length;
		
		FileWriter f=new FileWriter(file);
		BufferedWriter W=new BufferedWriter(f);

		for (int i = 0; i < dimension; i++) 
		{
			for (int j = 0; j < dimension; j++) 
  				W.write(loads[i][j] + " ");
  			W.write("\n");
		}
		W.flush();
		W.close();
		f.close();
 	}
	
	//receive graph dimension?
	//set dimension as a variable?
	public void printLoads ()
	{
		int dimension = loads.length;
		DecimalFormat df=new DecimalFormat("#.##");
		for(int i=0; i < dimension; i++)
		{
			for(int j=0; j < dimension; j++)	
				System.out.print(df.format(loads[i][j]) + " ");
			System.out.println(" ");
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
	public double getTotalLoads(){
		int dimension=loads.length;
		double t=0.0;
		for(int i=0; i < dimension; i++)
			for(int j=0; j < dimension; j++)	
				t+=loads[i][j];
		return t;
	}

	
	public void setLoads(int i, int j, double value) {
		loads[i][j]=value;
		//NOTE : should update U? 
	}

	public NetworkTopology getTopology() {
		return topology;
	}


	
	
	/**
	 * @return Maximum Link Utilization
	 */
	public double getMLU() {
		double res=0.0;
		for(int i=0;i<u.length;i++)
			for(int j=0;j<u.length;j++)
				if(u[i][j]>res)
					res=u[i][j];
		return res;
	}
	
	
	
	
	/**
	 * @return Average Link Utilization
	 */
	public double getALU() {
		double sum=0.0;
		for(int i=0;i<u.length;i++)
			for(int j=0;j<u.length;j++){
				if(u[i][j]>=0)
					sum+=u[i][j];
			}
		
		return (sum/topology.getNumberEdges());
	}
	
	
	
	public double getVar(){
		/**
		 *   M := 0
  			 S := 0
  			 for k from 1 to N:
    			x := samples[k]
    			oldM := M
    			M := M + (x-M)/k
    			S := S + (x-M)*(x-oldM)
  			return S/(N-1)
		 */
		
		double res =0.0;
		for(int i=0;i<u.length;i++)
			for(int j=0;j<u.length;j++){
				
		}
		return res;
	}
	
	
	/**
	 * @return Minimum Link Utilization
	 */
	public double getlLLU() {
		double res=0.0;
		for(int i=0;i<u.length;i++)
			for(int j=0;j<u.length;j++)
				if(u[i][j]<res)
					res=u[i][j];
		return res;
	}

	
	
	
	/**
	 * @return Link Range Utilization (Maximum-Minimum)
	 */
	public double getUsageRange(){
		double max=0.0;
		double min=0.0;
		for(int i=0;i<u.length;i++)
			for(int j=0;j<u.length;j++)
				if(u[i][j]<min)
					min=u[i][j];
				else if(u[i][j]>max)
					max=u[i][j];
		return max-min;
	}

	public int getDimension() {
		return this.loads.length;
	}
	
}
