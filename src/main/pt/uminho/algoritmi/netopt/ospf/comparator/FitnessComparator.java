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
import java.util.Comparator;

import pt.uminho.algoritmi.netopt.ospf.simulation.solution.ASolution;




@SuppressWarnings({ "serial", "rawtypes" })
public class FitnessComparator implements DominanceComparator, 
Comparator<ASolution>, Serializable {


	public FitnessComparator() {
		super();
	}

	@Override
	public int compare(ASolution solution1, ASolution solution2) {
		if(solution1.getNumberOfVariables()!=solution2.getNumberOfVariables())
			return 0;
		
		return Double.compare(
				(Double)solution1.getFitnessValue(),
				(Double)solution2.getFitnessValue());
	}

}
