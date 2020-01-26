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
package pt.uminho.algoritmi.netopt.ospf.utils;

public class IntPair {
	final int x;
	final int y;
	double value;

	public IntPair(int x, int y) {
		this.x = x;
		this.y = y;
		this.value = 0.0;
	}
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
	
	public boolean equals(Object obj){
		if(!(obj instanceof IntPair))
			return false;
		else{
			IntPair p=(IntPair) obj;
			return (this.x==p.x && this.y==p.y);
		}
	}
	
	public String toString(){
		return x+"-"+y;
	}

	public String getXString() {
		return ""+x;
	}

	
	public void setValue(double d){
		this.value =d;
	}
	
	public double getValue(){
		return this.value;
	}

	public String getYString() {
		return ""+y;
	}
}
