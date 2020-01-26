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
package pt.uminho.algoritmi.netopt.ospf.simulation.net;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import pt.uminho.algoritmi.netopt.ospf.graph.CapWGraph;
import pt.uminho.algoritmi.netopt.ospf.graph.Graph;
import pt.uminho.algoritmi.netopt.ospf.graph.Graph.Status;
import pt.uminho.algoritmi.netopt.ospf.graph.WGraph;
import pt.uminho.algoritmi.netopt.ospf.simulation.exception.DimensionErrorException;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetNode.NodeType;


@SuppressWarnings("serial")
public class NetGraph implements Serializable {

	protected int nnodes;
	protected int nedges;
	protected NetNode[] nodes;
	public NetEdge[] edges;
	
	
	/**
	 * @param filenameNodes
	 * @param filenameEdges
	 * @throws Exception
	 *             Receive both Nodes and Edges
	 */
	public NetGraph(String filenameNodes, String filenameEdges)
			throws Exception {
		FileReader f = new FileReader(filenameNodes);
		BufferedReader B = new BufferedReader(f);

		String str = B.readLine();
		nnodes = Integer.valueOf(str).intValue();
		nodes = new NetNode[nnodes];
		for (int i = 0; i < nnodes; i++) {
			str = B.readLine();
			nodes[i] = new NetNode(str);
		}

		B.close();
		f.close();

		f = new FileReader(filenameEdges);
		B = new BufferedReader(f);

		str = B.readLine();
		nedges = Integer.valueOf(str).intValue();
		edges = new NetEdge[nedges];
		for (int i = 0; i < nedges; i++) {
			str = B.readLine();
			edges[i] = new NetEdge(str);
		}
		B.close();
		f.close();
	}

	
	public NetGraph(String filename) throws Exception {
		FileReader f = new FileReader(filename + ".nodes");
		BufferedReader b = new BufferedReader(f);

		String str = b.readLine();
		nnodes = Integer.valueOf(str).intValue();
		nodes = new NetNode[nnodes];
		for (int i = 0; i < nnodes; i++) {
			str = b.readLine();
			nodes[i] = new NetNode(str);
		}

		b.close();
		f.close();

		f = new FileReader(filename + ".edges");
		b = new BufferedReader(f);

		str = b.readLine();
		nedges = Integer.valueOf(str).intValue();
		edges = new NetEdge[nedges];
		for (int i = 0; i < nedges; i++) {
			str = b.readLine();
			edges[i] = new NetEdge(str);
		}
		b.close();
		f.close();

	}

	
	public NetGraph(NetNode[] nodes, NetEdge[] edges) {
		this.nodes = nodes;
		this.edges = edges;
		this.nnodes = nodes.length;
		this.nedges = edges.length;
	}

	public NetGraph() {
		this.nodes = new NetNode[0];
		this.edges = new NetEdge[0];
		this.nnodes = 0;
		this.nedges = 0;
	}

	public NetEdge[] getEdges() {
		return edges;
	}

	public void setEdges(NetEdge[] edges) {
		this.edges = edges;
	}

	public NetNode getNodeByID(int i) {
		for (NetNode node : this.nodes) {
			if (node.getNodeId() == i) {
				return node;
			}
		}
		return null;
	}
	
	
	public NetEdge getEdgeByID(int i) throws NullPointerException {
		for (NetEdge edge : this.edges) {
			if (edge.getEdgeId() == i) {
				return edge;
			}
		}
		throw new NullPointerException("No edge with id="+i);
	}
	
	
	public NetNode getNodeAt(int i){
		return this.nodes[i];
	}

	public NetNode[] getNodes() {
		return nodes;
	}

	public void setNodes(NetNode[] nodes) {
		this.nodes = nodes;
	}

	public int getNNodes() {
		return this.nnodes;
	}

	public int getNEdges() {
		return this.nedges;
	}

	public NetEdge getEdge(int from, int to) throws NullPointerException {

		for (int i = 0; i < edges.length; i++) {
			NetEdge e = edges[i];
			if ((e.getFrom() == from && e.getTo() == to)
					|| (e.getFrom() == to && e.getTo() == from))
				return e;
		}
		throw new NullPointerException();
	}

	//
	public CapWGraph createGraph() {
		CapWGraph graph = new CapWGraph(nnodes, true);

		for (int i = 0; i < nnodes; i++)
			for (int j = 0; j < nnodes; j++) {
				graph.setConnection(i, j, Graph.Status.NOCONNECTION);
				graph.setCapacity(i, j, 0.0);
				graph.setWeight(i, j, 0.0);
			}

		for (int i = 0; i < nedges; i++) {
			graph.setCapacity(edges[i].from, edges[i].to, edges[i].bandwidth);
			graph.setWeight(edges[i].from, edges[i].to, edges[i].length);
			graph.setCapacity(edges[i].to, edges[i].from, edges[i].bandwidth);
			graph.setWeight(edges[i].to, edges[i].from, edges[i].length);
			
			if(edges[i].isUP()){
				graph.setConnection(edges[i].from, edges[i].to, Graph.Status.UP);
				graph.setConnection(edges[i].to, edges[i].from, Graph.Status.UP);
			}
			else{
				graph.setConnection(edges[i].from, edges[i].to, Graph.Status.DOWN);
				graph.setConnection(edges[i].to, edges[i].from, Graph.Status.DOWN);
			}
			
		}

		return graph;
	}

	// creates WGraph only with topology and no weights nor capacities
	public WGraph createWGraph() {
		WGraph graph = new WGraph(nnodes, true);

		for (int i = 0; i < nnodes; i++)
			for (int j = 0; j < nnodes; j++) {
				graph.setConnection(i, j, Graph.Status.NOCONNECTION);
				graph.setWeight(i, j, 0.0);
			}
		for (int i = 0; i < nedges; i++) {
			graph.setConnection(edges[i].from, edges[i].to, edges[i].getStatus());
			graph.setWeight(edges[i].from, edges[i].to, edges[i].length);

			graph.setConnection(edges[i].to, edges[i].from, edges[i].getStatus());
			graph.setWeight(edges[i].to, edges[i].from, edges[i].length);
		}

		return graph;
	}

	public WGraph createDelayGraph() {
		WGraph graph = new WGraph(nnodes, true);

		for (int i = 0; i < nnodes; i++)
			for (int j = 0; j < nnodes; j++) {
				graph.setConnection(i, j, Graph.Status.NOCONNECTION);
				graph.setWeight(i, j, 0.0);
			}

		for (int i = 0; i < nedges; i++) {
			graph.setConnection(edges[i].from, edges[i].to, edges[i].getStatus());
			graph.setWeight(edges[i].from, edges[i].to, edges[i].delay);

			graph.setConnection(edges[i].to, edges[i].from, edges[i].getStatus());
			graph.setWeight(edges[i].to, edges[i].from, edges[i].delay);
		}
		return graph;
	}
	
	
	public double[][] getDelays() {
		double[][] res = new double[nnodes][nnodes];
		for (int i = 0; i < nedges; i++) {
			res[edges[i].from][edges[i].to] = edges[i].delay;
			res[edges[i].to][edges[i].from] = edges[i].delay;
		}
		return res;
	}

	public double[][] getBandwidths() {
		double[][] res = new double[nnodes][nnodes];
		for (int i = 0; i < nedges; i++) {
			res[edges[i].from][edges[i].to] = edges[i].bandwidth;
			res[edges[i].to][edges[i].from] = edges[i].bandwidth;
		}
		return res;
	}

	public double maxLinkLength() {
		double res = 0.0;

		for (int i = 0; i < nedges; i++)
			if (edges[i].length > res)
				res = edges[i].length;

		return res;
	}

	public double minLinkLength() {
		double res = Double.MAX_VALUE;

		for (int i = 0; i < nedges; i++)
			if (edges[i].length < res)
				res = edges[i].length;

		return res;
	}

	public double maxLinkBandwidth() {
		double res = 0.0;

		for (int i = 0; i < nedges; i++)
			if (edges[i].bandwidth > res)
				res = edges[i].bandwidth;

		return res;
	}

	public double minLinkBandwidth() {
		double res = Double.MAX_VALUE;

		for (int i = 0; i < nedges; i++)
			if (edges[i].bandwidth < res)
				res = edges[i].bandwidth;

		return res;
	}

	public void print() {
		System.out.println(nnodes + " nodes");
		for (int i = 0; i < nnodes; i++)
			nodes[i].print();

		System.out.println(nedges + " edges");
		for (int i = 0; i < nedges; i++)
			edges[i].print();
	}



	public void setBandwidth(int from, int to, double bandwidth) {
		for (int i = 0; i < nedges; i++) {
			if ((edges[i].getFrom() == from && edges[i].getTo() == to)
					|| ((edges[i].getFrom() == to && edges[i].getTo() == from))) {
				edges[i].setBandwidth(bandwidth);
				return;
			}
		}
	}


	
	public NetGraph copy() {
		NetNode[] cnodes = new NetNode[this.nnodes];
		for (int i = 0; i < this.nnodes; i++) {
			cnodes[i] = this.nodes[i].copy();
		}
		NetEdge[] cedges = new NetEdge[this.nedges];
		for (int i = 0; i < this.nedges; i++) {
			cedges[i] = this.edges[i].copy();
		}
		return new NetGraph(cnodes, cedges);
	}

	
	
	
	
	/**
	 * Verify if the graph is connected
	 * 
	 * @param allowBandWidthZero
	 *            : true 0 bandwidth as considered as connected false 0
	 *            bandwidth as considered as unconnected
	 * @return true if connected
	 */

	public boolean isConnected(boolean allowBandWidthZero) {

		CapWGraph g = this.createGraph();
		Queue<Integer> q = new LinkedList<Integer>();
		Boolean result[] = new Boolean[nnodes];
		for (int i = 0; i < nnodes; i++) {
			result[i] = false;
		}
		// starting node could be randomly selected
		q.add(0);
		result[0] = true;

		while (!q.isEmpty()) {
			int position = q.remove();
			df(position, q, result, g, allowBandWidthZero);
		}

		for (int i = 0; i < nnodes; i++)
			if (result[i] == false)
				return false;
		return true;
	}

	/**
	 * Depth First search traversal algorithm implementation used by @see
	 * isConnected(boolean allowBandWidthZero)
	 * 
	 * @param position
	 * @param q queue
	 * @param r traversed nodes list
	 * @param gaph
	 * @param allowBandWidthZero
	 */
	private void df(int position, Queue<Integer> q, Boolean[] r, CapWGraph g,
			boolean allowBandWidthZero) {

		for (int i = 0; i < nnodes; i++) {
			if (position != i
					&& g.getConnection(position, i).equals(Graph.Status.UP)
					&& (allowBandWidthZero || g.getCapacity(position, i) > 0)) {
				if (!q.contains(i) && !r[i]) {
					q.add(i);
					r[i] = true;
				}
			}
		}
	}

	public boolean existEdge(int from, int to) {
		for (int i = 0; i < this.nedges; i++) {
			if (this.edges[i].getFrom() == from && this.edges[i].getTo() == to)
				return true;
			else if (this.edges[i].getFrom() == to && this.edges[i].getTo() == from)
				return true;
		}
		return false;
	}

	
	public int greatestEdgeID() {
		int id=-1;
		for (int i = 0; i < this.edges.length; i++) {
			if (this.edges[i].getEdgeId()>id)
				id=this.edges[i].getEdgeId();
		}
		return id;
	}
	
	private int greatestNodeID(){
		int id=0;
		for(int i=0;i<nodes.length;i++){
			NetNode node=nodes[i];
			if(node.getNodeId()>id)
				id=node.getNodeId();
		}
		return id;
	}

	
	
	
	public boolean addEdge(NetEdge e) {
		// verify if edge already exists
		if (!existEdge(e.getFrom(),e.getTo())) {
			NetEdge[] ed = new NetEdge[nedges+1];
			System.arraycopy(edges, 0, ed, 0, nedges);
			nedges++;
			ed[nedges-1] = e;
			this.edges = ed;
			
			//node in/out degree
			int in=nodes[e.getFrom()].getIndegree();
			nodes[e.getFrom()].setIndegree(in+1);
			nodes[e.getFrom()].setOutdegree(in+1);
			in=nodes[e.getTo()].getIndegree();
			nodes[e.getTo()].setIndegree(in+1);
			nodes[e.getTo()].setOutdegree(in+1);
			return true;
		}
		else
			return false;
	}
	
	
	public boolean addNode(NetNode node){
		try{
			NetNode[] nd;
			if(nnodes==0){
				//TODO: Change code to turn id independent from array positioning
				node.setNodeId(0);
				nd=new NetNode[1];
				nd[0]=node;
			}
			else
			{
				node.setNodeId(greatestNodeID()+1);
				nd = new NetNode[nodes.length+1];
				System.arraycopy(nodes, 0, nd, 0, nodes.length);
				nd[nd.length-1] = node;
			}
			this.nodes =  nd;
			nnodes=nodes.length;
			return true;
			
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	
	/*
	 * Remove a node and all edges atached to it
	 */ 
	public boolean removeNode(NetNode node){
		boolean flag=false;
		ArrayList<NetNode> aux = new ArrayList<NetNode>();
		for(int i=0;i<nnodes;i++){
			NetNode anode=getNodeAt(i);
			if(anode.equals(node))
				flag=true;
			else
				aux.add(anode);
		}
		if(flag){
			NetNode[] newnodes=new NetNode[aux.size()];
			aux.toArray(newnodes);
			this.nodes=newnodes;
			this.nnodes=nodes.length;
			
			//remove attached edges
			int c = 0;
			ArrayList<NetEdge> v = new ArrayList<NetEdge>();
			for (int i = 0; i < this.nedges; i++) {
				NetEdge e = this.edges[i];
				if (e.getFrom()==node.getNodeId()||e.getTo()==node.getNodeId()) {
					c++;
				} else {
					v.add(e);
				}
			}
			if (c > 0) {
				NetEdge[] newedges = new NetEdge[v.size()];
				v.toArray(newedges);
				this.edges = newedges;
				this.nedges = newedges.length;
			}
		}
		
		return flag;
	}
	
	
	
	public NetEdge getEdge(int i){
		return edges[i];
	} 
	
	
	
	public void removeEdge(NetEdge edge) {
		int c = 0;
		ArrayList<NetEdge> v = new ArrayList<NetEdge>();
		for (int i = 0; i < this.nedges; i++) {
			NetEdge e = this.edges[i];
			if (e.equals(edge)) {
				c++;
			} else {
				v.add(e);
			}
		}
		if (c > 0) {
			NetEdge[] newedges = new NetEdge[v.size()];
			v.toArray(newedges);
			this.edges = newedges;
			this.nedges = newedges.length;
		}
	}
	
	
	
	
	
	public NetNode[] getNeighbors(NetNode node){
		ArrayList<NetNode> l=new ArrayList<NetNode>();
		for(NetNode next:nodes){
			if(this.existEdge(node.getNodeId(),next.getNodeId()) && getEdge(node.getNodeId(), next.getNodeId()).isUP())
				l.add(next);
		}
		NetNode[] adjNodes=new NetNode[l.size()];
		l.toArray(adjNodes);
		return adjNodes;
	}


	public void setAllNodesType(NodeType type) {
		for(int i=0;i<nodes.length;i++)
			nodes[i].setNodeType(type);
	}
	
	
	public void applyNodeTypeConfiguration(NodeType[] conf) throws DimensionErrorException{
		if(conf.length!=this.getNNodes())
			throw new DimensionErrorException("NodeType length is "+conf.length);
		for(int i=0;i<getNNodes();i++){
			this.getNodeAt(i).setNodeType(conf[i]);
		}
	}
	
	
	public List<NetEdge> getEdges(Status status){
		List<NetEdge> list = new ArrayList<NetEdge>();
		for(NetEdge e : this.edges)
			if(status.equals(e.getStatus()))
				list.add(e);
		return list;
	}
	
	
	
	public NetGraph subGraph(NodeType type){
		ArrayList<NetNode> snodes= new ArrayList<NetNode>();
		ArrayList<NetEdge> sedges= new ArrayList<NetEdge>();
		int index=0;
		for(NetNode n:nodes){
			if(n.getNodeType().equals(type)){
				NetNode nn =n.copy();
				nn.setProperty(NodeProperty.ID, ""+nn.getNodeId());
				nn.setNodeId(index);
				index++;
				snodes.add(nn);
			}
		}
		for(NetEdge e:edges){
			boolean from =false, to =false;
			int fromIndex =0;
			int toIndex =0;
			for(NetNode a:snodes){
				  int id = Integer.valueOf(a.getProperty(NodeProperty.ID));
				  if(id==e.getFrom()){
					  from =true;
					  fromIndex = a.getNodeId();
				  }
				  else if(id==e.getTo()){
					  to =true;
					  toIndex = a.getNodeId();
				  }
			}		
			if(from && to){
				NetEdge b =e.copy();
				b.setFrom(fromIndex);
				b.setTo(toIndex);
				//renumber edges ID?
				sedges.add(b);
			}
		}
		/*
		System.out.println("Subgraph Nodes");
		for(NetNode node:snodes){
			System.out.print(node.getNodeId()+"["+node.getProperty(NodeProperty.ID)+"] ");
		}
		System.out.println();
		for(NetEdge edge:sedges)
			System.out.print(edge.toString()+" ");
		System.out.println();		
		*/
		return new NetGraph((NetNode[])snodes.toArray(new NetNode[snodes.size()]),(NetEdge[])sedges.toArray(new NetEdge[sedges.size()]));
	}
	
	
	

	public boolean isConnected(NodeType type,boolean allowBandWidthZero){
		NetGraph g = subGraph(type);
		if(g.getNNodes()==0)
			return true;
		else
			return g.isConnected(allowBandWidthZero);
	}


	public List<NetNode> getNodesByType(NodeType type) {
		ArrayList<NetNode> list =new ArrayList<NetNode>();
		for(NetNode node:nodes)
			if(node.getNodeType().equals(type))
				list.add(node);
		return list;
	}
	
}
