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
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.StringTokenizer;
import java.util.Vector;


/* Graphs matrix like representation of a network, oriented or not. 
 * Links can have three status: Up, Down, NoConnection
 * */
@SuppressWarnings("serial")
public class Graph implements Serializable
{

	public static enum Status {
	    UP, NOCONNECTION, DOWN 
	}	
	
/** dimension - number of nodes in the graph */
 protected int dimension; 

/** connections in the graph; it is null if the graph is complete */
 Status[][] connections;

/** defines if the graph is complete */
 boolean complete;

/** defines if the graph is oriented */
 boolean oriented;

// graph formats
 public static final int ORIENTED = 0; // oriented graph (NxN 0/1 matrix)
 public static final int OR_DIMACS = 1; // DIMACS format (specify edges)
 public static final int NO_DIMACS = 2; // DIMACS format (specify edges)
 public static final int NO_UPPER = 3; // not oriented - upper triangular matrix 
 public static final int NO_LOWER = 4; // not oriented - lower triangular matrix

 public Graph()
 {
 }

/** Constructor from dimension. Assumes a oriented graph */
 public Graph(int d)
 {
 	this(d, true);
 }

 
 
/** Construction from dimension and flag: oriented or not */
 public Graph(int d, boolean or)
 {
	oriented = or;
	complete = false;
	allocate(d);
 }

 
 
 
/** Reads a graph from file - graph specified as 0/1 matrix with dimension in first row */
 public Graph(String filename, boolean oriented) throws Exception
 {
	FileReader f = new FileReader(filename);
	BufferedReader B = new BufferedReader(f);
	this.oriented = oriented;
	read(B);
 }

 
 
 
/** Reads a graph from file given the selected format */
 public Graph(String filename, int format) throws Exception
 {
	FileReader f = new FileReader(filename);
	BufferedReader B = new BufferedReader(f);
	read(B, format);
 }

/** Allocates the connections matrix given the dimension */
 public void allocate(int d)
 {
 	dimension = d;
	if(oriented){
		connections = new Status [dimension][dimension];
		for(int i=0;i<dimension;i++)
			for(int j=0;j<dimension;j++)
				connections[i][j]=Status.NOCONNECTION;
	}
	else
	{
		connections = new Status[dimension][];
		for(int i=0; i<dimension; i++){
			connections[i] = new Status[i+1];
			for(int j=0;j<i+1;j++)
				connections[i][j]=Status.NOCONNECTION;
		}
	}
 }
 
/** Returns the dimension (number of nodes) of the graph) */
 public int getDimension()
 {
	return dimension;
 }

/** Returns true if a connection between n1 and n2 exists and false otherwise */
 public Status getConnection(int n1, int n2)
 {
	 Status res;
	if(complete) res =  Status.UP;
	if(oriented) res =  connections[n1][n2];
	else
	{
		if(n1>n2) res = connections[n1][n2];
		else res = connections[n1][n2];
	}
	return res;
 }

/* Sets a connection between nodes n1 and n2 to true or false */
 public void setConnection(int n1, int n2, Status v)
 {
	if(oriented) connections[n1][n2] = v;
	else
		if(n1>n2) connections[n1][n2] = v;
		else connections[n2][n1]=v;
 }

 
 public void setConnections(Status[][] con)
 {
	 this.connections=con;
 }

 public void setComplete()
 {
	this.complete = true; 
	for(int i=0; i < connections.length; i++)
		for(int j=0; j<connections[i].length; j++)
			connections[i][j] = Status.UP;
 }
 
/* Returns the number of connections = true */
 public int countEdges()
 {
 	int cont = 0;
 	int nrows = connections.length;
	for(int i=0; i< nrows; i++)
	{
		int ncols = connections[i].length; 
		for(int j=0; j< ncols; j++)
			if( !getConnection(i,j).equals(Graph.Status.NOCONNECTION) ) cont++;
	}
	return cont;
 }
 
 public int countUpEdges()
 {
 	int cont = 0;
 	int nrows = connections.length;
	for(int i=0; i< nrows; i++)
	{
		int ncols = connections[i].length; 
		for(int j=0; j< ncols; j++)
			if(getConnection(i,j).equals(Graph.Status.UP) ) cont++;
	}
	return cont;
 }

 public int[][] getAllEdges()
 {
    int ne = countUpEdges();
	int [][] res = new int[ne][2];
 	int cont = 0;
 	int nrows = connections.length;
	for(int i=0; i< nrows; i++)
	{
		int ncols = connections[i].length; 
		for(int j=0; j< ncols; j++)
			if( getConnection(i,j).equals(Graph.Status.UP) ) 
			{	
				res[cont][0] = i;
				res[cont][1] = j;
				cont++;
			}
	}
	return res;
 }

/* returns all destinations nodes leaving from s*/
 public Vector<Integer> getDestinations(int s)
 {
	Vector<Integer> res = new Vector<Integer>(); 
	for(int i=0; i<dimension; i++)
			if (i!=s && getConnection(s,i).equals(Graph.Status.UP)) 
				res.add(new Integer(i));	
	return res;
 }

/** returns number of edges out of a node */
 public int outDegree(int node)
 {
	int res = 0;
	for(int i=0; i<dimension; i++)
			if (getConnection(node,i).equals(Graph.Status.UP)) res++;	
	return res;	
 }

 
 public List<Integer> outNodes(int node)
 {
	ArrayList<Integer> outlist= new ArrayList<Integer>();
	for(int i=0; i<dimension; i++)
			if (getConnection(node,i).equals(Graph.Status.UP)) 
				outlist.add(i);	
	return outlist;	
 }
 
 
 public int inDegree(int node)
 {
	int res = 0;
	for(int i=0; i<dimension; i++)
			if (getConnection(i, node).equals(Graph.Status.UP)) res++;	
	return res;	
 }


 public Vector<Vector<Integer>> getAllPaths(int origin, int dest)
 {
  Vector<Integer> cp =new Vector<Integer>();
  cp.add(new Integer(origin));
  Vector<Vector<Integer>> cs = new Vector<Vector<Integer>>();
  getAllPaths(origin, dest, cp, cs); 
  return cs;
 }

 public void getAllPaths(int origin, int dest, Vector<Integer> cp, Vector<Vector<Integer>> cs)
 {
   if (origin==dest) {cs.add(cp);}
	else 
	{
		for(int i=0; i<dimension; i++)
			if(getConnection(origin, i).equals(Graph.Status.UP) )
			{
				Vector<Integer> newpp=new Vector<Integer>();
				newpp.addAll(cp);
				//Vector<Integer> newpp= (Vector<Integer>)cp.clone(); 
				newpp.add(new Integer(i));
				getAllPaths(i, dest, newpp, cs);
			}	
	}
 }

// IO FUNCTIONS
 
/* Writes the graph to a Buffered Writer stream */
 public void write(BufferedWriter B) throws Exception
 {
	if(oriented)
		write(B, ORIENTED);
	else
		write(B, NO_LOWER);
	
 }

/* Writes the graph to a Buffered Writer stream given the format */
 public void write(BufferedWriter B, int format) throws Exception
 {
	switch (format)
	{
		case ORIENTED:
		case NO_LOWER:
			writecons(B);
			break;
		case NO_UPPER:
			System.out.println("not implemented");
			break;
		case OR_DIMACS:
		case NO_DIMACS:
			writedimacs(B);
			break;
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
			if( getConnection(i,j).equals(Graph.Status.UP) ) B.write("1 ");
			else B.write("0 ");
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
			if( getConnection(i,j).equals(Graph.Status.UP) ) B.write(i + " " + j + "\n");
	}
	B.flush();
 }

 public void read (BufferedReader B) throws Exception
 {
	if(oriented)
		read(B, ORIENTED);
	else
		read(B, NO_LOWER);
 }

 public void read (BufferedReader B, int format) throws Exception
 {
	switch (format)
	{
		case ORIENTED:
			oriented = true;
			readoriented(B);
			break;
		case NO_LOWER:
			oriented = false;
			read_lower(B);
			break;
		case NO_UPPER:
			oriented = false;
			read_upper(B);
			break;
		case OR_DIMACS:
			oriented = true;
			readimacs(B);
			break;
		case NO_DIMACS:
			oriented = false;
			readimacs(B);
			break;
	}
 }

 public void readoriented (BufferedReader B) throws Exception
 {
	// read dimension
	String str = B.readLine();
	//allocates space
	allocate(Integer.parseInt(str));

	for(int i=0; i<dimension; i++)
	{	
		str = B.readLine();
		StringTokenizer st = new StringTokenizer(str);
		if(st.countTokens()!=dimension) 
				throw new Exception("Error in file format: class Graph"); 
		for(int j=0; j< dimension; j++)
		{
			int c = Integer.parseInt(st.nextToken());
			if(c==1) setConnection(i,j,Status.UP);
			else if(c==0) setConnection(i,j,Status.NOCONNECTION);
			else throw new Exception("Error in file format: class Graph");
		}
	}
 }

/** Reads oriented graph in lower triangular matrix */
 public void read_lower(BufferedReader B) throws Exception
 {
	// read dimension
	String str = B.readLine();
	//allocates space
	allocate(Integer.parseInt(str));

	for(int i=0; i<dimension; i++)
	{	
		str = B.readLine();
		StringTokenizer st = new StringTokenizer(str);
		if(st.countTokens()!=i+1) 
				throw new Exception("Error in file format: class Graph"); 
		for(int j=0; j<=i; j++)
		{
			int c = Integer.parseInt(st.nextToken());
			if(c==1) setConnection(i,j,Status.UP);
			else if(c==0) setConnection(i,j,Status.NOCONNECTION);
			else throw new Exception("Error in file format: class Graph");
		}
	}
 }

/** Reads oriented graph in upper triangular matrix */
 public void read_upper(BufferedReader B) throws Exception
 {
	// read dimension
	String str = B.readLine();
	allocate(Integer.parseInt(str));

	for(int j=0; j<dimension; j++)
		setConnection(j,j, Status.NOCONNECTION);
	
	for(int i=0; i<dimension-1; i++)
	{	
		str = B.readLine();
		StringTokenizer st = new StringTokenizer(str);
		if(st.countTokens()!=dimension-i-1) 
				throw new Exception("Error in file format: class Graph"); 
		for(int j=1; j< dimension-i; j++)
		{
			int c = Integer.parseInt(st.nextToken());
			if(c==1) setConnection(i,j+i,Status.UP);
			else if(c==0) setConnection(i,j+i,Status.NOCONNECTION);
			else throw new Exception("Error in file format: class Graph");
		}
	}
 }

/** Reads graph in list of edges (DIMACS) format */
 public void readimacs(BufferedReader B) throws Exception
 {
	// read dimension
	String str = B.readLine();
	allocate(Integer.parseInt(str));
	//initialize connections to false	
	for(int i=0; i<dimension; i++)
	{
		int ncols = (oriented? dimension: i+1);
		for(int j=0; j< ncols; j++)
			setConnection(i,j,Status.NOCONNECTION);
	}

	str = B.readLine();
	int nedges = Integer.parseInt(str);
	for(int i=0; i<nedges; i++)
	{	
		str = B.readLine();
		StringTokenizer st = new StringTokenizer(str);
		if(st.countTokens()!=2) 
				throw new Exception("Error in file format: class Graph"); 
		int c1 = Integer.parseInt(st.nextToken());
		int c2 = Integer.parseInt(st.nextToken());
		setConnection(c1, c2, Status.UP);
	}
 }

 
 
 
 public void save(String filename) throws Exception
 {
	FileWriter f = new FileWriter(filename);
	BufferedWriter b = new BufferedWriter(f);
	write(b);
 }

 
 
 public void save(String filename, int format) throws Exception
 {
	FileWriter f = new FileWriter(filename);
	BufferedWriter b = new BufferedWriter(f);
	write(b, format);
 }

 
 
 public void load(String filename) throws Exception
 {
	FileReader f = new FileReader(filename);
	BufferedReader B = new BufferedReader(f);
	read(B);
 }

 
 
 
 public void load(String filename, int format) throws Exception
 {
	FileReader f = new FileReader(filename);
	BufferedReader B = new BufferedReader(f);
	read(B, format);
 }

 
 
 public void print() throws Exception
 {
	PrintWriter p = new PrintWriter(System.out);
	BufferedWriter b = new BufferedWriter(p);
	write(b);
 }

 
 
 public void print(int format) throws Exception
 {
	PrintWriter p = new PrintWriter(System.out);
	BufferedWriter b = new BufferedWriter(p);
	write(b, format);
 }


public void printStatus(){
	System.out.print("\n  ");
	for(int i=0;i< dimension;i++)
		System.out.print("["+i+"]");
	for(int i=0;i< dimension;i++){
		System.out.print("\n["+i+"]");
		for(int j=0;j<dimension;j++){
			if(connections[i][j]==Status.UP)
				System.out.print("U ");
			else if(connections[i][j]==Status.DOWN)
				System.out.print(". ");
			else if(connections[i][j]==Status.NOCONNECTION)
				System.out.print(". ");
		}
			
	}
}
 
 
 
 
 
 /**
  * Verify if the graph is connected
  * 
  * @param allowBandWidthZero : true   0 bandwidth as considered as connected
  * 							false  0 bandwidth as considered as unconnected
  * @return true if connected
  */
 
 public boolean isConnected(){
	 
	 
	 Queue<Integer> q =new LinkedList<Integer>();
	 Boolean result[]=new Boolean[dimension];
	 for(int i=0;i<dimension;i++)
	 {
		 result[i]=false;
	 }
	 // starting node could be randomly selected
	 q.add(0);
	 result[0]=true;
	 
	 while(!q.isEmpty()){
		 int position= q.remove();
	     df(position,q,result);	 
	 }
	 
	 for(int i=0;i<dimension;i++)
		 if(result[i]==false)
			 return false;
	 return true;
 }
 

 
 /**
  * depth first search traversal algorithm implementation used by @see isConnected(boolean allowBandWidthZero)
  * 
  * @param position
  * @param q queue
  * @param r traversed nodes list
  * @param gaph 
  * @param allowBandWidthZero 
  */
 private void df(int position, Queue<Integer> q, Boolean[] r){
	 
	 for(int i=0;i<dimension;i++){
		 if(  position!=i &&
			  getConnection(position, i).equals(Graph.Status.UP) 
		   ){
				 if(!q.contains(i) && !r[i])
				 {
					 q.add(i);
					 r[i]=true;
				 }
			}
	 } 
 }
 
 
 
  
  
}

