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

import java.io.Serializable;
import java.util.ArrayList;


import pt.uminho.algoritmi.netopt.ospf.simulation.AverageEndtoEndDelays;
import pt.uminho.algoritmi.netopt.ospf.simulation.DelayRequests;
import pt.uminho.algoritmi.netopt.ospf.simulation.Demands;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkLoads;
import pt.uminho.algoritmi.netopt.ospf.simulation.OSPFWeights;
import pt.uminho.algoritmi.netopt.ospf.simulation.PValues;
import pt.uminho.algoritmi.netopt.ospf.simulation.Population;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.ASolution;



public class ResultOptim implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<Demands> demands;
	private DelayRequests delayReqs;
	private ArrayList<OSPFWeights> weights;
	private ArrayList<NetworkLoads> loads;
	private ArrayList<AverageEndtoEndDelays> endToEndDelays;
	private ASolution<Integer> solution;
	private Population population;
	private PValues pvalues;

	public ResultOptim() {
		this.demands = new ArrayList<Demands>();
		this.delayReqs = null;
		this.weights = new ArrayList<OSPFWeights>();
		this.loads = new ArrayList<NetworkLoads>();
		this.endToEndDelays = new ArrayList<AverageEndtoEndDelays>();
		this.solution = null;
		this.population=null;
		this.pvalues=null;
	}

	public ASolution<Integer> getSolutions() {
		return solution;
	}

	public void setSolution(ASolution<Integer> solution) {
		this.solution = solution;
	}

	public void addNetworkLoads(NetworkLoads loads) {
		this.loads.add(loads);
	}

	public ArrayList<NetworkLoads> getNetworkLoads() {
		return loads;
	}

	public void addAverageEndtoEndDelays(AverageEndtoEndDelays endToEndDelays) {
		this.endToEndDelays.add(endToEndDelays);
	}

	public ArrayList<AverageEndtoEndDelays> getAverageEndtoEndDelays() {
		return this.endToEndDelays;
	}

	
		
	public void addDemands(Demands demands) {
		this.demands.add(demands);
	}

	public ArrayList<Demands> getDemands() {
		return demands;
	}

	public void setDelayReqs(DelayRequests delayReqs) {
		this.delayReqs = delayReqs;
	}

	public DelayRequests getDelayReqs() {
		return delayReqs;
	}

	public void addWeights(OSPFWeights weights) {
		this.weights.add(weights);
	}

	public ArrayList<OSPFWeights> getWeights() {
		return weights;
	}

	public void setEndToEndDelays(AverageEndtoEndDelays endToEndDelays) {
		this.endToEndDelays.add(0,endToEndDelays);
	}

	public AverageEndtoEndDelays getEndToEndDelays() {
		return endToEndDelays.get(0);
	}
	
	public Population getPopulation(){
		return this.population;
	}
	
	public void setPopulation(Population solutionSet){
		this.population=solutionSet;
	}


	public void setPValues(PValues pvalues){
		this.pvalues=pvalues;
	}
	
	public PValues getPValues(){
		return this.pvalues;
	}
}
