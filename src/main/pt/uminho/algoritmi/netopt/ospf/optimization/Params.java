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
package pt.uminho.algoritmi.netopt.ospf.optimization;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import pt.uminho.algoritmi.netopt.ospf.simulation.OSPFWeights;
import pt.uminho.algoritmi.netopt.ospf.simulation.PValues;
import pt.uminho.algoritmi.netopt.ospf.simulation.Population;
import pt.uminho.algoritmi.netopt.ospf.simulation.Simul.LoadBalancer;


/**
 * Optimization parameters
 */

//TODO: Convert to Property bean???
public class Params {
	
	

	public enum  AlgorithmSelectionOption{
		SOEA("Single Objective"),NSGAII("NSGAII"),SPEA2("SPEA2");
		private final String name;
		AlgorithmSelectionOption(String name){
			this.name=name;
		}
		
		public String toString(){
			return name;
		}
	}

	
	
	public enum  AlgorithmSecondObjective{
		DEMANDS,DELAY,MLU,ALU;
	}
	

	
	public enum TerminationCriteria {
		ITERATION("Iteration"), FITNESS("Fitness");
		private final String name;
		
		TerminationCriteria(String name){
			this.name=name;
		}
		
		public String toString(){
			return name;
		}
	}


	
	
	public static enum  EdgeSelectionOption{
		
		HIGHERLOAD("Higher Total Load"),
		HIGHERDIRECTLOAD("Higher Direct Load"),
		MOSTUSEDPATH("Higher Centrality"),
		MOSTDIRECTUSEDRACIO("Higher Direct Used Racio"),
		HIGHERFAILUREFITNESSIMPACT("Failure Higher Fitness Impact "),
		USERSELECTED("User Selected"), 
		ALLEDGES("All Edges");
	
		private final String name;
		
		EdgeSelectionOption(String name){
			this.name=name;
		}
		public String toString(){
			return name;
		}
	}

	
	// variables
	
	
	private double alfa;
	private double beta;
	int populationSize;
	int archiveSize;
	int numberGenerations;

	/**
	 * Previous population
	 */
	private Population population;
	/**
	 * Percentage of previous population used
	 */
	private double percentage;	
	/**
	 *  stop criteria value 
	 */
	private double criteriaValue;
	private TerminationCriteria criteria;
	private OSPFWeights previousWeights;
	private boolean applyWeightsPenalties;
	private AlgorithmSecondObjective secondObjective;
	private boolean useInvCap;
	private boolean useL2;
	private boolean useUnit;
	private boolean linkFailure;
	private LoadBalancer loadBalancer; 
	private String loadBalancerFunction;
	private EdgeSelectionOption edgeSelectionOption;
	private ArrayList<Integer> edgeFailureId;
	private int numberSDNNodes;
	private PValues pvalues;
	private double[][] initialLoads;
	
	public Params()
	{
		this.alfa=0.5;
		this.beta=1;
		this.populationSize=100;
		this.archiveSize=100;
		this.numberGenerations = 200;		
		this.percentage=0.0;
		this.setCriteriaValue(0.0);
		this.setCriteria(TerminationCriteria.ITERATION);
		this.setSecondObjective(AlgorithmSecondObjective.DEMANDS);
		this.edgeFailureId=new ArrayList<Integer>();
		this.loadBalancer=LoadBalancer.ECMP;
		this.previousWeights=null;
		this.pvalues=null;
	}

	
	
	public double getAlfa() {
		return alfa;
	}

	public void setAlfa(double alfa) {
		this.alfa = alfa;
	}

	public int getPopulationSize() {
		return populationSize;
	}

	public void setPopulationSize(int populationSize) {
		this.populationSize = populationSize;
	}

	public int getArchiveSize() {
		return archiveSize;
	}

	public void setArchiveSize(int archiveSize) {
		this.archiveSize = archiveSize;
	}

	public int getNumberGenerations() {
		return numberGenerations;
	}

	public void setNumberGenerations(int numberGenerations) {
		this.numberGenerations = numberGenerations;
	}
	
	
	public boolean hasInitialPopulation(){	
		return (this.percentage>0 && this.population!=null && this.population.getNumberOfSolutions()>0);
	}
	
	
	public void setPValues(PValues pvalues){
		this.pvalues=pvalues;
	}
	
	
	public PValues getPValues(){
		return this.pvalues;
	}
	
	
	@Override
	public String toString()
	{
		int count=0;
		if(this.useInvCap)count++;
		if(this.useL2)count++;
		if(this.useUnit)count++;
		
		StringBuffer str = new StringBuffer();
		str.append("Alfa: "+alfa+"\n");
		str.append("Load Balancer: "+this.loadBalancer.getName()+"\n");
		str.append("Population: "+(populationSize+count)+"\n");
		str.append("Archive: "+archiveSize+"\n");
		str.append("Generations: "+numberGenerations+"\n");
		str.append("Initial Population: "+this.hasInitialPopulation()+"\n");
		str.append("Initial Population: "+percentage+"%\n");
		str.append("InvCap in Population: "+this.useInvCap+"\n");
		str.append("L2 in Population: "+this.useL2+"\n");
		str.append("Unit in Population: "+this.useUnit);
		return str.toString();
	}

	public void setInitialPopulation(Population population) {
		this.population=population;
	}
	
	public Population getInitialPopulation()
	{
		return this.population;
	}

	public void setInitialPopulationPercentage(double percentage) {
		this.percentage=percentage;
	}
	
	public double getInitialPopulationPercentage() {
		return this.percentage;
	}

	public TerminationCriteria getCriteria() {
		return criteria;
	}

	public void setCriteria(TerminationCriteria criteria) {
		this.criteria = criteria;
	}

	public double getCriteriaValue() {
		return criteriaValue;
	}

	public void setCriteriaValue(double criteriaValue) {
		this.criteriaValue = criteriaValue;
	}

	public OSPFWeights getPreviousWeights() {
		return previousWeights;
	}

	public void setPreviousWeights(OSPFWeights previousWeights) {
		this.previousWeights = previousWeights;
	}

	public boolean isApplyWeightsPenalties() {
		return applyWeightsPenalties;
	}

	public void setApplyWeightsPenalties(boolean applyWeightsPenalties) {
		this.applyWeightsPenalties = applyWeightsPenalties;
	}

	public AlgorithmSecondObjective getSecondObjective() {
		return secondObjective;
	}

	public void setSecondObjective(AlgorithmSecondObjective objective) {
		this.secondObjective = objective;
	}

	public void setUseInvCap(boolean invCap) {
		this.useInvCap=invCap;
	}
	
	public boolean getUseInvCap() {
		return this.useInvCap;
	}

	public void setUseL2(boolean l2) {
		this.useL2=l2;
	}

	public void setUseUnit(boolean unit) {
		this.useUnit=unit;
	}
	

	public boolean getUseL2() {
		return this.useL2;
	}

	public boolean getUseUnit() {
		return this.useUnit;
	}

	public double getBeta() {
		return this.beta;
	}
	
	public void setBeta(double beta)
	{
			this.beta=beta;
	}

	public boolean isLinkFailure() {
		return linkFailure;
	}

	public void setLinkFailure(boolean linkFailure) {
		this.linkFailure = linkFailure;
	}

	public EdgeSelectionOption getEdgeSelectionOption() {
		return edgeSelectionOption;
	}

	public void setEdgeSelectionOption(EdgeSelectionOption edgeSelectionOption) {
		this.edgeSelectionOption = edgeSelectionOption;
	}

	public int[] getEdgeFailureId() {
		return convertIntegers(edgeFailureId);
	}

	public void addEdgeFailureId(int edgeFailureId) {
		this.edgeFailureId.add(edgeFailureId);
	}
	


	public LoadBalancer getLoadBalancer() {
		return loadBalancer;
	}



	public void setLoadBalancer(LoadBalancer loadBalancer) {
		this.loadBalancer = loadBalancer;
	}

	
	
	public static int[] convertIntegers(List<Integer> integers)
	{
	    int[] ret = new int[integers.size()];
	    Iterator<Integer> iterator = integers.iterator();
	    for (int i = 0; i < ret.length; i++)
	    {
	        ret[i] = iterator.next().intValue();
	    }
	    return ret;
	}



	public void setLoadBalancerFunction(String loadBalancerf) {
		this.loadBalancerFunction=loadBalancerf;
	}
	
	
	public String getLoadBalancerFunction() {
		return this.loadBalancerFunction;
	}



	public void setNumberSDNNodes(int n) {
		this.numberSDNNodes =n;
	}
	
	public int getNumberSDNNodes() {
		return  this.numberSDNNodes;
	}

	public Params copy(){
		Params p= new Params();
		p.setAlfa(this.alfa);
		p.setApplyWeightsPenalties(this.applyWeightsPenalties);
		p.setArchiveSize(this.archiveSize);
		p.setBeta(this.beta);
		p.setCriteria(this.criteria);
		p.setEdgeSelectionOption(this.edgeSelectionOption);
		p.setInitialPopulation(this.population);
		p.setInitialPopulationPercentage(this.percentage);
		p.setLinkFailure(linkFailure);
		p.setLoadBalancerFunction(this.loadBalancerFunction);
		p.setNumberGenerations(numberGenerations);
		p.setNumberSDNNodes(numberSDNNodes);
		p.setPopulationSize(populationSize);
		p.setPreviousWeights(previousWeights);
		p.setSecondObjective(secondObjective);
		p.setUseInvCap(this.useInvCap);
		p.setUseL2(useL2);
		p.setUseUnit(useUnit);
		p.setPValues(pvalues);
		p.setInitialLoads(getInitialLoads());
		return p;
	}



	public double[][] getInitialLoads() {
		return initialLoads;
	}



	public void setInitialLoads(double[][] initialLoads) {
		this.initialLoads = initialLoads;
	}

}
