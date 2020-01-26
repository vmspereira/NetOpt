/* Copyright 2012-2017,
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

package pt.uminho.algoritmi.netopt.ospf.simulation.sr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

import pt.uminho.algoritmi.netopt.ospf.graph.Graph;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.OSPFWeights;
import pt.uminho.algoritmi.netopt.ospf.simulation.exception.DimensionErrorException;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetNode;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.Segment.SegmentType;
import pt.uminho.algoritmi.netopt.ospf.utils.IntPair;

/**
 * Given a network topology and an IGP weights configuration translates a given
 * path identified as nodes into a SR label path.
 */

public class SRPathTranslator {

	NetworkTopology topo;
	OSPFWeights weights;

	public SRPathTranslator(NetworkTopology topology) {
		this.topo = topology;
	}

	public SRPathTranslator(NetworkTopology topology, OSPFWeights weights) {
		this.topo = topology;
		this.weights = weights;
		try {
			topo.applyWeights(weights);
		} catch (DimensionErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setWeights(OSPFWeights weights) {
		this.weights = weights;
		try {
			topo.applyWeights(weights);
		} catch (DimensionErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Converts a ordered list of nodes representing a path into a SR label
	 * switching path
	 * 
	 * @param SR
	 *            path as list of nodes
	 * @return SR label path
	 * @throws DimensionErrorException
	 */
	public LabelPath translate(List<NetNode> path) throws DimensionErrorException {
		NetNode src = path.get(0);
		NetNode dst = path.get(path.size() - 1);
		LabelPath lsp = new LabelPath(src, dst);
		topo.applyWeights(weights);
		int n=path.size();
		
		int t = n-1;
		int previous = t-1;
		
		Graph spGraph = topo.getShortestPathGraph().getArcsShortestPath(path.get(t).getNodeId());
		
		while (previous >=0) {
			if(!spGraph.getConnection(path.get(previous).getNodeId(), path.get(previous+1).getNodeId()).equals(Graph.Status.UP) || previous == 0) 
			{
				if (t - previous > 1) {
					IntPair p = new IntPair(path.get(previous+1).getNodeId(), path.get(t).getNodeId());
					Segment segment = new Segment(p.getYString(), SegmentType.NODE);
					segment.setSrcNodeId(path.get(previous+1).getNodeId());
					segment.setDstNodeId(p.getY());
					lsp.addSegment(segment);
					t = previous+1;

				}else{
					IntPair p = new IntPair(path.get(previous).getNodeId(), path.get(t).getNodeId());
					Segment segment = new Segment(p.toString(), SegmentType.ADJ);
					segment.setSrcNodeId(path.get(previous).getNodeId());
					segment.setDstNodeId(path.get(t).getNodeId());
					lsp.addSegment(segment);
					t = previous;
				}
				
				spGraph = topo.getShortestPathGraph().getArcsShortestPath(path.get(t).getNodeId());
				previous=t-1;
				
			}
			else
			   previous--;
		}
		Collections.reverse(lsp.getLabels());
		return lsp;
	}

	
	/*
	 * if a label path has more than maxLabel,
	 * it is substituted by SP
	 */
	public LabelPath translate(List<NetNode> path, int maxLabel) throws DimensionErrorException {
		LabelPath l;
		LabelPath lp=this.translate(path);
		if(lp.getLabelStackLength() >maxLabel){
		   l = new LabelPath(lp.getSource(),lp.getDestination());
		   Segment segment = new Segment(""+lp.getDestination().getNodeId(), SegmentType.NODE);
		   segment.setSrcNodeId(lp.getSource().getNodeId());
		   segment.setDstNodeId(lp.getDestination().getNodeId());
		   l.addSegment(segment);
		}else{
			l=lp;
		}
		return l;
	}
	
	
	
	public List<List<NetNode>> translate(LabelPath path) throws DimensionErrorException {
		ArrayList<List<NetNode>> list = new ArrayList<List<NetNode>>();
		ArrayList<NetNode> l0 = new ArrayList<NetNode>();
		l0.add(path.getSource());
		list.add(l0);
		Iterator<Segment> it = path.getIterator();
		while (it.hasNext()) {
			Segment s = it.next();
			if (s.getType().equals(SegmentType.ADJ)) {
				for (List<NetNode> l : list)
					l.add(topo.getNetGraph().getNodeByID(s.getDstNodeId()));
			} else {
				ArrayList<List<NetNode>> temp = new ArrayList<List<NetNode>>();
				List<Vector<Integer>> l = topo.getShortestPathGraph().getAllPaths(s.getSrcNodeId(), s.getDstNodeId());
				for (Vector<Integer> p : l) {
					List<NetNode> ln = p.stream().map(r -> topo.getNetGraph().getNodeByID(r))
							.collect(Collectors.toList());
					for (List<NetNode> a : list) {
						if (a.get(a.size() - 1).equals(ln.get(0))) {
							List<NetNode> b = new ArrayList<NetNode>();
							b.addAll(a);
							b.remove(b.size() - 1);
							b.addAll(ln);
							temp.add(b);
						}
					}
				}
				list = temp;
			}
		}
		return list;
	}

}
