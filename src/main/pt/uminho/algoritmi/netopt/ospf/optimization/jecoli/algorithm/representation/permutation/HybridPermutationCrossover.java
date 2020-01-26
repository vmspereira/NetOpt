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

import java.util.ArrayList;
import java.util.List;

import jecoli.algorithm.components.operator.IReproductionOperator;
import jecoli.algorithm.components.operator.reproduction.linear.AbstractCrossoverOperator;
import jecoli.algorithm.components.randomnumbergenerator.IRandomNumberGenerator;
import jecoli.algorithm.components.representation.linear.ILinearRepresentation;
import jecoli.algorithm.components.representation.linear.ILinearRepresentationFactory;
import jecoli.algorithm.components.representation.linear.LinearRepresentation;
import jecoli.algorithm.components.solution.ISolution;
import jecoli.algorithm.components.solution.Solution;

/**
 * 
 * @author vitor pereira
 *
 * @param <G>
 * 
 * 
 * Permutation crossover for hybrid representations. 
 * Swaps permutations representations between both parents.
 */

public class HybridPermutationCrossover<G> extends AbstractCrossoverOperator<ILinearRepresentation<G>,ILinearRepresentationFactory<G>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int position;
	
	public HybridPermutationCrossover(int position){
		this.position=position;
	}

	@Override
	public IReproductionOperator<ILinearRepresentation<G>, ILinearRepresentationFactory<G>> deepCopy()
			throws Exception {
		return new HybridPermutationCrossover(this.position);
	}

	@Override
	protected List<ISolution<ILinearRepresentation<G>>> crossOverGenomes(ILinearRepresentation<G> parentGenome,
			ILinearRepresentation<G> parent1Genome, ILinearRepresentationFactory<G> solutionFactory,
			IRandomNumberGenerator randomNumberGenerator) {
		
		int parentGenomeSize = parentGenome.getNumberOfElements();
				
		List<G> childGenome = new ArrayList<G>();	
		List<G> childGenome1 = new ArrayList<G>();

		for(int i=0; i< position; i++){
			childGenome.add(parentGenome.getElementAt(i));
			childGenome1.add(parent1Genome.getElementAt(i));
		}

		for(int i = position; i<parentGenomeSize; i++){
			childGenome.add(parent1Genome.getElementAt(i));
			childGenome1.add(parentGenome.getElementAt(i));
		}

		int numObjectives = solutionFactory.getNumberOfObjectives();

		ISolution<ILinearRepresentation<G>> childSolution = new Solution<ILinearRepresentation<G>>(new LinearRepresentation<G>(childGenome),numObjectives);
		ISolution<ILinearRepresentation<G>> child1Solution = new Solution<ILinearRepresentation<G>>(new LinearRepresentation<G>(childGenome1),numObjectives);
		
		List<ISolution<ILinearRepresentation<G>>> solutionList = new ArrayList<ISolution<ILinearRepresentation<G>>>();
		solutionList.add(child1Solution); solutionList.add(childSolution);
		return solutionList;

		
		
	}
	
	
}
