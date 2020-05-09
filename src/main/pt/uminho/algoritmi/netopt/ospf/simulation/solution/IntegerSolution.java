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
package pt.uminho.algoritmi.netopt.ospf.simulation.solution;


import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class IntegerSolution extends AbstractSolution<Integer> implements Serializable{

	
	protected IntegerSolution(int numberOfObjectives, int numberOfVariables) {
		super(numberOfObjectives, numberOfVariables);
	}

	
	public IntegerSolution(List<Integer> genome, int numberOfObjectives) {
		super(numberOfObjectives, genome.size());
		for(int i=0;i<genome.size();i++)
			this.setVariableValue(i, genome.get(i));
	}

	
	public IntegerSolution(int[] genome, int numberOfObjectives) {
		super(numberOfObjectives, genome.length);
		for(int i=0;i<genome.length;i++)
			this.setVariableValue(i, genome[i]);
	}

	@Override
	public IntegerSolution copy() {
		IntegerSolution solution = new IntegerSolution(this.getNumberOfObjectives(),this.getNumberOfVariables());
		for(int i=0;i<getNumberOfObjectives();i++){
			solution.setObjective(i, this.getObjective(i));
		}
		for(int i=0;i<getNumberOfVariables();i++){
			solution.setVariableValue(i, this.getVariableValue(i).intValue());
		}
		return solution;
	}
	
	
	public int[] getVariablesArray(){
		int[] array = new int[this.getNumberOfVariables()];
		for(int i=0;i<this.getNumberOfVariables();i++)
			array[i]=this.getVariableValue(i).intValue();
		return array;
	}
	

}
