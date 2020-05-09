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
package pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.algorithm.representation.tuple;

import java.io.Serializable;


import jecoli.algorithm.components.representation.IComparableRepresentation;
import jecoli.algorithm.components.representation.IRepresentation;
import jecoli.algorithm.components.representation.linear.LinearRepresentation;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.algorithm.representation.permutation.PermutationRepresentation;



public class TupleRepresentation implements IRepresentation,IComparableRepresentation<TupleRepresentation>, Serializable{


	private LinearRepresentation<Integer> linear;
	private PermutationRepresentation permutation;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	public TupleRepresentation(LinearRepresentation<Integer> linear,PermutationRepresentation permutation){
		this.linear=linear;
		this.permutation=permutation;
				
	}

	
	/**
     * Gets the number of elements.
     * 
     * @return the number of elements
     */
    public int getNumberOfElements() {
        return linear.getNumberOfElements()+permutation.getNumberOfElements();
    }
	
	@Override
	public boolean equals(TupleRepresentation representation) {
		return representation.getLinearRepresentation().equals(linear) && representation.getPermutationRepresentation().equals(permutation);
	}

	@Override
	public String stringRepresentation() {
		StringBuffer sb =new StringBuffer();
		sb.append(this.linear.stringRepresentation());
		sb.append(this.permutation.stringRepresentation());
		return sb.toString();
	}
	
	public LinearRepresentation<Integer> getLinearRepresentation(){
		return this.linear;
	}
	
	public PermutationRepresentation getPermutationRepresentation(){
		return this.permutation;
	}

}
