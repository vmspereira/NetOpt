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
package pt.uminho.algoritmi.netopt.ospf.simulation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import pt.uminho.algoritmi.netopt.ospf.comparator.FitnessComparator;
import pt.uminho.algoritmi.netopt.ospf.comparator.SingleObjectiveComparator;
import pt.uminho.algoritmi.netopt.ospf.comparator.TradeOffComparator;
import pt.uminho.algoritmi.netopt.ospf.listener.PopulationChangedListener;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.Atributes;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.ASolution;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.ASolutionSet;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.IntegerSolution;



public class Population implements Serializable {

	
	public enum Encoding{
		WEIGHTS,PVALUES,NODETYPES;
	}
	
	private static final long serialVersionUID = 1L;
	private int numberOfObjectives;
	List<IntegerSolution> data;
	List<PopulationChangedListener> listeners = new ArrayList<PopulationChangedListener>();
	private List<SolutionType> types;
	
	
	
	
	String[] objectivesName;

	private boolean ALLOWEQUALGENOME = false;
	private boolean ALLOWEQUALFITNESS = false;

	public Population() {
		numberOfObjectives = 0;
		this.data = new ArrayList<IntegerSolution>();
		this.listeners=new ArrayList<PopulationChangedListener>();
		this.types =new ArrayList<SolutionType>();
	}
	
	public Population(int nObjectives) {
		numberOfObjectives = nObjectives;
		this.data = new ArrayList<IntegerSolution>();
		this.listeners=new ArrayList<PopulationChangedListener>();
		this.types =new ArrayList<SolutionType>();
	}

	public Population(ASolutionSet<Integer> population) {
		this.data = new ArrayList<IntegerSolution>();
		int n = population.getNumberOfSolutions();
		numberOfObjectives = population.getNumberOfObjectives();
		for (int i = 0; i < n; i++)
			add(population.getSolution(i));
		this.listeners=new ArrayList<PopulationChangedListener>();
		this.types =new ArrayList<SolutionType>();
	}

	public Population(List<IntegerSolution> set) {
		this.data = new ArrayList<IntegerSolution>();
		Iterator<IntegerSolution> it = set.iterator();
		while (it.hasNext())
			add(it.next());
		this.listeners=new ArrayList<PopulationChangedListener>();
		this.types =new ArrayList<SolutionType>();
	}

	public void setPopulation(ASolutionSet<Integer> population) {
		this.numberOfObjectives = population.getNumberOfObjectives();
		this.data = new ArrayList<IntegerSolution>();
		int n = population.getNumberOfSolutions();
		for (int i = 0; i < n; i++)
			add(population.getSolution(i));
	}

	public int getNumberOfSolutions() {
		return data.size();
	}

	public IntegerSolution getSolution(int i) {
		return data.get(i);
	}

	public int[] getLinearSolution(int i) {
		IntegerSolution s = getSolution(i);
		int[] genome = new int[s.getNumberOfVariables()];
		for (int j = 0; j < s.getNumberOfVariables(); j++) {
			genome[j] = s.getVariableValue(j);
		}
		return genome;
	}

	public void setObjectiveName(String[] names) {
		this.objectivesName = names;
	}

	public String[] getObjectiveName() {
		return this.objectivesName;
	}

	/**
	 * Clone the population
	 * 
	 * A cloned population is used when resourcing to previously obtained
	 * populations on new optimization processes.
	 */

	public Population copy(int numberOfObjectives) {
		List<IntegerSolution> set = new ArrayList<IntegerSolution>();
		int n = getNumberOfSolutions();
		// Solution
		for (int i = 0; i < n; i++) {
			IntegerSolution s = getSolution(i);
			set.add(s.copy());
		}
		Population p = new Population(set);
		return p;
	}

	/**
	 * Solutions from MOEA the scalar fitness value has to be
	 * calculated using each objective fitness value and alpha.
	 * 
	 * @param alpha
	 */
	public void setAlphaScalarFitness(double alpha) {

		for (int i = 0; i < getNumberOfSolutions(); i++) {
			IntegerSolution s = getSolution(i);
			if (s.getNumberOfObjectives() == 2) {
				double d1 = s.getFitnessValue(0);
				double d2 = s.getFitnessValue(1);
				s.setAttribute(Atributes.SCALARFITNESS, alpha * d1 + (1.0 - alpha) * d2);
			}
		}
	}

	/**
	 * changes weight at index for all genomes on the population
	 * 
	 * @param index
	 * @param weigth
	 */
	public void changeWeight(int index, int weigth) {

		for (int i = 0; i < getNumberOfSolutions(); i++) {
			IntegerSolution s = getSolution(i);
			s.setVariableValue(index, weigth);
		}
	}

	
	/**
	 * 
	 * @return double[size][number of objectives]
	 */
	public double[][] getParetoMatrix() {
		int nobjectives = getSolution(0).getNumberOfObjectives();
		double[][] pareto = new double[getNumberOfSolutions()][nobjectives];
		for (int i = 0; i < getNumberOfSolutions(); i++) {
			IntegerSolution solution = getSolution(i);
			for (int j = 0; j < nobjectives; j++)
				pareto[i][j] = solution.getFitnessValue(j);
		}
		return pareto;
	}

	/**
	 * 
	 * @return double[number of objectives][size]
	 */
	public double[][] getParetoMatrixTranspose() {
		int nobjectives = getSolution(0).getNumberOfObjectives();
		double[][] pareto = new double[nobjectives][getNumberOfSolutions()];
		for (int i = 0; i < getNumberOfSolutions(); i++) {
			IntegerSolution solution = getSolution(i);
			for (int j = 0; j < nobjectives; j++)
				pareto[j][i] = solution.getFitnessValue(j);
		}
		return pareto;
	}

	/**
	 * 
	 * Add a solution to the population based if equality restrictions
	 * 
	 * @param iSolution
	 */
	public boolean add(ASolution<Integer> iSolution) {
		if ((!this.ALLOWEQUALGENOME || this.ALLOWEQUALFITNESS) && contains(iSolution)) {
			return false;
		} else {
			data.add((IntegerSolution) iSolution);
			firePopulationChanged();
			return true;
		}
	}
	
	
	
	public void add(OSPFWeights weights){
		List<Integer> w  = Arrays.stream( weights.asIntArray() ).boxed().collect( Collectors.toList() );
		IntegerSolution solution = new IntegerSolution(w,this.numberOfObjectives);
		data.add(solution);
		firePopulationChanged();
	}
	

	/**
	 **/

	public boolean contains(ASolution<Integer> iSolution) {
		int count = 0;
		Iterator<IntegerSolution> it = data.iterator();
		while (it.hasNext() && !it.next().equals(iSolution))
			count++;
		if (count < data.size())
			return true;
		else
			return false;
	}

	/**
	 * verify if two solutions are considered equal based on Objectives Fitness
	 * equality and Genome equality
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
	// TODO
	public boolean isEqual(IntegerSolution s1, IntegerSolution s2) {
		return s1.equals(s2);
	}

	/**
	 * Adds a collection of solutions to this population.
	 * 
	 * @param it
	 *            the collection of solutions to be added
	 * @return {@code true} if the population was modified as a result of this
	 *         method; {@code false} otherwise
	 */

	public Iterator<IntegerSolution> iterator() {

		return this.data.iterator();
	}

	public void addAll(Iterator<IntegerSolution> it) {

		while (it.hasNext())
			add(it.next());
		firePopulationChanged();
	}

	/**
	 * 
	 * @param size
	 * @return a list of the p lower solutions ordered by dominance.
	 */
	public List<IntegerSolution> getLowestValuedSolutions(int size) {
		Collections.sort(data, new FitnessComparator());
		return data.subList(0, size);
	}

	/**
	 * 
	 * @param objectiveIndex Objective Index
	 * @param numberOfSolutions Number of solutions
	 * @return a list of n solutions with the lowest fitness value for an objective 
	 */
	public List<IntegerSolution> getLowestValuedSolutions(int objectiveIndex, int numberOfSolutions) {
		Collections.sort(data, new SingleObjectiveComparator(objectiveIndex));
		return data.subList(0, numberOfSolutions);
	}
	
	
	public List<IntegerSolution> getLowestTradeOffSolutions(double tradeoff) {
		Collections.sort(data, new TradeOffComparator(tradeoff));
		return data;
	}
	
	public List<IntegerSolution> getLowestFitnessSolutions(int p) {
		Collections.sort(data, new FitnessComparator());
		return data.subList(0, p);
	}
	
	public int getNumberOfObjectives() {
		return this.numberOfObjectives;
	}

	public double[][] getPopulation() {
		return this.getParetoMatrixTranspose();
	}

	public boolean isAllowDuplicateSolutions() {
		return ALLOWEQUALGENOME;
	}

	public void setAllowDuplicateSolutions(boolean allowDuplicateSolutions) {
		this.ALLOWEQUALGENOME = allowDuplicateSolutions;
	}

	public void addAll(ASolutionSet<Integer> solutionSet) {
		int n = solutionSet.getNumberOfSolutions();
		numberOfObjectives = solutionSet.getNumberOfObjectives();
		for (int i = 0; i < n; i++)
			add(solutionSet.getSolution(i));
		firePopulationChanged();
	}

	public boolean isAllowEqualFitnessSolutions() {
		return ALLOWEQUALFITNESS;
	}

	public void setAllowEqualFitnessSolutions(boolean allowEqualFitnessSolutions) {
		this.ALLOWEQUALFITNESS = allowEqualFitnessSolutions;
	}
	
	
	public void addPopulationChangedListener(PopulationChangedListener listener){
		this.listeners.add(listener);
	}
	
	
	private void firePopulationChanged(){
		Iterator<PopulationChangedListener> it=listeners.iterator();
		while(it.hasNext())
			it.next().populationChanged();
	}
	
	
	public void addType(SolutionType type){
		this.types.add(type);
	}
	
	public SolutionType getType(int index){
		return this.types.get(index);
	}
	
	public int getNumberOfTypes(){
		return this.types.size();
	}

	
	public int[] getNodeTypes(int index) throws NullPointerException{
		
		Iterator<SolutionType> it=types.iterator();
		while(it.hasNext()){
			SolutionType next = it.next();
			if(next.getEncoding().equals(Encoding.NODETYPES)){
				int[] var = data.get(index).getVariablesArray();
				return Arrays.copyOfRange(var,next.getStartPosition(),next.getEndPosition());
			}
		}
		throw new NullPointerException();
	}
	
	
	public int[] getWeights(int index) throws NullPointerException{
		if(types.size()==0){
			return data.get(index).getVariablesArray();
		}else{
			Iterator<SolutionType> it=types.iterator();
			while(it.hasNext()){
				SolutionType next = it.next();
				if(next.getEncoding().equals(Encoding.WEIGHTS)){
					int[] var = data.get(index).getVariablesArray();
					return Arrays.copyOfRange(var,next.getStartPosition(),next.getEndPosition());
				}
			}
		}
		throw new NullPointerException();
	}
	
	
	public class SolutionType{
		
		private int startPosition;
		private int endPosition;
		private Encoding encoding;
		
		public SolutionType(int from, int to, Encoding encoding){
			this.setStartPosition(from);
			this.setEndPosition(to);
			this.setEncoding(encoding);
		}

		public int getStartPosition() {
			return startPosition;
		}

		public void setStartPosition(int startPosition) {
			this.startPosition = startPosition;
		}

		public int getEndPosition() {
			return endPosition;
		}

		public void setEndPosition(int endPosition) {
			this.endPosition = endPosition;
		}

		public Encoding getEncoding() {
			return encoding;
		}

		public void setEncoding(Encoding encoding) {
			this.encoding = encoding;
		}
	}

	
}
