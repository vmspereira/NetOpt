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
package pt.uminho.algoritmi.netopt.ospf.graph;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.Serializable;
import java.util.StringTokenizer;


@SuppressWarnings("serial")
public class WGraph extends Graph implements Serializable
{


/** weights of the edges; has no meaning if connection does not exist;*/
 double[][] weights;

 public WGraph()
 {}

 public WGraph(int d)
 {
	super(d);
	weights = new double [dimension][dimension]; // oriented 
 }

 public WGraph(int d, boolean or)
 {
	oriented = or;
	complete = false;
	allocate(d);
 }

 public WGraph(String filename, boolean oriented) throws Exception
 {
	FileReader f = new FileReader(filename);
	BufferedReader B = new BufferedReader(f);
	this.oriented = oriented;
	read(B);
 }

 public WGraph(String filename, int format) throws Exception
 {
 	FileReader f = new FileReader(filename);
	BufferedReader B = new BufferedReader(f);
	read(B, format);
 }
 
 
 public WGraph(double[][] weights, double noValue){
	 super(weights.length);
	 this.weights=weights;
	 for(int i=0;i<dimension;i++)
			for(int j=0;j<dimension;j++){
				if(weights[i][j]!=noValue)
				connections[i][j]=Status.UP;
			}
				
 }


 public WGraph(double[][] weights) {
	this(weights,0.0);
}

public void allocate (int d)
 {
 	super.allocate(d);
	if(oriented)
		weights = new double [dimension][dimension]; 
	else
	{
		weights = new double[dimension][];
		for(int i=0; i<dimension; i++)
			weights[i] = new double[i+1];
	}
 }

 public double getWeight(int n1, int n2)
 {
 	double res;
	if(oriented) res = weights[n1][n2];
	else
	{
		if(n1>n2) res = weights[n1][n2];
		else res = weights[n2][n1];
	}
	return res;
 }

 public void setWeight(int n1, int n2, double w)
 {
	if(oriented) weights[n1][n2] = w;
	else
	{
		if(n1>n2) weights[n1][n2]=w;
		else weights[n2][n1]=w;
	}
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
	B.flush();
 }

 public void writedimacs(BufferedWriter B) throws Exception
 {
	B.write(dimension+"\n");
	B.write(countEdges()+"\n");
	int nrows = connections.length;
	for(int i=0; i< nrows; i++)
	{
		int ncols = connections[i].length; 
		for(int j=0; j< ncols; j++)
			if( getConnection(i,j).equals(Graph.Status.UP) ) B.write(i + " " + j + + getWeight(i,j)+ "\n");
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
 }

/** Reads graph in list of edges (DIMACS) format */
 public void readimacs(BufferedReader B) throws Exception
 {
	// read dimension
	String str = B.readLine();
	allocate(Integer.parseInt(str));

	for(int i=0; i<dimension; i++)
	{
		int ncols = (oriented? dimension: i+1);
		for(int j=0; j< ncols; j++)
			setConnection(i,j,Graph.Status.NOCONNECTION);
	}

	str = B.readLine();
	int nedges = Integer.parseInt(str);
	for(int i=0; i<nedges; i++)
	{	
		str = B.readLine();
		StringTokenizer st = new StringTokenizer(str);
		if(st.countTokens()!=3) 
				throw new Exception("Error in file format: class Graph"); 
		int c1 = Integer.parseInt(st.nextToken());
		int c2 = Integer.parseInt(st.nextToken());
		double w = Double.valueOf(st.nextToken()).doubleValue();
		setConnection(c1, c2, Graph.Status.UP);
		setWeight(c1, c2, w);
	}
 }
 
 
 public double[][] getWeights(){
	 return this.weights;
 }
 
 
}
