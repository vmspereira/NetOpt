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
package pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.evaluation.sr;



import jecoli.algorithm.components.evaluationfunction.AbstractMultiobjectiveEvaluationFunction;
import jecoli.algorithm.components.evaluationfunction.IEvaluationFunction;
import jecoli.algorithm.components.evaluationfunction.InvalidEvaluationFunctionInputDataException;
import jecoli.algorithm.components.representation.linear.ILinearRepresentation;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.evaluation.EvaluationType;
import pt.uminho.algoritmi.netopt.ospf.simulation.DelayRequests;
import pt.uminho.algoritmi.netopt.ospf.simulation.Demands;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.Simul.LoadBalancer;
import pt.uminho.algoritmi.netopt.ospf.simulation.exception.DimensionErrorException;
import pt.uminho.algoritmi.netopt.ospf.simulation.simulators.SRMultiDemandSimul;
import pt.uminho.algoritmi.netopt.ospf.simulation.simulators.SRSimul;



public class SRIntegerEvaluationMO extends AbstractMultiobjectiveEvaluationFunction<ILinearRepresentation<Integer>> 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	
	NetworkTopology topology;
	Demands[] demands;
	DelayRequests delays;
	EvaluationType type;
	LoadBalancer loadBalancer;
	
	
	public SRIntegerEvaluationMO (NetworkTopology topology, Demands[] demands, DelayRequests delays,EvaluationType type2)
	{
		super(false); //sets isMaximization
		this.topology = topology;
		this.demands = demands;
		this.delays = delays;
		this.type=type2;
		//default load balancer
		this.loadBalancer= LoadBalancer.DEFT;
	}
	
	@Override
	public Double[] evaluateMO(ILinearRepresentation<Integer> solution) throws DimensionErrorException {
		Double[] resultList =  new Double[2];
			
		int[] weights = decode(solution);
		double[] fitness = evalWeightsMO(weights);
	        
		resultList[0]=new Double(fitness[0]);
		resultList[1]=new Double(fitness[1]);
		
		return resultList;
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
	
	
	
	protected double[] evalWeightsMO (int[] weights) throws DimensionErrorException
	{
		
		if(type.equals(EvaluationType.DEMANDS_DELAY))
		{
			SRSimul simul = new SRSimul(topology,this.loadBalancer);
			return simul.evalWeightsMO(weights, true, true, true, demands[0], delays);
		}
		else if(type.equals(EvaluationType.DEMANDS_MLU))
		{
			SRMultiDemandSimul simul = new SRMultiDemandSimul(topology,this.loadBalancer);
			return simul.evalWeightsMOMLU(weights, demands[0]);
		}
		else if(type.equals(EvaluationType.DEMANDS_ALU))
		{
			SRMultiDemandSimul simul = new SRMultiDemandSimul(topology,this.loadBalancer);
			return simul.evalWeightsMOALU(weights, demands[0]);
		}
		else
		{ 
			SRMultiDemandSimul simul =  new SRMultiDemandSimul(topology,this.loadBalancer);
			return simul.evalWeightsMO(weights, true, true, true, demands[0], demands[1]);
		}
	}

	@Override
	public void verifyInputData()
			throws InvalidEvaluationFunctionInputDataException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IEvaluationFunction<ILinearRepresentation<Integer>> deepCopy()
			throws Exception {
		Demands[] d = new Demands[demands.length];
		for(int i=0; i<demands.length;i++)
			d[i]=demands[i].copy();
		return new SRIntegerEvaluationMO (topology.copy(), d, delays.copy(), type);
	}

	@Override
	public int getNumberOfObjectives() {
		return 2;
	}


	public void setLoadBalancer(LoadBalancer lb){
		this.loadBalancer=lb;
	}
}
