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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/**
 * Implements the shortest path algorithm (Dijkstra) in a oriented weighted
 * graph (WGraph)
 * 
 * 2012-09-08 Links may be UP, DOWN or NOT CONNECTED. 
 * 2012-08-10 Implements ECMP
 */

@SuppressWarnings("serial")
public class MatDijkstra implements Serializable {

	WGraph graph; // weighted graph - should be oriented
	
	//shortest distances between nodes
	double[][] dists;
	SPFElement[][] preds;
	boolean[][] alternatives;
	private boolean[][] settled;
	private boolean singleSP; 

	
	public MatDijkstra(WGraph g){
		this(g,true);
	}

	public MatDijkstra(WGraph g, boolean SSP) {
		this.graph = g;
		this.singleSP=SSP;
		preds = new SPFElement[graph.getDimension()][graph.getDimension()];
		dists = new double[graph.getDimension()][graph.getDimension()];
		settled = new boolean[graph.getDimension()][graph.getDimension()];
		alternatives = new boolean[graph.getDimension()][graph.getDimension()];
		init();
	}

	
	
	/** 
	 * build from a graph with no weights; 
	 * put unit weights
    * */
	public MatDijkstra(Graph gr,boolean SSP) 
	{
		this.singleSP=SSP;
		int n = gr.getDimension();
		this.graph = new WGraph(n);
		for (int i = 0; i < n; i++)
			for (int j = 0; j < n; j++)
				if (gr.getConnection(i, j).equals(Graph.Status.UP)) {
					this.graph.setConnection(i, j, Graph.Status.UP);
					this.graph.setWeight(i, j, 1);
				}

		preds = new SPFElement[graph.getDimension()][graph.getDimension()];
		dists = new double[graph.getDimension()][graph.getDimension()];
		settled = new boolean[graph.getDimension()][graph.getDimension()];
		alternatives = new boolean[graph.getDimension()][graph.getDimension()];
		init();
	}

	
	public MatDijkstra(Graph gr){
		this(gr,true);
	}
	
	
	public boolean isSingleSP(){
		return this.singleSP;
	}
	
	public void setSSP(boolean b){
		this.singleSP=b;
	}
	
	public void printSettled() {
		System.out.println("\n Settled");
		for (int i = 0; i < graph.getDimension(); i++) {
			for (int j = 0; j < graph.getDimension(); j++) {
				String str = "0 ";
				if (settled[i][j])
					str = "1 ";
				System.out.print(str);
			}
			System.out.println();
		}
		System.out.println();
	}

	
	
	public void printPred() {
		System.out.println("\n Predecessor");
		for (int i = 0; i < graph.getDimension(); i++) {
			for (int j = 0; j < graph.getDimension(); j++) {
				System.out.println("(" + i + "," + j + ")" + preds[i][j]);
			}
			System.out.println();
		}
		System.out.println();
	}

	public SPFElement[][] getSolPreds() {
		return preds;
	}

	public double[][] getShortestPathDistances() {
		return dists;
	}

	public boolean[][] getSolAlternatives() {
		return alternatives;
	}

	public void init() {
		for (int i = 0; i < this.graph.getDimension(); i++)
			for (int j = 0; j < this.graph.getDimension(); j++) {
				preds[i][j] = null;
				settled[i][j] = false;
				alternatives[i][j] = false;
				dists[i][j] = 0.0;
			}
	}

	// finds pairs of shortest routes from all pairs of nodes
	public void execute()
	{
		init();
		for (int i = 0; i < graph.getDimension(); i++)
			executeAux(i);
	}

	public void execute(int st) {
		init();
		executeAux(st);
	}

	public void executeAux(int st) {
		// System.out.println("* root at " + st);

		int dim = graph.getDimension();
		preds[st][st] = new SPFElement(st);
		int cur = st;

		while (cur != -1) {
			relaxNeighbors(st, cur);
			// printSettled();
			// printPred();
			settled[st][cur] = true;

			// get next node
			double min = Double.MAX_VALUE;
			int next = -1;
			for (int i = 0; i < dim; i++)
				if (i != st && preds[st][i] != null && !settled[st][i]) {
					if (dists[st][i] < min) {
						min = dists[st][i];
						next = i;
					}
				}
			// System.out.println("next node = " + next);
			cur = next;
		}

	}

	private void relaxNeighbors(int s, int node) {
		int dim = graph.getDimension();

		for (int i = 0; i < dim; i++) {
			if (i != node && !settled[s][i] && graph.getConnection(node, i).equals(Graph.Status.UP)) // neighbors
			{
				double d = graph.getWeight(node, i) + dists[s][node];
				double w = Double.MAX_VALUE; 
				if (preds[s][i] != null) { // node is on the unsettled list
					if (dists[s][i] > d) {
						dists[s][i] = d;
						preds[s][i].removeAll();
						preds[s][i].addNextHop(node);
						w=graph.getWeight(node, i);
						alternatives[s][i] = false;	
					} 
					else if (dists[s][i] == d)
					{
						if(!singleSP){
							alternatives[s][i] = true;
							preds[s][i].addNextHop(node);
						}else if(graph.getWeight(node, i)<w && singleSP){
							w=graph.getWeight(node, i);
							preds[s][i].removeAll();
							preds[s][i].addNextHop(node);							
						}
					}
					
				} else {

					preds[s][i] = new SPFElement(node);
					dists[s][i] = d;
					alternatives[s][i] = false;
					settled[s][i] = false;
				}
			}
		}
	}

	public Vector<Integer> getPath(int source, int dest) {
		List<Vector<Integer>> l = getAllPaths(source, dest);
		if (l == null)
			return null;
		else
			return l.get(0);
	}
	
	
	
	public int countAllPath(){
		int count =0;
		for(int i=0;i<this.graph.getDimension();i++){
			for(int j=0;j<this.graph.getDimension();j++)
				if(i!=j)
					count+=getAllPaths(i,j).size();
		}
		return count;
	}

	
	
	
	
	
	
	
	public List<Vector<Integer>> getAllPaths(int source, int dest) 
	{ 
		// assumes execute is done before with a given start node
		List<Vector<Integer>> list = new ArrayList<Vector<Integer>>();
		Vector<Integer> path = null;

		if (preds[source][dest] != null) {
			int pos = 0;
			path = new Vector<Integer>();
			path.add(dest);
			list.add(path);

			do {
				path = list.get(pos);
				int d = path.get(path.size() - 1);
				do {
					int n = preds[source][d].countHops();
					if (n > 1) {
						// clone path & add to vector
						for (int i = 1; i < n; i++) {
							@SuppressWarnings("unchecked")
							Vector<Integer> e = (Vector<Integer>) (path.clone());
							int v = preds[source][d].getNextHop(i);
							e.add(new Integer(v));
							list.add(e);
						}
					}
					d = preds[source][d].getNextHop();
					path.add(new Integer(d));

				} while (d != source);
				pos++;
			} while (pos < list.size());

			// reverse all paths
			for (int i = 0; i < list.size(); i++) {
				path = list.get(i);
				Collections.reverse(path);
			}

		} else
			return null;

		return list;
	}
	
	
	
	
	
	public double[][] getAveragePathLengths(){
		int n=this.graph.getDimension();
		double[][] lengths = new double[n][n];
		for(int i=0;i<n;i++){
			for(int j=0;j<n;j++){
				if(i==j){
					lengths[i][i]=0.0;
				}
				else{
					List<Vector<Integer>> l=getAllPaths(i,j);
					int sum = l.stream().map(Vector::size).reduce(0,
						       (a, b) -> a + b);
					lengths[i][j]=sum/l.size();
				}
			}
		}
		
		return lengths;
	} 
	
	

	public boolean existsPath(int source, int dest) {
		return (preds[source][dest] != null);
	}

	public double getDist(int source, int dest) // from the defined source
	{ // assumes execute is done previously
		if (preds[source][dest] != null)
			return dists[source][dest];
		else
			return Double.MAX_VALUE;
	}

	
	
	
	
	public Graph getArcsShortestPath(int dest) {
		Graph res = new Graph(graph.getDimension());
		
		for (int i = 0; i < graph.getDimension(); i++)
			for (int k = 0; k < graph.getDimension(); k++)
				if (i != k 	&& graph.getConnection(i, k).equals(Graph.Status.UP) && (getDist(i, dest) - getDist(k, dest) == graph.getWeight(i, k)))
					res.setConnection(i, k, Graph.Status.UP);
				else
					res.setConnection(i, k, Graph.Status.NOCONNECTION);
		return res;
	}

	
	
	
	
	public Graph getArcsShortestPath(int s, int dest, int[][] sparse) {
		Graph res = new Graph(graph.getDimension());

		for (int k = 0; k < sparse.length; k++)
			if (getDist(s, dest) - getDist(s, sparse[k][0])
					- getDist(sparse[k][1], dest) == graph.getWeight(
					sparse[k][0], sparse[k][1]))
				res.setConnection(sparse[k][0], sparse[k][1], Graph.Status.UP);
			else
				res.setConnection(sparse[k][0], sparse[k][1], Graph.Status.NOCONNECTION);

		return res;
	}
	
	
	

	// gets all arcs in the shortest path tree from root to a set of nodes
	public Graph getArcsTree(int root, int[] listDest) {
		Graph res = new Graph(graph.getDimension());

		for (int i = 0; i < listDest.length; i++) {
			if (preds[root][listDest[i]] != null) {
				int d = preds[root][listDest[i]].getNextHop();
				res.setConnection(d, listDest[i], Graph.Status.UP);
				int p = d;
				do {
					d = preds[root][p].getNextHop();
					res.setConnection(d, p, Graph.Status.UP);
					p = d;
				} while (d != root);
			}
		}
		return res;
	}

	public Vector<Integer> getNodesForDest(int dest) {
		Vector<Integer> res = new Vector<Integer>();
		for (int i = 0; i < preds.length; i++)
			if (i != dest && preds[i][dest] != null)
				res.add(new Integer(i));
		return res;
	}

	
	/**
	 *  getNodesOnShortestPaths(int source,int dest,boolean includeDest) is more efficient
	 *  
	 * @param source
	 * @param dest
	 * @return
	 */
	public List<Integer> getNodesOnShortestPaths(int source,int dest) {
		ArrayList<Integer> res=new ArrayList<Integer>();
		List<Vector<Integer>> all=getAllPaths(source,dest);
		Iterator<Vector<Integer>> it=all.iterator();
		while(it.hasNext()){
			Vector<Integer> v=it.next();
			for(int i=0;i<v.size();i++){
				Integer node=v.get(i);
				if(!res.contains(node) && node!=dest)
					res.add(node);
			}
		}
		return res;
	}

	
	

	public List<Integer> getNodesOnShortestPaths(int source,int dest,boolean includeDest){
		ArrayList<Integer> list =new ArrayList<Integer>();
		ArrayList<Integer> visit =new ArrayList<Integer>();
		
		if (preds[source][dest] != null) {
			if(includeDest)
				list.add(dest);
			visit.add(dest);
			do {
				int d = visit.get(0);
				int n = preds[source][d].countHops();
				for (int i = 0; i < n; i++) {
					int v = preds[source][d].getNextHop(i);
					if(!list.contains(v)){
						list.add(v);
					}
					if(!visit.contains(v))
						visit.add(v);
				}
				visit.remove(0);
			} while (visit.size()>0);
		} 
		return list;
	}


	public double getArcWeight(int currentNode, int nextNode) {
		return graph.getWeight(currentNode, nextNode);
	}
	
	
	
}
