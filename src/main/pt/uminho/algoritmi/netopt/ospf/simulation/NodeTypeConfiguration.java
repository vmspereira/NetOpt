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

import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetNode.NodeType;

public class NodeTypeConfiguration {
	
	NodeType[] configuration;
	
	
	public NodeTypeConfiguration(int numberOfNodes){
		this(numberOfNodes,NodeType.LEGACY);
	}
	
	public NodeTypeConfiguration(int numberOfNodes,NodeType type){
		configuration = new NodeType[numberOfNodes];
		for(int i=0;i<numberOfNodes;i++)
			configuration[i]=type;
	}
	
	public NodeTypeConfiguration(int[] conf){
		configuration = new NodeType[conf.length];
		for(int i=0;i<conf.length;i++){
			if(conf[i]==0)
				configuration[i]=NodeType.LEGACY;
			else
				configuration[i]=NodeType.SDN_SR;
		}
	} 
	
	
	public NodeType getNodeType(int i){
		return this.configuration[i];
	}
	
	public void setNodeType(int i, NodeType type){
		this.configuration[i]=type;
	}

	public NodeType[] getConfiguration() {
		return this.configuration;
	}

}
