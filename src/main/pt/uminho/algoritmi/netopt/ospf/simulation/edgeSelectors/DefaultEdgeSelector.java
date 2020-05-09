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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import pt.uminho.algoritmi.netopt.ospf.simulation.OSPFWeights;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetEdge;
import pt.uminho.algoritmi.netopt.ospf.simulation.simulators.ISimulator;

/**
 * 
 * @author Vitor
 *
 *Edge selector by ID 
 *
 */

public class DefaultEdgeSelector implements INetEdgeSelector,
		Comparator<NetEdge> {

	protected ISimulator simul;
	protected OSPFWeights weights;

	
	public DefaultEdgeSelector(ISimulator simul) {
		this.simul = simul;
		weights=null;
	}

	
	@Override
	public Iterator<NetEdge> getIterator() {
		ArrayList<NetEdge> list = new ArrayList<NetEdge>(
				Arrays.asList(simul.getTopology().getNetGraph().getEdges()));
		Collections.sort(list, this);
		Collections.reverse(list);
		return list.iterator();
	}

	
	@Override
	/**
	 * Default comparison by edge ID
	 */
	public int compare(NetEdge e1, NetEdge e2) {
		return Integer.compare(e1.getEdgeId(), e2.getEdgeId());
	}


	@Override
	public void setWeights(OSPFWeights weights) {
		this.weights=weights;
	}

	
	protected double max(double d1, double d2){
		if(d1>d2)
			return d1;
		else return d2;
	}
}
