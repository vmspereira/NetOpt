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
package pt.uminho.algoritmi.netopt.ospf.comparator;

import java.io.Serializable;

/**
 * Compares two solutions using aggregate constraint violation and the Pareto
 * dominance relation as originally proposed by Kalyanmoy Deb.
 * <p>
 * References:
 * <ol>
 * <li>Kalyanmoy, D., "An Efficient Constraint Handling Method for Genetic
 * Algorithms." Computer Methods in Applied Mechanics and Engineering, pp.
 * 311--338, 1998.
 * </ol>
 * 
 * @see AggregateConstraintComparator
 * @see ParetoObjectiveComparator
 */
@SuppressWarnings("serial")
public class ParetoDominanceComparator extends ChainedComparator implements
Serializable {

	
	/**
	 * Constructs a Pareto dominance comparator.
	 */
	public ParetoDominanceComparator() {
		super(new ParetoObjectiveComparator());
	}

}
