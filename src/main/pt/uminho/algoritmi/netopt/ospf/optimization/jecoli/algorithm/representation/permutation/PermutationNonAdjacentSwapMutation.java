/**
* Copyright 2017,
* Centro Algoritmi
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
* @author Vítor Pereira
*/
package pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.algorithm.representation.permutation;

import jecoli.algorithm.components.operator.reproduction.linear.AbstractMutationOperator;
import jecoli.algorithm.components.randomnumbergenerator.IRandomNumberGenerator;
import jecoli.algorithm.components.representation.linear.ILinearRepresentation;
import jecoli.algorithm.components.representation.linear.ILinearRepresentationFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class PermutationNonAdjacentSwapMutation.
 */
public class PermutationNonAdjacentSwapMutation
		extends AbstractMutationOperator<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>> {

	/**
	 * Positions between which the mutation is applied
	 */
	protected int minPosition;
	protected int maxPosition;

	/**
	 * 
	 */
	private static final long serialVersionUID = 320340013308073657L;

	public PermutationNonAdjacentSwapMutation() {
		this.minPosition = 0;
		this.maxPosition = Integer.MAX_VALUE;
	}

	public PermutationNonAdjacentSwapMutation(int minPosition, int maxPosition) {
		this.minPosition = minPosition;
		this.maxPosition = maxPosition;
	}

	@Override
	public PermutationNonAdjacentSwapMutation deepCopy() throws Exception {
		return new PermutationNonAdjacentSwapMutation(this.minPosition, this.maxPosition);
	}

	@Override
	protected void mutateGenome(ILinearRepresentation<Integer> childGenome,
			ILinearRepresentationFactory<Integer> solutionFactory, IRandomNumberGenerator randomNumberGenerator) {
		
		
		int numberOfGenes = Math.min(childGenome.getNumberOfElements(), maxPosition) - minPosition;
		int pos, pos1,pos_next,pos_current,inc;
		pos = (int) (randomNumberGenerator.nextDouble() * (numberOfGenes)) + minPosition;
		do {
			pos1 = (int) (randomNumberGenerator.nextDouble() * (numberOfGenes)) + minPosition;
		} while (pos == pos1);
		inc = (randomNumberGenerator.nextDouble()<0.5)? 1 : -1;
		pos_current=pos1;
		pos_next = (((pos1+inc)< minPosition)? (minPosition+numberOfGenes-1) : (pos1-minPosition+inc) % numberOfGenes +minPosition );
		Integer g1 = childGenome.getElementAt(pos);
		Integer g2 = childGenome.getElementAt(pos_current);
		// while the values to be permuted are equal or until all values have been compared
		while(g1==g2 && pos1!=pos_next ){
			pos_current = pos_next;
			g2 = childGenome.getElementAt(pos_current);
			pos_next = (((pos_next+inc)< minPosition)? (minPosition+numberOfGenes-1) : (pos_next-minPosition+inc) % numberOfGenes +minPosition );
		}
		childGenome.setElement(pos, g2);
		childGenome.setElement(pos_current,g1);

	}

}
