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


import java.util.HashMap;
import java.util.Map;

import pt.uminho.algoritmi.netopt.ospf.simulation.exception.DimensionErrorException;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.Flow.FlowType;



/***
 * 
 * @author Vitor
 *
 */
public class FlowManager {
	
	
	private Map<Integer,Flow> flows;
		
	public FlowManager() throws DimensionErrorException{
		this.flows =new HashMap<Integer,Flow>();		
	}
	
	
	
	public void addFlow(int source, int destination,FlowType type){
		Flow flow=new Flow(source,destination,type);
		this.addFlow(flow);
	}
	
	public void addFlow(Flow flow){
		this.addFlow(flow,true);
	}
	
	public void addFlow(Flow flow, boolean maxbw){
		Integer k=flow.hashCode();
		this.flows.put(k,flow);
		
	}
	
	
	public void removeFlow(Flow flow){	
		flows.remove(flow.hashCode());
	}
}
