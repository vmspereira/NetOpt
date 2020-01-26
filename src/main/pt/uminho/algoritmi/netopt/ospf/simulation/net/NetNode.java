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

import java.io.Serializable;
import java.util.Properties;
import java.util.StringTokenizer;

@SuppressWarnings("serial")
public class NetNode implements Serializable {

	private int nodeId;
	private double xpos;
	private double ypos;
	protected NodeType nodeType;
	protected int indegree, outdegree;
	protected String label;
	protected Properties properties;

	public enum NodeType{
		LEGACY,SDN_SR,DISABLED;
	}
	
	
	
	public NetNode(String line) {
		StringTokenizer st = new StringTokenizer(line, "\t");
		this.setNodeId(Integer.valueOf(st.nextToken()).intValue());
		this.setXpos(Double.valueOf(st.nextToken()).doubleValue());
		this.setYpos(Double.valueOf(st.nextToken()).doubleValue());
		this.nodeType = NodeType.SDN_SR;
		this.indegree = Integer.valueOf(st.nextToken()).intValue();
		this.outdegree = Integer.valueOf(st.nextToken()).intValue();
		this.label="";
		this.properties = new Properties();
		properties.setProperty(NodeProperty.ASID, st.nextToken()); 
		properties.setProperty(NodeProperty.TYPE,st.nextToken());
		
	}

	public NetNode() {
		this.setNodeId(-1);
		this.setXpos(0);
		this.setYpos(0);
		this.nodeType = NodeType.SDN_SR;
		this.indegree = 0;
		this.outdegree = 0;
		this.label="";
		this.properties = new NetProperties();
		
		
	}

	public NetNode(int nodeId, double xpos, double ypos, int indegree, int outdegree) {
		this.setNodeId(nodeId);
		this.setXpos(xpos);
		this.setYpos(ypos);
		this.nodeType = NodeType.SDN_SR;
		this.indegree = indegree;
		this.outdegree = outdegree;
		this.label="";
		this.properties = new NetProperties();
	}

	public int getNodeId() {
		return nodeId;
	}

	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}

	public double getXpos() {
		return xpos;
	}

	public void setXpos(double xpos) {
		this.xpos = xpos;
	}

	public double getYpos() {
		return ypos;
	}

	public void setYpos(double ypos) {
		this.ypos = ypos;
	}

	public int getIndegree() {
		return indegree;
	}

	public void setIndegree(int indegree) {
		this.indegree = indegree;
	}

	public int getOutdegree() {
		return outdegree;
	}

	public void setOutdegree(int outdegree) {
		this.outdegree = outdegree;
	}

	
	public String print() {
		return "ID: " + getNodeId() + " X: " + getXpos() + " Y: " + getYpos() + "Deg: " + indegree + " " + outdegree;
	}

	@Override
	public String toString() {
		return "" + getNodeId();
	}

	public boolean equals(NetNode node) {
		if (node.getNodeId() == this.getNodeId())
			return true;
		else
			return false;
	}

	public Properties getProperties() {
		return this.properties;
	}

	public String getProperty(String key) {
		return this.properties.getProperty(key);
	}
	
	public NetNode copy() {
		NetNode node = new NetNode(this.getNodeId(), this.getXpos(), this.getYpos(), this.getIndegree(),
				this.getOutdegree());
		node.setNodeType(this.nodeType);
		node.setLabel(this.label);
		return node;
	}

	public void setProperty(String label, String value) {
		this.properties.setProperty(label, value);
	}
	
	public NodeType getNodeType(){
		return this.nodeType;
	}
	
	public void setNodeType(NodeType type){
		this.nodeType = type;
	}
	
	public void setLabel(String s){
		this.label =s;
	}
	
	public String getLabel(){
		return this.label;
	}

}
