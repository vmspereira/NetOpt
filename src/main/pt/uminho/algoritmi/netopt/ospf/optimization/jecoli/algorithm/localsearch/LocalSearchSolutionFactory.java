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
package pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.algorithm.localsearch;

import java.util.ArrayList;
import java.util.List;

import jecoli.algorithm.components.randomnumbergenerator.IRandomNumberGenerator;
import jecoli.algorithm.components.representation.linear.ILinearRepresentation;
import jecoli.algorithm.components.representation.linear.LinearRepresentation;
import jecoli.algorithm.components.solution.ISolution;
import jecoli.algorithm.components.solution.ISolutionSet;
import jecoli.algorithm.components.solution.Solution;
import jecoli.algorithm.components.solution.SolutionSet;


public class LocalSearchSolutionFactory {

	ISolution<ILinearRepresentation<Integer>> solution;
	int numberOfObjectives=1;
	
	public LocalSearchSolutionFactory(ISolution<ILinearRepresentation<Integer>> solution,int numberOfObjectives){
		this.solution=solution;
		this.numberOfObjectives=numberOfObjectives;
	}
	
	

	
	public ISolutionSet<ILinearRepresentation<Integer>> generateSolutionSet(int numberOfSolutions,
			IRandomNumberGenerator randomGenerator) {
		ISolutionSet<ILinearRepresentation<Integer>> solutionSet = new SolutionSet<ILinearRepresentation<Integer>>();
		for(int i=0;i<solution.getRepresentation().getNumberOfElements();i++){
			List<Integer> childGenome = new ArrayList<Integer>();	
			List<Integer> childGenome1 = new ArrayList<Integer>();
			for(int j=0;j<solution.getRepresentation().getNumberOfElements();j++){
				childGenome.add(solution.getRepresentation().getElementAt(j));
				childGenome1.add(solution.getRepresentation().getElementAt(j));
			}
			
			ISolution<ILinearRepresentation<Integer>> childSolution = new Solution<ILinearRepresentation<Integer>>(new LinearRepresentation<Integer>(childGenome),numberOfObjectives);
			solutionSet.add(childSolution);
			ISolution<ILinearRepresentation<Integer>> child1Solution = new Solution<ILinearRepresentation<Integer>>(new LinearRepresentation<Integer>(childGenome1),numberOfObjectives);
			solutionSet.add(child1Solution);
		}
		return solutionSet;
	}

	
	public int getNumberOfObjectives() {
		return numberOfObjectives;
	}

}
