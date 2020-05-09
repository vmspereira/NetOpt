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
import pt.uminho.algoritmi.netopt.SystemConf;
import pt.uminho.algoritmi.netopt.ospf.simulation.Demands;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.Simul.LoadBalancer;
import pt.uminho.algoritmi.netopt.ospf.simulation.exception.DimensionErrorException;
import pt.uminho.algoritmi.netopt.ospf.simulation.simulators.SRSimul;


public class SRPValueIntegerEvaluationMO extends AbstractMultiobjectiveEvaluationFunction<ILinearRepresentation<Integer>> 
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
	
	public SRPValueIntegerEvaluationMO (NetworkTopology topology, Demands demand, int[] weights)
	{
		this(topology,demand,weights,LoadBalancer.DEFT);
	}
	
	public SRPValueIntegerEvaluationMO (NetworkTopology topology, Demands demand, int[] weights,LoadBalancer lb)
	{
		super(false); //sets isMaximization
		this.topology = topology;
		this.demands = demand;
		this.weights = weights;
		this.loadBalancer= lb;
		divider=SystemConf.getPropertyInt("pvalue.divider",100);
	}
	
	
	
	
	@Override
	public Double[] evaluateMO(ILinearRepresentation<Integer> solution) throws DimensionErrorException {
		if(solution.getNumberOfElements()!=topology.getDimension())
			throw new DimensionErrorException();
		double[] pvalues = decode(solution);
		Double[] fitness = evalPValues(pvalues);
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
	
	protected Double[] evalPValues(double[] pValues) throws DimensionErrorException
	{
		double res1=0.0;
		double res2=0.0;
		SRSimul simul = new SRSimul(topology,this.loadBalancer);
		simul.computeLoads(weights, pValues, demands);
		res1= simul.getLoads().getCongestion();
		res2=simul.getLoads().getMLU();
		Double[] result = new Double[2];
		result[0] = res1;
		result[1] = res2;
		return result;
	}

	@Override
	public void verifyInputData()
			throws InvalidEvaluationFunctionInputDataException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IEvaluationFunction<ILinearRepresentation<Integer>> deepCopy()
			throws Exception {
		SRPValueIntegerEvaluationMO e = new SRPValueIntegerEvaluationMO(this.topology.copy(),demands.copy(),this.weights.clone());
		e.setLoadBalancer(this.loadBalancer);
		return e;
	}
	
	@Override
	public int getNumberOfObjectives() {
		return 2;
	}
	
	
	public void setLoadBalancer(LoadBalancer lb){
		this.loadBalancer=lb;
	}
	
}
