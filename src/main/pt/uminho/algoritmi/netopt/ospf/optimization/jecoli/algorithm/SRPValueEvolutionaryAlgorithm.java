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
 *  @author V�tor Pereira
 ******************************************************************************/
package pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.algorithm;

import jecoli.algorithm.components.representation.linear.ILinearRepresentation;
import jecoli.algorithm.components.representation.real.RealValueRepresentationFactory;
import jecoli.algorithm.components.solution.ISolutionSet;
import jecoli.algorithm.singleobjective.evolutionary.EvolutionaryAlgorithm;
import jecoli.algorithm.singleobjective.evolutionary.EvolutionaryConfiguration;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.SolutionParser;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.AbstractSolutionSet;


/**
 * Allows to retrieve the solutionSet to be used as initialPopulation
 * wrapper for jecoli
 * @author Vitor Pereira
 *
 */
public class SRPValueEvolutionaryAlgorithm extends EvolutionaryAlgorithm<ILinearRepresentation<Double>, RealValueRepresentationFactory> implements AlgorithmInterface<Double>{

	
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public SRPValueEvolutionaryAlgorithm(EvolutionaryConfiguration<ILinearRepresentation<Double>,RealValueRepresentationFactory> configuration)
			throws Exception {
			super(configuration);
	}
	
	
	public AbstractSolutionSet<Double> getSolutionSet(){
		 ISolutionSet<ILinearRepresentation<Double>> set=this.getAlgorithmState().getSolutionSet();
		 return (AbstractSolutionSet<Double>) SolutionParser.convertReal(set);
	}



	@Override
	public AbstractSolutionSet<Double> getAchiveSolutionSet()
			throws NullPointerException {
		ISolutionSet<ILinearRepresentation<Double>> set= this.getAlgorithmState().getSolutionSet();
		return (AbstractSolutionSet<Double>) SolutionParser.convertReal(set);
	}



	@Override
	public AbstractSolutionSet<Double> getNonDominatedSolutionSet()
			throws NullPointerException {
		AbstractSolutionSet<Double> set=this.getSolutionSet();
		return set;
	}


	
	 
}
