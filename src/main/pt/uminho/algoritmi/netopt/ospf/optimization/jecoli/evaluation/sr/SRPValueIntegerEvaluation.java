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



import jecoli.algorithm.components.evaluationfunction.AbstractEvaluationFunction;
import jecoli.algorithm.components.evaluationfunction.IEvaluationFunction;
import jecoli.algorithm.components.evaluationfunction.InvalidEvaluationFunctionInputDataException;
import jecoli.algorithm.components.representation.linear.ILinearRepresentation;
import pt.uminho.algoritmi.netopt.SystemConf;
import pt.uminho.algoritmi.netopt.ospf.simulation.Demands;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.Simul.LoadBalancer;
import pt.uminho.algoritmi.netopt.ospf.simulation.exception.DimensionErrorException;
import pt.uminho.algoritmi.netopt.ospf.simulation.simulators.SRSimul;


public class SRPValueIntegerEvaluation extends AbstractEvaluationFunction<ILinearRepresentation<Integer>> 
{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	protected NetworkTopology topology;
	protected Demands demands;
	protected int[] weights;
	protected LoadBalancer loadBalancer;
	protected double divider = 100; 
	
		
	public SRPValueIntegerEvaluation (NetworkTopology topology, Demands demand, int[] weights)
	{
		super(false); //sets isMaximization
		this.topology = topology;
		this.demands = demand;
		this.weights = weights;
		this.loadBalancer= LoadBalancer.ECMP;
		divider=SystemConf.getPropertyInt("pvalue.divider",100);
	}
	
	
	
	
	@Override
	public double evaluate(ILinearRepresentation<Integer> solution) throws DimensionErrorException {
		if(solution.getNumberOfElements()!=topology.getDimension())
			throw new DimensionErrorException();
		double[] pvalues = decode(solution);
		double fitness = evalPValues(pvalues);
		return fitness;
	}
	
	
	
	protected double[] decode (ILinearRepresentation<Integer> solution)
	{
		double[] res = new double[solution.getNumberOfElements()];
		for(int i=0; i < solution.getNumberOfElements(); i++)
		{
			res[i] = solution.getElementAt(i)/divider;
		}
		return res;
	}
	
	protected double evalPValues(double[] pValues) throws DimensionErrorException
	{

		double fitness=0.0;
		SRSimul simul = new SRSimul(topology,this.loadBalancer);
		fitness= simul.evalPValues(pValues,weights,this.demands);
		return fitness;
	}

	@Override
	public void verifyInputData()
			throws InvalidEvaluationFunctionInputDataException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public SRPValueIntegerEvaluation deepCopy()
			throws Exception {
		
		SRPValueIntegerEvaluation e = new SRPValueIntegerEvaluation(this.topology.copy(),demands.copy(),this.weights.clone());
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
	
}
