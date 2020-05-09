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
package pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.evaluation.ospf;



import jecoli.algorithm.components.evaluationfunction.AbstractEvaluationFunction;
import jecoli.algorithm.components.evaluationfunction.IEvaluationFunction;
import jecoli.algorithm.components.evaluationfunction.InvalidEvaluationFunctionInputDataException;
import jecoli.algorithm.components.representation.linear.ILinearRepresentation;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.evaluation.EvaluationType;
import pt.uminho.algoritmi.netopt.ospf.simulation.DelayRequests;
import pt.uminho.algoritmi.netopt.ospf.simulation.Demands;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.Simul;
import pt.uminho.algoritmi.netopt.ospf.simulation.Simul.LoadBalancer;
import pt.uminho.algoritmi.netopt.ospf.simulation.exception.DimensionErrorException;
import pt.uminho.algoritmi.netopt.ospf.simulation.simulators.IMultiDemandSimulator;
import pt.uminho.algoritmi.netopt.ospf.simulation.simulators.ISimulator;
import pt.uminho.algoritmi.netopt.ospf.simulation.simulators.MultiDemandsSimul;
import pt.uminho.algoritmi.netopt.ospf.simulation.simulators.PDEFTSimul;


public class OSPFIntegerEvaluation extends AbstractEvaluationFunction<ILinearRepresentation<Integer>> 
{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	
	
	protected NetworkTopology topology;
	protected Demands[] demands;
	
	private double alpha;
	private DelayRequests delays;
	//private OSPFWeights previousWeights;
	//private boolean applyWeightsPenalties;
	private EvaluationType type;
	protected LoadBalancer loadBalancer;
	
		
	public OSPFIntegerEvaluation (NetworkTopology topology, Demands[] demands)
	{
		super(false); //sets isMaximization
		this.topology = topology;
		this.demands = demands;
		this.alpha=0.5;
		this.setDelays(null);
		this.setType(EvaluationType.TWO_DEMANDS);
		this.loadBalancer= LoadBalancer.ECMP;
	}
	
	
	
	
	@Override
	public double evaluate(ILinearRepresentation<Integer> solution) throws DimensionErrorException {	
		int[] weights = decode(solution);
		double fitness = evalWeights(weights);
		return fitness;
	}
	
	protected int[] decode (ILinearRepresentation<Integer> solution)
	{
		int[] res = new int[solution.getNumberOfElements()];
		for(int i=0; i < solution.getNumberOfElements(); i++)
		{
			res[i] = solution.getElementAt(i);
		}
		return res;
	}
	
	protected double evalWeights(int[] weights) throws DimensionErrorException
	{
		
		double fitness=0.0;
		if(this.type.equals(EvaluationType.DEMANDS_DELAY)){
			ISimulator simul;
			if(loadBalancer==LoadBalancer.DEFT)
				simul= new PDEFTSimul(topology,true);
			else 
				if(loadBalancer==LoadBalancer.PEFT)
					simul= new PDEFTSimul(topology,false);
			else{
				simul= new Simul(topology);
				simul.setLoadBalancer(loadBalancer);
			}
			
			fitness= simul.evalWeights(weights, this.alpha, this.demands[0], this.delays);
		}
		else if(this.type.equals(EvaluationType.TWO_DEMANDS)){
			IMultiDemandSimulator simul;
			if(loadBalancer==LoadBalancer.DEFT)
				simul= new PDEFTSimul(topology,true);
			else 
				if(loadBalancer==LoadBalancer.PEFT)
					simul= new PDEFTSimul(topology,false);
			else
				simul=  new MultiDemandsSimul(topology);
			
			fitness= simul.evalWeights(weights, this.alpha, this.demands[0], this.demands[1]);
		}else if(this.type.equals(EvaluationType.DEMANDS_MLU)){
			IMultiDemandSimulator simul;
			if(loadBalancer==LoadBalancer.DEFT)
				simul= new PDEFTSimul(topology,true);
			else 
				if(loadBalancer==LoadBalancer.PEFT)
					simul= new PDEFTSimul(topology,false);
			else
				simul=  new MultiDemandsSimul(topology);
			
			fitness= simul.evalWeightsMLU(weights, this.alpha, this.demands[0]);	
		}else if(this.type.equals(EvaluationType.DEMANDS_ALU)){
			IMultiDemandSimulator simul;
			if(loadBalancer==LoadBalancer.DEFT)
				simul= new PDEFTSimul(topology,true);
			else 
				if(loadBalancer==LoadBalancer.PEFT)
					simul= new PDEFTSimul(topology,false);
			else
				simul=  new MultiDemandsSimul(topology);
			fitness= simul.evalWeightsALU(weights, this.alpha, this.demands[0]);	
		}
		
		
		
		// Apply a factor as penalty to deviation from previously obtained solution
		// TODO Verify if it's useful
		/*
		if(this.applyWeightsPenalties && this.previousWeights!=null){
			OSPFWeights w = new OSPFWeights(this.topology.getDimension());
			w.setWeights(weights, topology);
			SPComparator sp=new SPComparator(this.topology.getNetGraph(),this.previousWeights,w);
			double penalty=sp.getPathChangePenalty();
			return fitness*(1+penalty);		
		}
		*/
		return fitness;
	}

	@Override
	public void verifyInputData()
			throws InvalidEvaluationFunctionInputDataException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public OSPFIntegerEvaluation deepCopy(){
		Demands[] d = new Demands[demands.length];
		for(int i=0; i<demands.length;i++)
			d[i]=demands[i].copy();
		return new OSPFIntegerEvaluation(this.topology.copy(),d);
	}
	
	@Override
	public int getNumberOfObjectives() {
		return 1;
	}
	
	
	public void setLoadBalancer(LoadBalancer lb){
		this.loadBalancer=lb;
	}


	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}


	public void setDelays(DelayRequests delays) {
		this.delays = delays;
	}


	
	public void setType(EvaluationType type) {
		this.type = type;
	}
	

}
