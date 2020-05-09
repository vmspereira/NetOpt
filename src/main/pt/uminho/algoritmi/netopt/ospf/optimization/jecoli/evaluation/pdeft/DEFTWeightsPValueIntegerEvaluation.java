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
package pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.evaluation.pdeft;



import jecoli.algorithm.components.evaluationfunction.AbstractEvaluationFunction;
import jecoli.algorithm.components.evaluationfunction.IEvaluationFunction;
import jecoli.algorithm.components.evaluationfunction.InvalidEvaluationFunctionInputDataException;
import jecoli.algorithm.components.representation.linear.ILinearRepresentation;
import pt.uminho.algoritmi.netopt.SystemConf;
import pt.uminho.algoritmi.netopt.ospf.simulation.Demands;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.exception.DimensionErrorException;
import pt.uminho.algoritmi.netopt.ospf.simulation.simulators.PDEFTSimul;

/**
 * Each solution encodes p values (int/divider) on the |n| first positions
 * and link weights on the |n|+1 to |n|+|a| positions.
 **/

public class DEFTWeightsPValueIntegerEvaluation extends AbstractEvaluationFunction<ILinearRepresentation<Integer>> 
{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	protected NetworkTopology topology;
	protected Demands demands;
	protected boolean USE_DEFT=true;
	private double divider =100;
	
		
	public DEFTWeightsPValueIntegerEvaluation (NetworkTopology topology, Demands demand)
	{
		super(false); //sets isMaximization
		this.topology = topology;
		this.demands = demand;
		divider=SystemConf.getPropertyInt("pvalue.divider",100);
	}
	
	public DEFTWeightsPValueIntegerEvaluation (NetworkTopology topology, Demands demand,boolean use_deft)
	{
		super(false); //sets isMaximization
		this.topology = topology;
		this.demands = demand;
		this.USE_DEFT=use_deft;
		divider=SystemConf.getPropertyInt("pvalue.divider",100);
	}
	
	
	
	@Override
	public double evaluate(ILinearRepresentation<Integer> solution) throws DimensionErrorException {
		if(solution.getNumberOfElements()!=(topology.getDimension()+topology.getNumberEdges()))
			throw new DimensionErrorException();
		
		double[] pvalues = decodePValues(solution);
		int[] weights = decodeWeights(solution);
		double fitness = evalPValues(pvalues,weights);
		return fitness;
	}
	
	
	
	protected double[] decodePValues (ILinearRepresentation<Integer> solution)
	{
		int n=topology.getDimension();
		double[] res = new double[n];
		for(int i=0; i < n; i++)
		{
			res[i] = ((double)solution.getElementAt(i)+1)/divider;
		}
		return res;
	}
	
	protected int[] decodeWeights (ILinearRepresentation<Integer> solution)
	{
		int n=topology.getDimension();
		int m =topology.getNumberEdges();
		int t= m+n;
		int[] res = new int[m];
		for(int i=n; i < t; i++)
		{
			res[i-n] = solution.getElementAt(i);
		}
		return res;
	}
	
	
	protected double evalPValues(double[] pValues,int[] weights) throws DimensionErrorException
	{
		double fitness=0.0;
		PDEFTSimul simul = new PDEFTSimul(topology,USE_DEFT);
		fitness= simul.evalPValues(pValues,weights,this.demands);
		return fitness;
	}

	@Override
	public void verifyInputData()
			throws InvalidEvaluationFunctionInputDataException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IEvaluationFunction<ILinearRepresentation<Integer>> deepCopy()
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int getNumberOfObjectives() {
		return 1;
	}
	
	
	
}
