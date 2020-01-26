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
package pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.algorithm.representation.permutation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jecoli.algorithm.components.randomnumbergenerator.IRandomNumberGenerator;
import jecoli.algorithm.components.solution.ISolution;
import jecoli.algorithm.components.solution.ISolutionFactory;
import jecoli.algorithm.components.solution.ISolutionSet;
import jecoli.algorithm.components.solution.Solution;
import jecoli.algorithm.components.solution.SolutionSet;


/**
 * A factory for creating PermutationRepresentation objects.
 */
public class PermutationRepresentationFactory implements ISolutionFactory<PermutationRepresentation>,Serializable {

	
	private static final long serialVersionUID = 2072793712057470588L;
   	protected int numberOfObjectives;
   	protected List<Integer> values;
	   

   	
	
   	/**
   	 * Instantiates a new permutation representation factory.
   	 * 
   	 * @param values the values to be permuted
   	 */
   	public PermutationRepresentationFactory(List<Integer> values,int numberObjectives){
   			this.values=values;
	        this.numberOfObjectives = numberObjectives;
	}
   	
   	/**
   	 * Instantiates a new permutation representation factory.
   	 * 
   	 * @param values the values to be permuted
   	 */
   	public PermutationRepresentationFactory(List<Integer> values){
	        this.values=values;
	        this.numberOfObjectives = 1;
	}
	

   	
   		    /**
    	 * Generate solution.
    	 * 
    	 * @param size the size
    	 * 
    	 * @return the i solution
    	 */
    	public ISolution<PermutationRepresentation> generateSolution(IRandomNumberGenerator randomGenerator){
	    	
	    	List<Integer> genome = new ArrayList<Integer>();
	    	Collections.copy(genome,values);
	    	Collections.shuffle(genome);
	        PermutationRepresentation representation = new PermutationRepresentation(genome);
	        return new Solution<PermutationRepresentation>(representation,numberOfObjectives);
	    }
	    
	    /* (non-Javadoc)
    	 * @see core.interfaces.ISolutionFactory#copySolution(core.interfaces.ISolution)
    	 */
    	public ISolution<PermutationRepresentation> copySolution(ISolution<PermutationRepresentation> solutionToCopy) {
	    	PermutationRepresentation solutionGenome = (PermutationRepresentation) solutionToCopy.getRepresentation();
	    	int solutionSize = solutionGenome.getNumberOfElements();
	
	    	List<Integer> newGenome = new ArrayList<Integer>(solutionSize);

	    	for (int i = 0; i < solutionSize; i++) {
				Integer geneValue = solutionGenome.getElement(i);
				Integer geneCopy = new Integer(geneValue.intValue());
				newGenome.add(geneCopy);
			}

			PermutationRepresentation newRepresentation =  new PermutationRepresentation(newGenome);
			ISolution<PermutationRepresentation> newSolution = new Solution<PermutationRepresentation>(newRepresentation);
			return newSolution;

	    }
	    
    	public ISolutionSet<PermutationRepresentation> generateSolutionSet(int numberOfSolutions,IRandomNumberGenerator randomGenerator){
	        ISolutionSet<PermutationRepresentation> solutionSet = new SolutionSet<PermutationRepresentation>();

	        for(int i = 0; i < numberOfSolutions;i++){
	            ISolution<PermutationRepresentation> solution = generateSolution(randomGenerator);
	            solutionSet.add(solution);
	        }
	        
	        return solutionSet;
	    }

		@Override
		public PermutationRepresentationFactory deepCopy(){
			List<Integer> genome = new ArrayList<Integer>();
	    	Collections.copy(genome,values);
			return new PermutationRepresentationFactory(genome);
		}

		@Override
		public int getNumberOfObjectives() {
			return numberOfObjectives;
		}



}


