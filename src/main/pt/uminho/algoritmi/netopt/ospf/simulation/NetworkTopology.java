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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import pt.uminho.algoritmi.netopt.ospf.graph.CapWGraph;
import pt.uminho.algoritmi.netopt.ospf.graph.Graph;
import pt.uminho.algoritmi.netopt.ospf.graph.Graph.Status;
import pt.uminho.algoritmi.netopt.ospf.graph.MatDijkstra;
import pt.uminho.algoritmi.netopt.ospf.graph.SPFElement;
import pt.uminho.algoritmi.netopt.ospf.graph.WGraph;
import pt.uminho.algoritmi.netopt.ospf.listener.ITopologyChangeListener;
import pt.uminho.algoritmi.netopt.ospf.listener.TopologyEvent;
import pt.uminho.algoritmi.netopt.ospf.simulation.exception.DimensionErrorException;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetEdge;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetGraph;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetNode;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetNode.NodeType;
import pt.uminho.algoritmi.netopt.ospf.utils.MathUtils;

@SuppressWarnings("serial")
public class NetworkTopology implements Serializable,ITopologyChangeListener {

	private MatDijkstra shortestPathGraph; // results shortest path algorithm (graph weights
								// = OSPF weights)
	private MatDijkstra euclidianDistanceSPGraph; // results shortest path algorithm (graph weights
								// = euclidean dist)
	protected CapWGraph graph; // graph of nodes and links, weights, capacities
	private String filenameNodes, filenameEdges;
	private NetGraph bgr; // keeps information for nodes and edges
	private String name;

	// Topology Change Listener
	public ArrayList<ITopologyChangeListener> listeners = new ArrayList<ITopologyChangeListener>();

	public NetworkTopology(String filename) throws Exception {
		this.bgr = new NetGraph(filename);
		this.graph = bgr.createGraph();
		this.shortestPathGraph = new MatDijkstra(graph);
		this.euclidianDistanceSPGraph = new MatDijkstra(graph);
		euclidianDistanceSPGraph.execute();
	}

	public NetworkTopology(String filenameNodes, String filenameEdges) throws Exception {
		this.filenameNodes = filenameNodes;
		this.filenameEdges = filenameEdges;
		this.bgr = new NetGraph(filenameNodes, filenameEdges);
		this.graph = bgr.createGraph();
		this.shortestPathGraph = new MatDijkstra(graph);
		this.euclidianDistanceSPGraph = new MatDijkstra(graph);
		euclidianDistanceSPGraph.execute(); // calculates shortest paths taking real distances into
						// account; used in some heuristics
	}

	public NetworkTopology(NetGraph brg) {
		this.filenameNodes = null;
		this.filenameEdges = null;
		this.bgr = brg;
		this.graph = bgr.createGraph();
		this.shortestPathGraph = new MatDijkstra(graph);
		this.euclidianDistanceSPGraph = new MatDijkstra(graph);
		euclidianDistanceSPGraph.execute(); // calculates shortest paths taking real distances into
						// account; used in some heuristics
	}

	public NetworkTopology() {
		this.bgr = new NetGraph();
		this.graph = bgr.createGraph();
		this.shortestPathGraph = new MatDijkstra(graph);
		this.euclidianDistanceSPGraph = new MatDijkstra(graph);
		euclidianDistanceSPGraph.execute(); // calculates shortest paths taking real distances into
						// account; used in some heuristics
	}

	public NetworkTopology(NetNode[] nodes, NetEdge[] edges) {
		this.bgr = new NetGraph(nodes, edges);
		this.graph = bgr.createGraph();
		this.shortestPathGraph = new MatDijkstra(graph);
		this.euclidianDistanceSPGraph = new MatDijkstra(graph);
		euclidianDistanceSPGraph.execute(); // calculates shortest paths taking real distances into
						// account; used in some heuristics
	}

	/*
	 * apply weigths to links Links maybe administratively UP or DOWN.
	 */
	public void applyWeights(int[] weights) {
		int w = 0;
		for (int i = 0; i < graph.getDimension(); i++)
			for (int j = 0; j < graph.getDimension(); j++)
				if (!graph.getConnection(i, j).equals(Graph.Status.NOCONNECTION)) {
					double d = 0;
					d = weights[w];
					graph.setWeight(i, j, d);
					w++;
				}
		this.shortestPathGraph.execute();
	}

	public void applyWeights(OSPFWeights weights) throws DimensionErrorException {
		if (weights.getDimension() != graph.getDimension()) {
			throw new DimensionErrorException();
		}
		for (int i = 0; i < graph.getDimension(); i++)
			for (int j = 0; j < graph.getDimension(); j++)
				if (!graph.getConnection(i, j).equals(Graph.Status.NOCONNECTION)) {
					graph.setWeight(i, j, weights.getWeight(i, j));
				}
		this.shortestPathGraph.execute();
	}

	public double[][] getWeights() {
		return this.graph.getWeights();
	}

	// used for populating the table view
	public Double[][] getBandwidthsObj() {
		int dimension = this.getDimension();
		double[][] bandwidths = this.bgr.getBandwidths();

		Double[][] aux = new Double[dimension][dimension];

		for (int s = 0; s < dimension; s++)
			for (int t = 0; t < dimension; t++) {
				Double d = MathUtils.truncate2(bandwidths[s][t]);

				aux[s][t] = d;
			}
		return aux;
	}

	public NetGraph getNetGraph() {
		return this.bgr;
	}

	// used for populating the table view
	public Double[][] getDelaysObj() {
		int dimension = this.getDimension();
		double[][] delays = this.bgr.getDelays();

		Double[][] aux = new Double[dimension][dimension];

		for (int s = 0; s < dimension; s++)
			for (int t = 0; t < dimension; t++) {
				Double d = MathUtils.truncate2(delays[s][t]);

				aux[s][t] = d;
			}
		return aux;
	}

	// returns number of nodes
	public int getDimension() {
		return this.graph.getDimension();
	}

	public MatDijkstra getEuclidianDistanceSPGraph() {
		return this.euclidianDistanceSPGraph;
	}

	public MatDijkstra getShortestPathGraph() {
		return this.shortestPathGraph;
	}
	
	public MatDijkstra getShortestPathGraph(boolean SSP) {
		MatDijkstra g=new MatDijkstra(graph,SSP);
		g.execute();
		return g;
	}

	public String getFilenameEdges() {
		return filenameEdges;
	}

	public String getFilenameNodes() {
		return filenameNodes;
	}

	public CapWGraph getGraph() {
		return this.graph;
	}

	public int getNumberEdges() {
		return this.graph.countEdges();
	}
	
	public int getNumberUpEdges() {
		return this.graph.countUpEdges();
	}

	public void setFilenameEdges(String filenameEdges) {
		this.filenameEdges = filenameEdges;
	}

	public void setFilenameNodes(String filenameNodes) {
		this.filenameNodes = filenameNodes;
	}

	// changes graph weights to 1.
	// used for random demands/dr generation.
	// replaces current weights
	public void setUnitWeights() {
		for (int i = 0; i < this.getGraph().getDimension(); i++)
			for (int j = 0; j < this.getGraph().getDimension(); j++)
				if (!this.getGraph().getConnection(i, j).equals(Graph.Status.NOCONNECTION)) {
					this.getGraph().setWeight(i, j, 1);
				}
	}

	/** Compute shortest distances and keeps in object of class Dijkstra */
	public void shortestDistances() {
		shortestPathGraph.execute();
	}

	public double sumEndtoEndDelays() {
		WGraph delay_gr = bgr.createDelayGraph();
		MatDijkstra del = new MatDijkstra(delay_gr);
		del.execute();
		SPFElement[][] preds = del.getSolPreds();
		double[][] dists = del.getShortestPathDistances();

		double sumEndtoEndDelays = 0.0;
		for (int s = 0; s < preds.length; s++)
			for (int t = 0; t < preds.length; t++)
				if (s != t && preds[s][t] != null) // predecessors
					sumEndtoEndDelays += dists[s][t];

		return sumEndtoEndDelays;
	}

	public NetworkTopology copy() {
		NetGraph g = this.bgr.copy();
		return new NetworkTopology(g);
	}

	public void updateGraph() {
		this.graph = bgr.createGraph();
		this.shortestPathGraph = new MatDijkstra(graph);
		this.euclidianDistanceSPGraph = new MatDijkstra(graph);
		euclidianDistanceSPGraph.execute();
	}

	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void fireTopologyChanged(TopologyEvent evt) {
		Iterator<ITopologyChangeListener> it=listeners.iterator();
		while(it.hasNext()){
			ITopologyChangeListener listener= it.next();
			listener.updateTopology(evt);
		}
	}

	@Override
	public void updateTopology(TopologyEvent evt) {
		// TODO Auto-generated method stub
		
	}

	public void addTopologyChangeListener(ITopologyChangeListener topologyListener) {
		listeners.add(topologyListener);
	}

	public void setEdgeStatus(NetEdge e, Status status) {
		this.graph.setConnection(e.getFrom(),e.getTo(),status);
		this.graph.setConnection(e.getTo(),e.getFrom(),status);
		e.setUP(status.equals(Status.UP));
		
		int type=0;
		if(status.equals(Status.UP))
			type=TopologyEvent.EDGE_UP;
		else	
			type=TopologyEvent.EDGE_DOWN;
		
		this.fireTopologyChanged(new TopologyEvent(this,type,e));
	}

	public void applyNodeTypeConfiguration(NodeTypeConfiguration configuration) throws DimensionErrorException {
		NodeType[] conf =configuration.getConfiguration();
		this.getNetGraph().applyNodeTypeConfiguration(conf);
		
	}

}
