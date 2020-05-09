/*******************************************************************************
 * Copyright 2012-2019,
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

package pt.uminho.algoritmi.netopt.cplex.utils;

public class Arc {

	private int fromNode;
	private int toNode;
	private double capacity;
	private int index;
	
	public Arc(int from, int to, double capacity){
		this.setFromNode(from);
		this.setToNode(to);
		this.setCapacity(capacity);
	}
	
	public Arc(int index,int from, int to, double capacity){
		this.index = index;
		this.setFromNode(from);
		this.setToNode(to);
		this.setCapacity(capacity);
	}

	public int getIndex(){
		return index;
	}
	
	public void setIndex(int index){
		this.index = index;
	}
	
	public int getFromNode() {
		return fromNode;
	}

	void setFromNode(int fromNode) {
		this.fromNode = fromNode;
	}

	public int getToNode() {
		return toNode;
	}

	void setToNode(int toNode) {
		this.toNode = toNode;
	}

	public double getCapacity() {
		return capacity;
	}

	void setCapacity(double capacity) {
		this.capacity = capacity;
	}
	
	
	public boolean equals(Object o){
		if(o instanceof Arc){
			Arc a = (Arc)o;
			if(a.getFromNode()==this.fromNode && a.getToNode()==this.toNode)
				return true;
			else return false;
		}
		else return false;
	}
	
}
