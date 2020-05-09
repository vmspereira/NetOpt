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
import java.util.List;
import java.util.Map;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetNode;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetNode.NodeType;

/**
 * A SRNodeConfiguration for a source node.
 * Maps each destination node to a a list of label paths configurations  
 * 
 * 
 * @author vítor
 *
 */
public class SRNodeConfiguration {
	
	private Map<NetNode,ArrayList<LabelPath>> configuration;
	NetNode sourceNode;

	
	public SRNodeConfiguration(NetNode node){
		this.sourceNode=node;
		this.setConfiguration(new HashMap<NetNode,ArrayList<LabelPath>>());
	}
	
	public void addLabelPath(NetNode destination, LabelPath lp){
		ArrayList<LabelPath> l=getConfiguration().get(destination);
		if(l==null){
			l= new ArrayList<LabelPath>();
			getConfiguration().put(destination, l);
		}
		l.add(lp);
	}
	
	public ArrayList<LabelPath> getPathsToDestination(NetNode destination){
		return configuration.get(destination);
	}

	public Map<NetNode,ArrayList<LabelPath>> getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Map<NetNode,ArrayList<LabelPath>> configuration) {
		this.configuration = configuration;
	}
	
	public boolean isSplitValid(NetNode destination){
		double sum=0;
		try {
			ArrayList<LabelPath> l=configuration.get(destination);
			for(LabelPath path:l )
				sum+=path.getFraction();
		} catch (NullPointerException e) {}
		return sum==1;
	}
	
	
	public int getSize(){
		int sum=0;
		Collection<ArrayList<LabelPath>> c=configuration.values();
		for(ArrayList<LabelPath> l:c)
			sum+=l.size();
		return sum;
	}
	
	public NetNode getSourceNode(){
		return this.sourceNode;
	}
	
	
	public boolean isSourceNodeSREnabled(){
		return this.sourceNode.getNodeType().equals(NodeType.SDN_SR);
	}

	public List<Integer> getPahtLengths() {
		ArrayList<Integer> lenghts=new ArrayList<Integer>();
		Collection<ArrayList<LabelPath>> c=configuration.values();
		for(ArrayList<LabelPath> l:c)
			for(LabelPath p:l)
				lenghts.add(p.getLabelStackLength());
		return lenghts;
	}
	
	
	public String toString(){
		StringBuffer bf= new StringBuffer();
		Collection<NetNode> c=configuration.keySet();
		for(NetNode a :c){
			bf.append("[").append(a.toString());
			List<LabelPath> l= configuration.get(a);
			for(LabelPath p:l){
				bf.append(p.toString());
			}
			bf.append("]");
		}
		return bf.toString();
	}
}
