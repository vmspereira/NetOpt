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
package pt.uminho.algoritmi.netopt.ospf.utils.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.io.gml.GMLReader;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReader;

import pt.uminho.algoritmi.netopt.SystemConf;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetEdge;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetGraph;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetNode;

public class GraphReader {

	public static NetGraph readGraphML(InputStream inputStream) throws IOException {
		TinkerGraph graph = new TinkerGraph();
		GraphMLReader reader = new GraphMLReader(graph);
		reader.inputGraph(inputStream);
		NetGraph ngraph = parseTinkerGraph(graph);
		return ngraph;
	}

	public static NetGraph readGML(InputStream inputStream) throws IOException {
		TinkerGraph graph = new TinkerGraph();
		GMLReader reader = new GMLReader(graph);
		reader.inputGraph(inputStream);
		NetGraph ngraph = parseTinkerGraph(graph);
		return ngraph;
	}

	private static NetGraph parseTinkerGraph(TinkerGraph graph) {

		Map<Object, Integer> objToIndex = new HashMap<Object, Integer>();
		ArrayList<NetNode> nodes = new ArrayList<NetNode>();
		ArrayList<NetEdge> edges = new ArrayList<NetEdge>();

		double defaultBW = SystemConf.getPropertyDouble("topology.defaultBW", 10.0);

		Iterator<Vertex> itv = graph.getVertices().iterator();

		int current = 0;
		while (itv.hasNext()) {
			Vertex vertex = itv.next();
			NetNode node = new NetNode();
			node.setNodeId(current);
			objToIndex.put(vertex.getId(), current);

			try {
				Object o = vertex.getProperty(Tokens.LABEL);
				node.setLabel(o.toString());
			} catch (Exception e) {
			}
			
			if (vertex.getPropertyKeys().contains(Tokens.GRPH)) {
				HashMap h = vertex.getProperty(Tokens.GRPH);
				double d = Double.parseDouble(h.get(Tokens.X).toString());
				node.setXpos(d);
				d = Double.parseDouble(h.get(Tokens.Y).toString());
				node.setYpos(d);
			} else {
				try {
					Object o = vertex.getProperty(Tokens.LON);
					double d = Double.parseDouble(o.toString());
					node.setXpos(d);
				} catch (Exception e) {
				}
				try {
					Object o = vertex.getProperty(Tokens.X);
					double d = Double.parseDouble(o.toString());
					node.setXpos(d);
				} catch (Exception e) {
				}
				// y
				try {
					Object o = vertex.getProperty(Tokens.LAT);
					double d = Double.parseDouble(o.toString());
					node.setYpos(d);
				} catch (Exception e) {
				}
				try {
					Object o = vertex.getProperty(Tokens.Y);
					double d = Double.parseDouble(o.toString());
					node.setYpos(d);
				} catch (Exception e) {
				}
			}
		
			
			
			nodes.add(current, node);
			current++;
		}

		double[][] c = new double[nodes.size()][nodes.size()];

		Iterator<Edge> ite = graph.getEdges().iterator();
		current = 0;
		while (ite.hasNext()) {
			Edge e = ite.next();
			Object in = e.getVertex(Direction.IN).getId();
			Object out = e.getVertex(Direction.OUT).getId();
			int from = objToIndex.get(in);
			int to = objToIndex.get(out);
			double bw = defaultBW;
			try {
				Object o = e.getProperty(Tokens.CAP);
				double d = Double.parseDouble(o.toString());
				bw = d;
			} catch (Exception exp) {
			}
			c[from][to] += bw;
			c[to][from] = +bw;
			current++;
		}
		current = 0;
		for (int i = 0; i < nodes.size() - 1; i++) {
			for (int j = i + 1; j < nodes.size(); j++) {
				if (c[i][j] > 0.0) {
					NetEdge edge = new NetEdge(i, j);
					edge.setBandwidth(c[i][j]);
					nodes.get(i).setIndegree(nodes.get(i).getIndegree() + 1);
					nodes.get(i).setOutdegree(nodes.get(i).getOutdegree() + 1);
					nodes.get(j).setIndegree(nodes.get(j).getIndegree() + 1);
					nodes.get(j).setOutdegree(nodes.get(j).getOutdegree() + 1);
					edge.setEdgeId(current);
					edges.add(current, edge);
					current++;
				}
			}
		}
		NetNode[] anodes = nodes.toArray(new NetNode[nodes.size()]);
		NetEdge[] aedges = edges.toArray(new NetEdge[edges.size()]);
		NetGraph ngraph = new NetGraph(anodes, aedges);
		return ngraph;
	}

	private static class Tokens {

		public static final String LAT = "Latitude";
		public static final String LON = "Longitude";
		public static final String X = "x";
		public static final String Y = "y";
		public static final String CAP = "Capacity";
		public static final String LABEL = "label";
		public static final String GRPH = "graphics";
	}
}
