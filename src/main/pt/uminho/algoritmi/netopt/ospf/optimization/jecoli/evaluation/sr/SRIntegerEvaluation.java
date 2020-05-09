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



import java.util.List;
import java.util.stream.Collectors;

import jecoli.algorithm.components.evaluationfunction.AbstractEvaluationFunction;
import jecoli.algorithm.components.evaluationfunction.IEvaluationFunction;
import jecoli.algorithm.components.evaluationfunction.InvalidEvaluationFunctionInputDataException;
import jecoli.algorithm.components.representation.linear.ILinearRepresentation;
import pt.uminho.algoritmi.netopt.SystemConf;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.evaluation.EvaluationType;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.evaluation.ospf.OSPFIntegerEvaluationMO;
import pt.uminho.algoritmi.netopt.ospf.simulation.DelayRequests;
import pt.uminho.algoritmi.netopt.ospf.simulation.Demands;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.Simul.LoadBalancer;
import pt.uminho.algoritmi.netopt.ospf.simulation.exception.DimensionErrorException;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetNode.NodeType;
import pt.uminho.algoritmi.netopt.ospf.simulation.simulators.ISimulator;
import pt.uminho.algoritmi.netopt.ospf.simulation.simulators.SRMultiDemandSimul;
import pt.uminho.algoritmi.netopt.ospf.simulation.simulators.SRSimul;


public class SRIntegerEvaluation extends AbstractEvaluationFunction<ILinearRepresentation<Integer>> 
{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	protected NetworkTopology topology;
	protected Demands[] demands;
	
	private double alpha;
	private DelayRequests delays;
	private EvaluationType type;
	protected LoadBalancer loadBalancer;
	protected boolean hybrid;
	/**
	 * Penalty factor to be applied in hybrid/IP SR optimization
	 * Penalizes solutions whose SP are not closed in the SR island
	 */
	double HYBRID_PENALTY_FACTOR= 10.0;
	
		
	public SRIntegerEvaluation (NetworkTopology topology, Demands[] demands){
		this(topology,demands,true);
	}
	
	public SRIntegerEvaluation (NetworkTopology topology, Demands[] demands,boolean hybrid)
	{
		super(false); //sets isMaximization
		this.topology = topology;
		this.demands = demands;
		this.alpha=0.5;
		this.setDelays(null);
		this.setType(EvaluationType.TWO_DEMANDS);
		this.loadBalancer= LoadBalancer.ECMP;
		this.hybrid= hybrid;
		this.HYBRID_PENALTY_FACTOR = SystemConf.getPropertyDouble("sr.hybridpenalty",10.0);
		if(topology.getNetGraph().getNodesByType(NodeType.SDN_SR).size()==topology.getDimension() || HYBRID_PENALTY_FACTOR==0)
			hybrid=false;
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
		// for hybrid IP/SR networks
		ISimulator s;
		if(this.type.equals(EvaluationType.DEMANDS_DELAY)){
			SRSimul simul = new SRSimul(topology,this.loadBalancer);
			fitness= simul.evalWeights(weights, this.alpha, this.demands[0], this.delays);
			s=simul;
		}
		else if(this.type.equals(EvaluationType.TWO_DEMANDS)){
			SRMultiDemandSimul simul = new SRMultiDemandSimul(topology,this.loadBalancer);
			fitness= simul.evalWeights(weights, this.alpha, this.demands[0], this.demands[1]);
			s=simul;
		}else if(this.type.equals(EvaluationType.DEMANDS_ALU)){
			SRMultiDemandSimul simul = new SRMultiDemandSimul(topology,this.loadBalancer);
			fitness= simul.evalWeightsALU(weights, this.alpha, this.demands[0]);
			s=simul;
		}
		else{
			SRMultiDemandSimul simul = new SRMultiDemandSimul(topology,this.loadBalancer);
			fitness= simul.evalWeightsMLU(weights, this.alpha, demands[0]);
			s=simul;
		}
		
		/**
		 * Penalty is computed in the ratio of nodes on SP between SR node which are not
		 * also SR nodes.
		 */
		if(hybrid){
			int counter=0;
			List<Integer> srNodes=s.getTopology().getNetGraph().getNodesByType(NodeType.SDN_SR)
					.stream()
					.map(elt -> elt.getNodeId())
					.collect(Collectors.toList());
			
			for(Integer src:srNodes){
				for(Integer dst:srNodes){
					if(!src.equals(dst)){
						List<Integer> nodesOnPath=s.getTopology().getShortestPathGraph().getNodesOnShortestPaths(src, dst);
						for(Integer n:nodesOnPath)
							if(!srNodes.contains(n))
								counter++;
					}
				}
			}
			double p=(double)counter/s.getTopology().getDimension();
			//apply penalty
			fitness+=(p*HYBRID_PENALTY_FACTOR);
		}
		
		return fitness;
	}

	@Override
	public void verifyInputData()
			throws InvalidEvaluationFunctionInputDataException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public SRIntegerEvaluation deepCopy(){
		Demands[] d = new Demands[demands.length];
		for(int i=0; i<demands.length;i++)
			d[i]=demands[i].copy();
		SRIntegerEvaluation e = new SRIntegerEvaluation(this.topology.copy(),d,this.hybrid);
		e.setLoadBalancer(this.loadBalancer);
		return e;
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
