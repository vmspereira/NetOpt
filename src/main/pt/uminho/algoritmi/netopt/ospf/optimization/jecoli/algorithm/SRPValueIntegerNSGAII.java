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
package pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.algorithm;


import jecoli.algorithm.components.representation.linear.ILinearRepresentation;
import jecoli.algorithm.components.representation.linear.ILinearRepresentationFactory;
import jecoli.algorithm.components.solution.ISolutionSet;
import jecoli.algorithm.multiobjective.nsgaII.NSGAII;
import jecoli.algorithm.multiobjective.nsgaII.NSGAIIConfiguration;
import jecoli.algorithm.multiobjective.spea2.SPEA2AlgorithmState;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.SolutionParser;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.AbstractSolutionSet;
import pt.uminho.algoritmi.netopt.ospf.utils.MOUtil;



public class SRPValueIntegerNSGAII extends NSGAII<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>> implements AlgorithmInterface<Integer> {

	
	
public SRPValueIntegerNSGAII(NSGAIIConfiguration<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>> configuration)
			throws Exception {
		super(configuration);
	}

private static final long serialVersionUID = 1L;
	
		
public AbstractSolutionSet<Integer> getSolutionSet(){
	 ISolutionSet<ILinearRepresentation<Integer>> set=this.getAlgorithmState().getSolutionSet();
	 return (AbstractSolutionSet<Integer>) SolutionParser.convert(set);
}



@Override
public AbstractSolutionSet<Integer> getAchiveSolutionSet()
		throws NullPointerException {
	ISolutionSet<ILinearRepresentation<Integer>> set=((SPEA2AlgorithmState<ILinearRepresentation<Integer>>)this.getAlgorithmState()).getArchive();
	return (AbstractSolutionSet<Integer>) SolutionParser.convert(set);
}



@Override
public AbstractSolutionSet<Integer> getNonDominatedSolutionSet()
		throws NullPointerException {
	AbstractSolutionSet<Integer> set=this.getSolutionSet();
	return MOUtil.getNonDominatedFront(set);
	
}


	
	
}
