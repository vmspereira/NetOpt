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

import pt.uminho.algoritmi.netopt.ospf.simulation.solution.ASolution;




@SuppressWarnings("serial")
public class ChainedComparator implements DominanceComparator, Serializable {

	/**
	 * The comparators in the order they are to be applied.
	 */
	private DominanceComparator[] comparators;

	public ChainedComparator(DominanceComparator... comparators) {
		super();
		this.comparators = comparators;
	}

	@Override
	public int compare(ASolution<?> solution1, ASolution<?> solution2) {
		for (DominanceComparator comparator : comparators) {
			int flag = comparator.compare(solution1, solution2);

			if (flag != 0) {
				return flag;
			}
		}

		return 0;
	}

}
