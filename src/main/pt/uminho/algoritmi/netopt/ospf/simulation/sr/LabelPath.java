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
import java.util.Iterator;
import java.util.List;

import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetEdge;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetNode;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.Segment.SegmentType;


/**
 * 
 * A label Path for a source/destination pair nodes is a list of 
 * segments which identify the path to be used to forward traffic.
 *
 */

public class LabelPath {

	private NetNode source;
	private NetNode destination;
	private double fraction;
	private ArrayList<Segment> labels;
	
	public LabelPath(NetNode source, NetNode destination){
		this.setSource(source);
		this.setDestination(destination);
		setLabels(new ArrayList<Segment>());
		this.fraction =1.0;
	}
	
	
	public void addSegment(Segment segment){
		 getLabels().add(segment);
	}


	public NetNode getSource() {
		return source;
	}


	public void setSource(NetNode source) {
		this.source = source;
	}


	public NetNode getDestination() {
		return destination;
	}


	public void setDestination(NetNode destination) {
		this.destination = destination;
	}

	public ArrayList<Segment> getLabels() {
		return labels;
	}


	public void setLabels(ArrayList<Segment> labels) {
		this.labels = labels;
	}
	
	
	public int getLabelStackLength(){
		return labels.size();
	}


	public double getFraction() {
		return fraction;
	}


	public void setFraction(double fraction) {
		this.fraction = fraction;
	}


	public String getLabelStackString() {
		StringBuffer sb =new StringBuffer();
		for(Segment s:labels)
			if(s.getType()== SegmentType.NODE)
				sb.append("N["+s.getSID()+"] ");
			else
				sb.append("A["+s.getSID()+"] ");
		return sb.toString();
	}
	
	public boolean isShortestPath(){
		boolean b=true;
		Iterator<Segment> it=labels.iterator();
		while(it.hasNext() && b){
			if(!it.next().getType().equals(SegmentType.NODE))
				b=false;
		}
		return b;
	}
	
	
	public List<Segment> getAdjacentSegment(){
		ArrayList<Segment> list =new ArrayList<Segment>();
		for(Segment s:labels){
			if(s.getType()==SegmentType.ADJ)
				list.add(s);
		}
		return list;
	}


	public boolean contain(List<NetEdge> list) {
		for(Segment s:labels)
			for(NetEdge e:list)
				if((s.getSrcNodeId()==e.getFrom() && s.getDstNodeId()==e.getTo()) ||
				   (s.getSrcNodeId()==e.getTo() && s.getDstNodeId()==e.getFrom()))
					return true;
		return false;
	}
	
	
	
	public boolean contain(NetEdge e) {
		for(Segment s:labels)
			if((s.getSrcNodeId()==e.getFrom() && s.getDstNodeId()==e.getTo()) ||
				   (s.getSrcNodeId()==e.getTo() && s.getDstNodeId()==e.getFrom()))
					return true;
		return false;
	}
	
	
	public Iterator<Segment> getIterator(){
		Iterator<Segment> it=labels.iterator();
		return it;
	}
	
	
	public String toString(){
		StringBuffer bf= new StringBuffer();
		bf.append("[");
		Iterator<Segment> it=labels.iterator();
		while(it.hasNext())
			bf.append(it.next().toString()+" ");
		bf.append("]");	
		return bf.toString();
	}
	
}
