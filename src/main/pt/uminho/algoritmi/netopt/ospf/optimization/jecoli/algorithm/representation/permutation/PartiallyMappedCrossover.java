package pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.algorithm.representation.permutation;

import java.util.ArrayList;
import java.util.List;

import jecoli.algorithm.components.operator.reproduction.linear.AbstractCrossoverOperator;
import jecoli.algorithm.components.randomnumbergenerator.IRandomNumberGenerator;
import jecoli.algorithm.components.representation.linear.ILinearRepresentation;
import jecoli.algorithm.components.representation.linear.ILinearRepresentationFactory;
import jecoli.algorithm.components.representation.linear.LinearRepresentation;
import jecoli.algorithm.components.solution.ISolution;
import jecoli.algorithm.components.solution.Solution;

public class PartiallyMappedCrossover<G> extends AbstractCrossoverOperator<ILinearRepresentation<G>, ILinearRepresentationFactory<G>> {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Positions between which the mutation is applied
	 */
	protected int minPosition;
	protected int maxPosition;

	public PartiallyMappedCrossover() {
		this.minPosition = 0;
		this.maxPosition = Integer.MAX_VALUE;
	}

	public PartiallyMappedCrossover(int minPosition, int maxPosition) {
		this.minPosition = minPosition;
		this.maxPosition = maxPosition;
	}

	@Override
	public PartiallyMappedCrossover<G> deepCopy() throws Exception {
		return new PartiallyMappedCrossover<G>(this.minPosition, this.maxPosition);
	}

	@Override
	protected List<ISolution<ILinearRepresentation<G>>> crossOverGenomes(ILinearRepresentation<G> parentGenome,
			ILinearRepresentation<G> parent1Genome, ILinearRepresentationFactory<G> solutionFactory,
			IRandomNumberGenerator randomNumberGenerator) {

		int parentGenomeSize = parentGenome.getNumberOfElements();
		int parent1GenomeSize = parent1Genome.getNumberOfElements();

		int finalSize = Math.min(maxPosition, Math.min(parentGenomeSize, parent1GenomeSize)) - minPosition;

		int crossoverPosition = (int) (randomNumberGenerator.nextDouble() * (finalSize - 2)) + minPosition;
		int crossoverPosition1 = (int) (randomNumberGenerator.nextDouble() * (finalSize - 2)) + minPosition;

		while (crossoverPosition == crossoverPosition1) {
			crossoverPosition1 = (int) (randomNumberGenerator.nextDouble() * (finalSize - 2)) + minPosition;
		}

		int cuttingPoint1 = Math.min(crossoverPosition, crossoverPosition1);
		int cuttingPoint2 = Math.max(crossoverPosition, crossoverPosition1);

		List<G> childGenome = new ArrayList<G>();
		List<G> childGenome1 = new ArrayList<G>();

		for (int i = 0; i < finalSize; i++) {
			if(i<minPosition ||i>maxPosition){
				childGenome.set(i,parentGenome.getElementAt(i));
				childGenome1.set(i,parent1Genome.getElementAt(i));
			}else{
				childGenome.set(i,null);
				childGenome1.set(i,null);
			}
		}

		for (int i = cuttingPoint1; i <= cuttingPoint2; i++) {
			childGenome.set(i,parent1Genome.getElementAt(i));
			childGenome1.set(i,parentGenome.getElementAt(i));
		}

		// fill in remaining slots with replacements
		for (int i = minPosition; i < maxPosition; i++) {
			if ((i < cuttingPoint1) || (i > cuttingPoint2)) {
				G n1 = parentGenome.getElementAt(i);
				G n2 = parent1Genome.getElementAt(i);
				
			}
		}

		int numObjectives = solutionFactory.getNumberOfObjectives();

		ISolution<ILinearRepresentation<G>> childSolution = new Solution<ILinearRepresentation<G>>(
				new LinearRepresentation<G>(childGenome), numObjectives);
		ISolution<ILinearRepresentation<G>> child1Solution = new Solution<ILinearRepresentation<G>>(
				new LinearRepresentation<G>(childGenome1), numObjectives);

		List<ISolution<ILinearRepresentation<G>>> solutionList = new ArrayList<ISolution<ILinearRepresentation<G>>>();
		solutionList.add(child1Solution);
		solutionList.add(childSolution);
		return solutionList;

	}

}
