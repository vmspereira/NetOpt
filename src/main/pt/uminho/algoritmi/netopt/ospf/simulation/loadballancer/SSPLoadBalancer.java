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
package pt.uminho.algoritmi.netopt.ospf.simulation.loadballancer;

import java.util.Collections;
import java.util.List;

import pt.uminho.algoritmi.netopt.ospf.graph.Graph;

public class SSPLoadBalancer implements ILoadBalancer {

	
	    
	// g is the SP spanning tree for destination dst
	private Graph g; 
	private double[][] weights;
	
	
	public SSPLoadBalancer(Graph g, double[][] weights){
		this.g=g;
		this.weights=weights;
	}

	@Override
	/*
	 * @see ospf.simulation.loadballancer.ILoadBalancer#getSplitRatio(int, int, int, int)
	 *  
	 */
	public double getSplitRatio(int flowSrc, int flowDst, int currentNode, int nextNode) {
		if(g.getConnection(currentNode,nextNode).equals(Graph.Status.UP)){
			List<Integer> out=g.outNodes(currentNode);
			Collections.sort(out);
			int selectedHop= out.get(0);
			for(int i=1;i<out.size();i++)
				if(weights[currentNode][out.get(i)]<weights[currentNode][selectedHop])
					selectedHop=out.get(i);
			if(nextNode==selectedHop)
				return 1;
			else 
				return 0;
		}
		else 
			return 0;
	}
	

}
