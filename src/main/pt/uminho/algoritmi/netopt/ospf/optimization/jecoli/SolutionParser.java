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
package pt.uminho.algoritmi.netopt.ospf.optimization.jecoli;

import java.util.Iterator;
import java.util.List;

import jecoli.algorithm.components.representation.linear.ILinearRepresentation;
import jecoli.algorithm.components.representation.linear.LinearRepresentation;
import jecoli.algorithm.components.solution.ISolution;
import jecoli.algorithm.components.solution.ISolutionSet;
import jecoli.algorithm.components.solution.Solution;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.ASolution;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.ASolutionSet;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.AbstractSolutionSet;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.IntegerSolution;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.RealSolution;

public class SolutionParser {

	public static ASolution<Integer> convert(
			jecoli.algorithm.components.solution.ISolution<ILinearRepresentation<Integer>> solution) {
		int nobj = solution.getNumberOfObjectives();
		List<Integer> l = solution.getRepresentation().getGenome();
		IntegerSolution s = new IntegerSolution(l, nobj);
	    for(int i=0;i<nobj;i++)
	    	s.setObjective(i, solution.getFitnessValue(i));
		return s;
	}

	
	
	public static ASolution<Double> convertReal(
			jecoli.algorithm.components.solution.ISolution<ILinearRepresentation<Double>> solution) {
		int nobj = solution.getNumberOfObjectives();
		List<Double> l = solution.getRepresentation().getGenome();
		RealSolution s = new RealSolution(l, nobj);
	    for(int i=0;i<nobj;i++)
	    	s.setObjective(i, solution.getFitnessValue(i));
		return s;
	}

	
	
	
	public static ASolutionSet<Integer> convert(ISolutionSet<ILinearRepresentation<Integer>> set) {

		List<ISolution<ILinearRepresentation<Integer>>> l = set.getListOfSolutions();
		Iterator<ISolution<ILinearRepresentation<Integer>>> it = l.iterator();
		AbstractSolutionSet<Integer> newSet = new AbstractSolutionSet<Integer>();
		while (it.hasNext()) {
			newSet.add(SolutionParser.convert(it.next()));
		}
		return newSet;
	}
	
	
	
	public static ASolutionSet<Double> convertReal(ISolutionSet<ILinearRepresentation<Double>> set) {

		List<ISolution<ILinearRepresentation<Double>>> l = set.getListOfSolutions();
		Iterator<ISolution<ILinearRepresentation<Double>>> it = l.iterator();
		AbstractSolutionSet<Double> newSet = new AbstractSolutionSet<Double>();
		while (it.hasNext()) {
			newSet.add(SolutionParser.convertReal(it.next()));
		}
		return newSet;
	}
	
	
	
	public static jecoli.algorithm.components.solution.ISolution<ILinearRepresentation<Integer>> convert(IntegerSolution solution,int nobj) {
		IntegerSolution sol=solution.copy();
		List<Integer> l = sol.getVariables();
		LinearRepresentation<Integer> r = new LinearRepresentation<Integer>(l);
		ISolution<ILinearRepresentation<Integer>> s = new Solution<ILinearRepresentation<Integer>>(r,2);
		return s;
	}
	
	
	
	public static jecoli.algorithm.components.solution.ISolution<ILinearRepresentation<Double>> convertReal(RealSolution solution) {
		int nobj = solution.getNumberOfObjectives();
		RealSolution sol=solution.copy();
		List<Double> l = sol.getVariables();
		LinearRepresentation<Double> r = new LinearRepresentation<Double>(l);
		ISolution<ILinearRepresentation<Double>> s = new Solution<ILinearRepresentation<Double>>(r,nobj);
		return s;
	}
}
