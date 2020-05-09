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



import java.util.Comparator;

import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetEdge;
import pt.uminho.algoritmi.netopt.ospf.simulation.simulators.ISimulator;


public class HigherLoadEdgeSelector extends DefaultEdgeSelector implements Comparator<NetEdge>{

	
	
	
	public HigherLoadEdgeSelector(ISimulator simul) {
		super(simul);
	}

	
	
	
	
	@Override
	public int compare(NetEdge e1, NetEdge e2) {
		double d1 = simul.getLoads().getLoads(e1.getFrom(), e1.getTo())+simul.getLoads().getLoads(e1.getTo(), e1.getFrom());
		double d2 = simul.getLoads().getLoads(e2.getFrom(), e2.getTo())+simul.getLoads().getLoads(e2.getTo(), e2.getFrom());
		return Double.compare(d1, d2);
	}
}
