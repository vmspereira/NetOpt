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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public abstract class AbstractSolution<E> implements ASolution<E>, Serializable {

	private List<E> variables;
	private double[] objectives;
	protected Map<Object, Object> attributes;

	/**
	 * Constructor
	 */
	protected AbstractSolution(int numberOfObjectives, int numberOfVariables) {

		attributes = new HashMap<Object, Object>();
		objectives = new double[numberOfObjectives];
		variables = new ArrayList<>(numberOfVariables);
		for (int i = 0; i < numberOfVariables; i++) {
			variables.add(i, null);
		}
	}

	@Override
	public double getFitnessValue(int i) {
		return objectives[i];
	}

	public void setAttribute(Object id, Object value) {
		attributes.put(id, value);
	}

	public Object getAttribute(Object id) {
		return attributes.get(id);
	}

	public void setObjective(int index, double value) {
		objectives[index] = value;
	}

	public double getObjective(int index) {
		return objectives[index];
	}

	public E getVariableValue(int index) {
		return variables.get(index);
	}

	public void setVariableValue(int index, E value) {
		variables.set(index, value);
	}

	
	public List<E> getVariables(){
		return this.variables;
	}
	
	public int getNumberOfVariables() {
		return variables.size();
	}

	public int getNumberOfObjectives() {
		return objectives.length;
	}

	@Override
	public String toString() {
		String result = "Variables: ";
		for (E var : variables) {
			result += "" + var + " ";
		}
		result += "Objectives: ";
		for (Double obj : objectives) {
			result += "" + obj + " ";
		}
		result += "\t";
		result += "AlgorithmAttributes: " + attributes + "\n";

		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		AbstractSolution<?> that = (AbstractSolution<?>) o;

		if (!attributes.equals(that.attributes))
			return false;
		if (!Arrays.equals(objectives, that.objectives))
			return false;
		if (!variables.equals(that.variables))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = Arrays.hashCode(objectives);
		result = 31 * result + variables.hashCode();
		result = 31 * result + attributes.hashCode();
		return result;
	}

	@Override
	public abstract ASolution<E> copy();

	
	@Override
	public double getFitnessValue(){
		double sum=0.0;
		for(int i=0;i<this.getNumberOfObjectives();i++)
			sum+=this.getFitnessValue(i);
		return sum;
	}
}
