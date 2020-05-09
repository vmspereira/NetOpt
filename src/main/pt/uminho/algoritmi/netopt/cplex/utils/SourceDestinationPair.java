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

public class SourceDestinationPair {
	final int source;
	final int destination;
	
	public SourceDestinationPair(int source, int destination) {
		this.source = source;
		this.destination = destination;
	}
	
	public int getSource(){
		return source;
	}
	
	public int getDestination(){
		return destination;
	}
	
	@Override 
	public boolean equals(Object obj){
		if(!(obj instanceof SourceDestinationPair))
			return false;
		else{
			SourceDestinationPair p= (SourceDestinationPair) obj;
			return (this.source==p.getSource() && this.destination==p.getDestination());
		}
	}
	
	
	@Override
    public int hashCode() 
    {  
        return this.source*199+this.destination; 
    } 
	
	public String toString(){
		return source+"-"+destination;
	}

	public String getXString() {
		return ""+source;
	}

	public String getYString() {
		return ""+destination;
	}
}
