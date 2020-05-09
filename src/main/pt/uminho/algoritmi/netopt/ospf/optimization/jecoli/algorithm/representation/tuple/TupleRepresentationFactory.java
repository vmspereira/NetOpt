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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jecoli.algorithm.components.randomnumbergenerator.IRandomNumberGenerator;
import jecoli.algorithm.components.representation.integer.IntegerRepresentationFactory;
import jecoli.algorithm.components.representation.linear.ILinearRepresentation;
import jecoli.algorithm.components.representation.linear.ILinearRepresentationFactory;
import jecoli.algorithm.components.solution.ISolution;
import jecoli.algorithm.components.solution.Solution;

public class TupleRepresentationFactory extends IntegerRepresentationFactory implements ILinearRepresentationFactory<Integer>, Serializable {

	
	private static final long serialVersionUID = 1L;
	
	/** The permutation list**/
	protected List<Integer> permutationValues;
	
	protected int linearSolutionPartSize;
	
	public TupleRepresentationFactory(int linearSolutionPartSize, Integer upperBound, Integer lowerBound,int nobjectives ,List<Integer> permutationValues) {
		super(linearSolutionPartSize+permutationValues.size(),upperBound,lowerBound,nobjectives);
		this.linearSolutionPartSize=linearSolutionPartSize;
		this.permutationValues = permutationValues;
	}
	
	
	
	@Override
	public ISolution<ILinearRepresentation<Integer>> generateSolution(IRandomNumberGenerator randomGenerator) {
		return generateSolution(solutionSize,randomGenerator);
	}

	
	@Override
	public ISolution<ILinearRepresentation<Integer>> generateSolution(int size,
			IRandomNumberGenerator randomGenerator) {
		List<Integer> genome = new ArrayList<Integer>(solutionSize);

        for(int i = 0; i < linearSolutionPartSize;i++){
            Integer geneValue = generateGeneValue(i,randomGenerator);
            genome.add(geneValue);
        }
        ArrayList<Integer> permutation = new ArrayList<Integer>(this.permutationValues);
        Collections.shuffle(permutation);
        genome.addAll(permutation);
        ILinearRepresentation<Integer> representation = createRepresentation(genome);
        return new Solution<ILinearRepresentation<Integer>>(representation,numberOfObjectives);

	}
	
	
	public ISolution<ILinearRepresentation<Integer>> generateSolution(int[] weights) throws Exception {
		if(weights.length != linearSolutionPartSize)
			throw new Exception();
		else{
			List<Integer> genome = new ArrayList<Integer>(solutionSize);
			for(int i = 0; i < linearSolutionPartSize;i++){
	            genome.add(weights[i]);
	        }
			ArrayList<Integer> permutation = new ArrayList<Integer>(this.permutationValues);
	        Collections.shuffle(permutation);
	        genome.addAll(permutation);
	        ILinearRepresentation<Integer> representation = createRepresentation(genome);
	        return new Solution<ILinearRepresentation<Integer>>(representation,numberOfObjectives);
		}
		
	}
	
		
	@Override
	public TupleRepresentationFactory deepCopy() {
		return new TupleRepresentationFactory(linearSolutionPartSize,this.lowerBoundGeneLimitList.get(0).intValue(),this.upperBoundGeneLimitList.get(0).intValue(),this.numberOfObjectives,this.permutationValues);
	}

	
}
