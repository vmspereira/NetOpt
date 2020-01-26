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
/**
* Copyright 2009,
* CCTC - Computer Science and Technology Center
* IBB-CEB - Institute for Biotechnology and  Bioengineering - Centre of Biological Engineering
* University of Minho
*
* This is free software: you can redistribute it and/or modify
* it under the terms of the GNU Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This code is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Public License for more details.
*
* You should have received a copy of the GNU Public License
* along with this code.  If not, see <http://www.gnu.org/licenses/>.
* 
* Created inside the SysBio Research Group <http://sysbio.di.uminho.pt/>
* University of Minho
*/
package pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.algorithm.representation.permutation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jecoli.algorithm.components.operator.reproduction.linear.AbstractMutationOperator;
import jecoli.algorithm.components.randomnumbergenerator.IRandomNumberGenerator;
import jecoli.algorithm.components.representation.linear.ILinearRepresentation;
import jecoli.algorithm.components.representation.linear.ILinearRepresentationFactory;

// TODO: Auto-generated Javado c
/**
 * The Class PermutationInversionMutation.
 * 
 */
public class PermutationAdjacentSwapMutation
		extends AbstractMutationOperator<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>> {

	/**
	 * Positions between which the mutation is applied
	 */
	protected int minPosition;
	protected int maxPosition;

	/**
	 * 
	 */
	private static final long serialVersionUID = 4187761441509119144L;

	public PermutationAdjacentSwapMutation() {
		this.minPosition = 0;
		this.maxPosition = Integer.MAX_VALUE;
	}

	public PermutationAdjacentSwapMutation(int minPosition, int maxPosition) {
		this.minPosition = minPosition;
		this.maxPosition = maxPosition;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * operators.reproduction.permutations.AbstractPermutationMutationOperator#
	 * mutateGenome(core.representation.permutations.PermutationRepresentation)
	 */

	
	@Override
	public PermutationInversionMutation deepCopy() throws Exception {
		return new PermutationInversionMutation(this.minPosition, this.maxPosition);
	}

	@Override
	protected void mutateGenome(ILinearRepresentation<Integer> childGenome,
			ILinearRepresentationFactory<Integer> solutionFactory, IRandomNumberGenerator randomNumberGenerator) {
		
		List<Integer> genome = new ArrayList<Integer>();
		int numberOfGenes = Math.min(childGenome.getNumberOfElements(), maxPosition) - minPosition;
		for(int i=0;i<numberOfGenes;i++)
			genome.add(i,childGenome.getElementAt(minPosition+i));
		Collections.reverse(genome);
		for(int i=0;i<numberOfGenes;i++)
			childGenome.setElement(minPosition+i,genome.get(i));
	}

}
