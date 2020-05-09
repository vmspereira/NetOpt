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

import pt.uminho.algoritmi.netopt.ospf.simulation.solution.ASolution;

public interface DominanceComparator {

	/**
	 * Compares the two solutions using a dominance relation, returning
	 * {@code -1} if {@code solution1} dominates {@code solution2}, {@code 1} if
	 * {@code solution2} dominates {@code solution1}, and {@code 0} if the
	 * solutions are non-dominated.
	 * 
	 * @param solution1 the first solution
	 * @param solution2 the second solution
	 * @return {@code -1} if {@code solution1} dominates {@code solution2},
	 *         {@code 1} if {@code solution2} dominates {@code solution1}, and
	 *         {@code 0} if the solutions are non-dominated
	 */
	public int compare(ASolution<?> solution1, ASolution<?> solution2);

}
