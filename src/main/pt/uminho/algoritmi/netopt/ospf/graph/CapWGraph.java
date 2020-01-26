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
 ******************************************************************************/
package pt.uminho.algoritmi.netopt.ospf.graph;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.Serializable;
import java.util.StringTokenizer;

/** Graph with weights on arcs and capacities on arcs */

@SuppressWarnings("serial")
public class CapWGraph extends WGraph implements Serializable
{


/** capacities of the edges */
 double[][] capacities;

 public CapWGraph()
 {}

 public CapWGraph(int d)
 {
	super(d);
	capacities = new double [dimension][dimension]; // oriented 
 }

 public CapWGraph(int d, boolean or)
 {
	oriented = or;
	complete = false;
	allocate(d);
 }

 public CapWGraph(String filename, boolean oriented) throws Exception
 {
	FileReader f = new FileReader(filename);
	BufferedReader B = new BufferedReader(f);
	this.oriented = oriented;
	read(B);
 }

 
 public CapWGraph(String filename, int format) throws Exception
 {
 	FileReader f = new FileReader(filename);
	BufferedReader B = new BufferedReader(f);
	read(B, format);
 }


 
 public void allocate (int d)
 {
 	super.allocate(d);
	if(oriented)
		capacities = new double [dimension][dimension]; 
	else
	{
		capacities = new double[dimension][];
		for(int i=0; i<dimension; i++)
			capacities[i] = new double[i+1];
	}
 }
 

 public double getCapacity(int n1, int n2)
 {
 	double res;
	if(oriented) res = capacities[n1][n2];
	else
	{
		if(n1>n2) res = capacities[n1][n2];
		else res = capacities[n2][n1];
	}
	return res;
 }

 public void setCapacity(int n1, int n2, double w)
 {
	if(oriented) capacities[n1][n2] = w;
	else
	{
		if(n1>n2) capacities[n1][n2]=w;
		else capacities[n2][n1]=w;
	}
 }

 public double averageCapacity()
 {
	double sum = 0.0;
	int ne = 0;

	int nrows = connections.length;
	for(int i=0; i< nrows; i++) {
		int ncols = connections[i].length; 
		for(int j=0; j< ncols; j++)
			if( getConnection(i,j).equals(Graph.Status.UP) ) {
				sum += getCapacity(i,j); 
				ne++;
			}
	}

	return (sum/ne);
 }

 public void writecons(BufferedWriter B) throws Exception
 {
	B.write(dimension+"\n");
	int nrows = connections.length;
	for(int i=0; i< nrows; i++)
	{
		int ncols = connections[i].length; 
		for(int j=0; j< ncols; j++)
			if( getConnection(i,j).equals(Graph.Status.UP) ) B.write(getWeight(i,j)+" ");
			else B.write("X ");
		B.write("\n");
	}
	for(int i=0; i< nrows; i++)
	{
		int ncols = connections[i].length; 
		for(int j=0; j< ncols; j++)
			if( getConnection(i,j).equals(Graph.Status.UP) ) B.write(getCapacity(i,j)+" ");
			else B.write("X ");
		B.write("\n");
	}
	B.flush();
 }

 public void read_lower(BufferedReader B) throws Exception
 {
 	// read dimension
	String str = B.readLine();
	allocate(Integer.parseInt(str));

	for(int i=0; i<dimension; i++)
	{	
		str = B.readLine();
		StringTokenizer st = new StringTokenizer(str);
		if(st.countTokens()!=i+1) 
				throw new Exception("Error in file format: class WGraph"); 
		for(int j=0; j<=i ; j++)
		{
			String token = st.nextToken();
			if(token.equals("X")) setConnection(i,j,Graph.Status.NOCONNECTION);
			else
			{
				setConnection(i,j,Graph.Status.UP);
				double w = Double.valueOf(token).doubleValue();
				setWeight(i,j,w);
			}
		}
	}
	for(int i=0; i<dimension; i++)
	{	
		str = B.readLine();
		StringTokenizer st = new StringTokenizer(str);
		if(st.countTokens()!=i+1) 
				throw new Exception("Error in file format: class WGraph"); 
		for(int j=0; j<=i ; j++)
		{
			String token = st.nextToken();
			if(!token.equals("X")) 
			{
				double w = Double.valueOf(token).doubleValue();
				setCapacity(i,j,w);
			}
		}
	}
 }

 public void readoriented(BufferedReader B) throws Exception
 {
 	// read dimension
	String str = B.readLine();
	allocate(Integer.parseInt(str));

	for(int i=0; i<dimension; i++)
	{	
		str = B.readLine();
		StringTokenizer st = new StringTokenizer(str);
		if(st.countTokens()!=dimension) 
				throw new Exception("Error in file format: class Graph"); 
		for(int j=0; j< dimension; j++)
		{
			String token = st.nextToken();
			if(token.equals("X")) setConnection(i,j,Graph.Status.NOCONNECTION);
			else
			{
				setConnection(i,j,Graph.Status.UP);
				double w = Double.valueOf(token).doubleValue();
				setWeight(i,j,w);
			}
		}
	}
	for(int i=0; i<dimension; i++)
	{	
		str = B.readLine();
		StringTokenizer st = new StringTokenizer(str);
		if(st.countTokens()!=dimension) 
				throw new Exception("Error in file format: class Graph"); 
		for(int j=0; j< dimension; j++)
		{
			String token = st.nextToken();
			if(!token.equals("X")) 
			{
				double w = Double.valueOf(token).doubleValue();
				setCapacity(i,j,w);
			}
		}
	}
 }

 public void read_upper(BufferedReader B) throws Exception
 {
	// read dimension
	String str = B.readLine();
	allocate(Integer.parseInt(str));

	for(int j=0; j<dimension; j++)
		setConnection(j,j, Graph.Status.NOCONNECTION);
	
	for(int i=0; i<dimension-1; i++)
	{	
		str = B.readLine();
		StringTokenizer st = new StringTokenizer(str);
		if(st.countTokens()!=dimension-i-1) 
				throw new Exception("Error in file format: class Graph"); 
		for(int j=1; j< dimension-i; j++)
		{
			String token = st.nextToken();
			if(token.equals("X")) setConnection(i,j,Graph.Status.NOCONNECTION);
			else
			{
				setConnection(i,j,Graph.Status.UP);
				double w = Double.valueOf(token).doubleValue();
				setWeight(i,j,w);
			}
		}
	}
	for(int i=0; i<dimension-1; i++)
	{	
		str = B.readLine();
		StringTokenizer st = new StringTokenizer(str);
		if(st.countTokens()!=dimension-i-1) 
				throw new Exception("Error in file format: class Graph"); 
		for(int j=1; j< dimension-i; j++)
		{
			String token = st.nextToken();
			if(!token.equals("X")) 
			{
				double w = Double.valueOf(token).doubleValue();
				setCapacity(i,j,w);
			}
		}
	}
 }

 
 public double[][] getCapacitie(){
	 return this.capacities;
 }

 public static void main(String [] args)
 {

	try{

		CapWGraph g = new CapWGraph("cor.gr", true);
		g.print();
	}
	catch(Exception e)
	{
		e.printStackTrace();
	}

 }


}
