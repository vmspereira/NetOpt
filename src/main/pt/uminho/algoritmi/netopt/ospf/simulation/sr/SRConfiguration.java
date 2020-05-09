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
package pt.uminho.algoritmi.netopt.ospf.simulation.sr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IntSummaryStatistics;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import pt.uminho.algoritmi.netopt.ospf.graph.Graph.Status;
import pt.uminho.algoritmi.netopt.ospf.simulation.Demands;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetEdge;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetNode;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.Segment.SegmentType;

public class SRConfiguration {

	
	public enum SRConfigurationType{
		NORMAL,LINK_FAILURE;
	}
	
	
	
	private SRConfigurationType type;
	/**
	 * Map source nodes to label paths configurations
	 */
	protected Map<NetNode, SRNodeConfiguration> configuration;
	/**
	 * If LINK_FAILURE refers the failing link
	 */
	protected ArrayList<NetEdge> failedLinks;

	public SRConfiguration() {
		this.configuration = new HashMap<NetNode, SRNodeConfiguration>();
		this.type=SRConfigurationType.NORMAL;
	}

	/*
	 * Add a new label path
	 * 
	 * check for consistency???
	 */
	public void addLabelPath(NetNode source, NetNode destination, LabelPath lp) {
		SRNodeConfiguration conf = configuration.get(source);
		if (conf == null) {
			conf = new SRNodeConfiguration(source);
			configuration.put(source, conf);
		}
		conf.addLabelPath(destination, lp);
	}

	/*
	 * Add a new label path
	 */
	public void addLabelPath(LabelPath lp) {
		this.addLabelPath(lp.getSource(), lp.getDestination(), lp);
	}

	/**
	 * @return the total number of configured label paths
	 */
	public int getSize() {
		int sum = 0;
		Collection<SRNodeConfiguration> c = configuration.values();
		for (SRNodeConfiguration s : c)
			sum += s.getSize();
		return sum;
	}

	public Collection<SRNodeConfiguration> getNodesConfigurations() {
		return configuration.values();
	}

	
	
	/** 
	 * Traffic to be routed along SP.
	 * 
	 * If the topology possesses failing links traffic that traveled over them is rerouted using SP from edges (edgeReroute = true) or
	 * solely from failing links adjacent nodes (edgeReroute=false)
	 * 
	 * @param demands
	 * @param topology
	 * @param edgeReroute
	 * 
	 * @return Demands
	 */
	public Demands getShortestPathDemands(Demands demands, NetworkTopology topology, boolean edgeReroute) {
		Demands dem = new Demands(demands.getDimension());

		for (SRNodeConfiguration nc : configuration.values()) {
			Collection<ArrayList<LabelPath>> u = nc.getConfiguration().values();
			for (ArrayList<LabelPath> a : u) {
				for (LabelPath b : a) {
					NetNode src = b.getSource();
					NetNode dst = b.getDestination();
					// Shortestpath Edge-to-Edge traffic (Label stack [Node-SID destination])
			        // is rerouted using new computed shortest paths  
					if (b.isShortestPath()) {
						double d = demands.getDemands(src.getNodeId(), dst.getNodeId()) * b.getFraction();
						Iterator<Segment> it = b.getLabels().iterator();
						while (it.hasNext()) {
							Segment s = it.next();
							dem.add(s.getSrcNodeId(), s.getDstNodeId(), d);
						}
					} else {
						
						double d = demands.getDemands(src.getNodeId(), dst.getNodeId()) * b.getFraction();
						List<NetEdge> list = topology.getNetGraph().getEdges(Status.DOWN);
						// reroute traffic by SP if path includes a failed link
						// and traffic is to be rerouted from edge-to-edge
						if (b.contain(list) && edgeReroute) {
							dem.add(src.getNodeId(), dst.getNodeId(), d);
						} else {
							Iterator<Segment> it=b.getLabels().iterator();
							boolean stop = false;
							while(it.hasNext() && !stop ) {
								Segment s= it.next();
								if (s.getType().equals(SegmentType.NODE)) {
									dem.add(s.getSrcNodeId(), s.getDstNodeId(), d);
								}
								// Traffic traveling on failing links is
								// rerouted via SP
								else if (s.getType().equals(SegmentType.ADJ)) {
									NetEdge e = topology.getNetGraph().getEdge(s.getSrcNodeId(), s.getDstNodeId());
									if (!e.isUP()) 
									{
										dem.add(s.getSrcNodeId(), b.getDestination().getNodeId(), d);
										stop=true;
									}
								}

							}
						}
						
					}
				}
			}
		}
		return dem;
	}

	public ArrayList<NetEdge> getFailedLinks() {
		return failedLinks;
	}

	public void addFailedLink(NetEdge failedLink) {
		this.failedLinks.add(failedLink);
	}

	public SRConfigurationType getType() {
		return type;
	}

	public void setType(SRConfigurationType type) {
		this.type = type;
	}
	
	// statistics
	
	public Properties statistics(){
		Properties prop =new Properties();
		ArrayList<Integer> pathLenghts=new ArrayList<Integer>() ;
		Collection<SRNodeConfiguration> c = configuration.values();
		
		for (SRNodeConfiguration s : c){
				pathLenghts.addAll(s.getPahtLengths());
		}
		
		IntSummaryStatistics stats = pathLenghts.stream().mapToInt(Integer::intValue).summaryStatistics();
		double rawSum=pathLenghts.stream().mapToDouble((x) -> Math.pow(x.doubleValue() - stats.getAverage(),2.0)).sum();
		double std=Math.sqrt(rawSum / (stats.getCount() - 1));
		long n3 = pathLenghts.stream().filter(l->l>3).count();
		prop.setProperty("size", ""+stats.getCount());
		prop.setProperty("mean", ""+stats.getAverage());
		prop.setProperty("min", ""+stats.getMin());
		prop.setProperty("max", ""+stats.getMax());
		prop.setProperty("std", ""+std);
		prop.setProperty("n3", ""+n3);
		return prop;
	}
	
	
	public String toString(){
		StringBuffer bf = new StringBuffer();
		Collection<NetNode> nn = configuration.keySet();
		for (NetNode n : nn){
			SRNodeConfiguration c=configuration.get(n);
				bf.append(c.toString());
		}
		return bf.toString();
	}
	
	/**
	 * Returns the list of SR paths from source to destination
	 * 
	 * @param source
	 * @param destination
	 * @return ArrayList of LabelPaths
	 */
	
	public ArrayList<LabelPath> getSRPaths(NetNode source, NetNode destination){
		ArrayList<LabelPath> list = new ArrayList<LabelPath>();
		SRNodeConfiguration conf = configuration.get(source);
		list = conf.getPathsToDestination(destination);
		return list;
	}

	// TODO:
	public SRConfiguration copy() {
		return null;
	}
	
	
	

}
