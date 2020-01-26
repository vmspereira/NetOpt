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
package pt.uminho.algoritmi.netopt.ospf.listener;

import java.util.EventObject;

public class TopologyEvent extends EventObject{

	public static final int ADD_EDGE = 1;
	public static final int ADD_NODE = 2;
	public static final int REMOVE_EDGE = 3;
	public static final int REMOVE_NODE = 4;
	public static final int EDGE_DOWN = 5;
	public static final int EDGE_UP = 6;
	
	private static final long serialVersionUID = 1L;
	
	private int type;
	private Object object;
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Object getObject(){
		return this.object;
	}
	

	/**
	 * 
	 * @param source
	 * @param type  TopologyEvent.REMOVE or TopologyEvent.MAXWEIGHT
	 * @param from  Edge Source node 
	 * @param to    Edge Destination node
	 */
	public TopologyEvent(Object source,int type, Object obj) {
		super(source);
		this.type=type;
		this.object=obj;
		
	}

	

	

}
