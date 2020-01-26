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

import java.io.Serializable;
import java.util.Collections;
import java.util.Vector;

/** Implements the shortest path algorithm (Dijkstra) in a oriented weighted graph (WGraph) */

@SuppressWarnings("serial")
public class Dijkstra implements Serializable
{

	WGraph graph; // weighted graph - should be oriented
	
	Vector<DijkstraNode> solution; // Vector of nodes (DijkstraNode objects) - final solution of execute

	private Vector<DijkstraNode> settled_nodes; // Vector of nodes - objects of class DijkstraNode
	private Vector<DijkstraNode> unsettled_nodes; // Vector of nodes - objects of class DijkstraNode

	public boolean checkAlternatives = true; // check for alternative routes

	public Dijkstra(WGraph g)
	{
		this.graph = g;
		settled_nodes = new Vector<DijkstraNode>();
		unsettled_nodes = new Vector<DijkstraNode>();
		solution = new Vector<DijkstraNode>();
	}

	public void clear ()
	{
		settled_nodes.clear();
		unsettled_nodes.clear();
	}

	public Vector<DijkstraNode> getSolution()
	{
		return solution;
	}

	public void execute()
	// finds pairs of shortest routes from all pairs of nodes
	// result goes to solution
	{
		for (int i=0; i< graph.getDimension(); i++)
		{
			executeAux(i);
			for(int k=0; k < settled_nodes.size(); k++)
				solution.add( settled_nodes.get(k) );
			this.clear();
		}
	}

	public void execute(int start) // gets distances to all reachable destinations from node start
	{
		executeAux(start);
		solution = settled_nodes;
		this.clear();
	}


	private void executeAux(int start) // gets distances to all reachable destinations from node start
	{
		DijkstraNode s = new DijkstraNode(start, start, start, 0.0);
		unsettled_nodes.add(s);

		DijkstraNode current;

		while ( (current = extractNode()) != null )
		{
			// mark settled u
			//markSettled(current);
			if(current.getNode() != start) markSettled(current); // do not mark start
			else unsettled_nodes.remove(current);

			// relax neighbors
			relaxNeighbors(current);
		}
	}

	public void execute(int start, int dest) //stops when dest is reached
	{
		DijkstraNode s = new DijkstraNode(start, start, start, 0.0);
		unsettled_nodes.add(s);

		DijkstraNode current;

		while ( (current = extractNode()) != null )
		{
			//markSettled(current);
			if(current.getNode() != start) markSettled(current); // do not mark start
			else unsettled_nodes.remove(current);

			if (current.getNode() == dest) 
			{
				solution = settled_nodes;
				this.clear();
				return;
			}

			// relax neighbors
			relaxNeighbors(current);
		}

		solution = null; // unreachable
	}

	public Vector<DijkstraNode> getPath (int source, int dest) // from the defined source
	{ // assumes execute is done before with a given start node
		DijkstraNode d = findNodeSol(source, dest);
		Vector<DijkstraNode> path = null;

		if (d != null) {
			path = new Vector<DijkstraNode>();
			path.add(d);

			DijkstraNode current = d;

			while (current != null) {
				current = getPredec (current);
				if(current != null) path.add(current);
			}
		}

		if (path != null) Collections.reverse(path);
		return path;
	}

	public double getDist (int source, int dest) // from the defined source
	{ // assumes execute is done previously
		DijkstraNode d = findNodeSol(source, dest);
		if(source == dest) return 0.0;
		else if(d != null) return d.getDist();	
		else return Double.MAX_VALUE;
	}

/** returns a graph with all arcs in shortest paths to dest */
	public Graph getArcsShortestPath(int dest)
	{
		Graph res = new Graph(graph.getDimension());

		for(int i=0; i < graph.getDimension(); i++)
			for(int k=0; k < graph.getDimension(); k++)
				if (i!=k && graph.getConnection(i, k).equals(Graph.Status.UP) && 
						( getDist(i,dest) - getDist(k,dest) == graph.getWeight(i,k) )  )
					res.setConnection(i,k,Graph.Status.UP);
				else res.setConnection(i,k,Graph.Status.NOCONNECTION);

		return res;
	}

	public Graph getArcsShortestPath(int s, int dest, int[][] sparse)
	{
		Graph res = new Graph(graph.getDimension());

		for (int k = 0; k < sparse.length; k++)	
				if	( getDist(s,dest) - getDist(s,sparse[k][0]) - getDist(sparse[k][1],dest) == graph.getWeight(sparse[k][0],sparse[k][1]) )  
					res.setConnection(sparse[k][0],sparse[k][1],Graph.Status.UP);
				else res.setConnection(sparse[k][0],sparse[k][1],Graph.Status.NOCONNECTION);

		return res;
	}


	
/** gets all DijkstraNodes with destination equal to dest.
* Assumes execute() was done before to build solution */
	public Vector<DijkstraNode> getNodesForDest (int dest)
	{
		Vector<DijkstraNode> res = new Vector<DijkstraNode>();

		for (int i=0; i< solution.size(); i++)
		{
			DijkstraNode temp = (DijkstraNode)solution.get(i);
			if (temp.getNode() == dest) res.add(temp); 
		}

		return res;
	}
	

/** gets next node to handle */
	private DijkstraNode extractNode ()
	{
		DijkstraNode res= null;
		if (unsettled_nodes.size() > 0 ) {
			res = (DijkstraNode)(unsettled_nodes.get(0));
			// choose minimum distance
			for (int i=1; i< unsettled_nodes.size(); i++)
			{
				DijkstraNode temp = (DijkstraNode)unsettled_nodes.get(i);
				if (temp.getDist() < res.getDist()) res = temp;
			}
		}
		return res;
	}

/** marks a node as settled */
	private void markSettled(DijkstraNode n)
	{
		unsettled_nodes.remove(n);
		settled_nodes.add(n);
	}
	
	private void relaxNeighbors(DijkstraNode n)
	{
		int node = n.getNode();
		int dim = graph.getDimension();

		for(int i=0; i< dim; i++)
		{
			if( i!=node && !isSettled(i) && graph.getConnection(node, i).equals(Graph.Status.UP) ) // neighbors
			{
				double d = graph.getWeight(node, i) + n.getDist();
				DijkstraNode t = isUnsettled(i);
				if ( t != null ) { // node is on the unsettled list
					if ( t.getDist() > d) {
						t.setDist(d);
						t.setPred(node);
						t.setAlternative(false);
					}
					else if ( t.getDist() == d ) 
					{
						t.setAlternative(true);
					}
				}
			 	else unsettled_nodes.add( new DijkstraNode(i, n.getSource(), node, d) );
			}
		}
	}	

/** checks if a node is in the settled list */
	private boolean isSettled (int node)
	{
		boolean res = false;
		for (int i=0; i< settled_nodes.size(); i++)
		{
			DijkstraNode temp = (DijkstraNode)settled_nodes.get(i);
			if (temp.getNode() == node) res = true;
		}
		return res;
	}

	private DijkstraNode isUnsettled (int node)
	{
		DijkstraNode res = null;
		for (int i=0; i< unsettled_nodes.size() && res == null; i++)
		{
			DijkstraNode temp = (DijkstraNode)unsettled_nodes.get(i);
			if (temp.getNode() == node) res = temp;
		}
		return res;
	}

/** get predecessor node from a given node */
	private DijkstraNode getPredec (DijkstraNode n)
	{
		int pred = n.getPred();
		DijkstraNode res = null;
	
		if(n.getNode() == n.getSource()) res = null;
		else res = findNodeSol(n.getSource(), pred); 

		return res;
	}

	public DijkstraNode findNodeSol (int source, int dest)
	{
		DijkstraNode res = null;
	
		for (int i=0; i< solution.size() && res == null; i++)
		{
			DijkstraNode temp = (DijkstraNode)solution.get(i);
			if (temp.getNode() == dest && temp.getSource()==source) 
				res = temp;
		}
		return res;
	}


/* prints a solution; assumes execute is done before */
	public void printSol ()
	{
		for (int i=0; i< solution.size(); i++)
			((DijkstraNode)solution.get(i)).print();
	}

	public static void main (String[] args)
	{
		try{
			WGraph g = new WGraph("sj5-all-ospf.gr", true);
			Dijkstra d = new Dijkstra(g);
			d.execute();
			Vector<DijkstraNode> v = d.getSolution();
			DijkstraNode.print(v);
			d.printSol();
	
			for(int i = 0; i < g.getDimension(); i++)
				for (int j=0; j < g.getDimension(); j++)
					if (i != j )
					{
						Vector<DijkstraNode> p1 = d.getPath(i,j);
						if (p1 != null)
						{
							//System.out.println("Path "+ i + " to " + j);
							DijkstraNode.print_simp(p1);
						}
						
					}
/*			System.out.println("Dist.:" + d.getDist(0,3) );

			Graph asp = d.getArcsShortestPath(3);
			System.out.println("Arcs on shortest paths to 3");
			asp.print();
*/		}
		catch (Exception E)
		{
			E.printStackTrace();
		}
	}
}
