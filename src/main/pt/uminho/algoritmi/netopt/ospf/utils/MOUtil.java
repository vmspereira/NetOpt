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

import pt.uminho.algoritmi.netopt.ospf.simulation.solution.AbstractSolutionSet;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.ASolution;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.ASolutionSet;

public class MOUtil {
	
	
	

	public static AbstractSolutionSet<Integer> getNonDominatedFront(
			ASolutionSet<Integer> solutionSet) {

		AbstractSolutionSet<Integer> nondominated = new AbstractSolutionSet<Integer>();
		int n= filterNonDominatedFront(solutionSet,solutionSet.getNumberOfSolutions());	
		for(int i=0;i<n; i++)
			nondominated.add(solutionSet.getSolution(i));
		return nondominated;
	}
	

	

	
	
	
	
	
	public static int filterNonDominatedFront(ASolutionSet<Integer> front, int noPoints){
		int i, j;
		int n;
		n = noPoints;
		i = 0;
		while (i < n) {
			j = i + 1;
			while (j < n) {
				if (dominates(front.getSolution(i), front.getSolution(j),front.getSolution(i).getNumberOfObjectives())) {
					n--;
					swap(front, j, n);
				} else if (dominates(front.getSolution(j), front.getSolution(i),front.getSolution(i).getNumberOfObjectives())) {		
					n--;
					swap(front, i, n);
					i--;
					break;
				} else
					j++;
			}
			i++;
		}
		return n;
	} 
	
	
	
	
	public static void swap(ASolutionSet<Integer> front, int i, int j) {
		ASolution<Integer> temp;

		temp = front.getSolution(i);
		front.setSolution(i,front.getSolution(j));
		front.setSolution(j,temp);
	}
	
	
	
	
	
	public static boolean dominates(ASolution<Integer> point1, ASolution<Integer> point2, int noObjectives) {
		int i;
		int betterInAnyObjective;
		betterInAnyObjective = 0;
		for (i = 0; i < noObjectives && point1.getFitnessValue(i) <= point2.getFitnessValue(i); i++)
			if (point1.getFitnessValue(i) < point2.getFitnessValue(i))
				betterInAnyObjective = 1;
		return ((i >= noObjectives) && (betterInAnyObjective > 0));
	}

}
