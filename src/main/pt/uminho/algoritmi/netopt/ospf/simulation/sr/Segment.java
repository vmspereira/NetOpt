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

public class Segment {

	
	private String SID;
	private SegmentType type;
	private int dstNodeId;
	private int srcNodeId;

	
	/**
	 * Although SR routing considers a wider set of segment's types
	 * we only consider Adjacency Segments and Node Segments.
	 * Segments IDs (SIDs) are strings that have global significance.
	 * 
	 * Node Segments only have a destination node ID, an identifies a topology node.
	 * Adjacency Segments have both destination and source nodes, used to identity a link. 
	 */
	
	public static enum SegmentType {

		NODE("Node-SID"), ADJ("Adj-SID");

		private final String name;
		
		SegmentType(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}
	}

	
	
	public Segment(String SID, SegmentType type){
		this.SID=SID;
		this.type=type;
	}
	
	
	public SegmentType getType() {
		return type;
	}

	public void setType(SegmentType type) {
		this.type = type;
	}

	public String getSID() {
		return SID;
	}

	public void setSID(String sID) {
		SID = sID;
	}


	public int getSrcNodeId() {
		return srcNodeId;
	}


	public void setSrcNodeId(int srcNodeId) {
		this.srcNodeId = srcNodeId;
	}


	public int getDstNodeId() {
		return dstNodeId;
	}


	public void setDstNodeId(int dstNodeId) {
		this.dstNodeId = dstNodeId;
	}

	public String toString(){
		if(this.type.equals(SegmentType.NODE)){
			return this.type.toString()+" "+this.dstNodeId;
		}
		else{	
			return this.type.toString()+" "+this.srcNodeId+"-"+this.dstNodeId;
		}
	}
}
