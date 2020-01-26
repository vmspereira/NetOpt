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
package pt.uminho.algoritmi.netopt.ospf.simulation.edgeSelectors;

import java.util.ArrayList;
import java.util.Iterator;

import pt.uminho.algoritmi.netopt.ospf.simulation.OSPFWeights;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetEdge;
import pt.uminho.algoritmi.netopt.ospf.simulation.simulators.ISimulator;


/**
 * 
 * @author Vitor
 *
 *Edge Selector by given edge ID
 *
 */


public class IDEdgeSelector implements INetEdgeSelector
	{

	ISimulator simul;
	OSPFWeights weights;
	int[] edges;

	
	public IDEdgeSelector(ISimulator simul, int[] edgesId) {
		this.simul = simul;
		weights=null;
		edges=edgesId;
	}

	
	@Override
	public Iterator<NetEdge> getIterator() {
		ArrayList<NetEdge> list = new ArrayList<NetEdge>();
		for(int i=0;i<edges.length;i++)
			list.add(simul.getTopology().getNetGraph().getEdgeByID(edges[i]));
		return list.iterator();
	}


	@Override
	public void setWeights(OSPFWeights weights) {
		this.weights=weights;
	}

}
