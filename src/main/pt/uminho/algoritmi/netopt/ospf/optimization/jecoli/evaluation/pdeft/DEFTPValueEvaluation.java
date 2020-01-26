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
import pt.uminho.algoritmi.netopt.ospf.simulation.Demands;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.exception.DimensionErrorException;
import pt.uminho.algoritmi.netopt.ospf.simulation.simulators.PDEFTSimul;


public class DEFTPValueEvaluation extends AbstractEvaluationFunction<ILinearRepresentation<Double>> 
{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	protected NetworkTopology topology;
	protected Demands demands;
	protected int[] weights;
	protected boolean USE_DEFT=true;
	
		
	public DEFTPValueEvaluation (NetworkTopology topology, Demands demand, int[] weights)
	{
		super(false); //sets isMaximization
		this.topology = topology;
		this.demands = demand;
		this.weights = weights;
	}
	
	public DEFTPValueEvaluation (NetworkTopology topology, Demands demand, int[] weights,boolean use_deft)
	{
		super(false); //sets isMaximization
		this.topology = topology;
		this.demands = demand;
		this.weights = weights;
		this.USE_DEFT=use_deft;
	}
	
	
	
	@Override
	public double evaluate(ILinearRepresentation<Double> solution) throws DimensionErrorException {
		if(solution.getNumberOfElements()!=topology.getDimension())
			throw new DimensionErrorException();
		double[] pvalues = decode(solution);
		double fitness = evalPValues(pvalues);
		return fitness;
	}
	
	
	
	protected double[] decode (ILinearRepresentation<Double> solution)
	{
		double[] res = new double[solution.getNumberOfElements()];
		for(int i=0; i < solution.getNumberOfElements(); i++)
		{
			res[i] = solution.getElementAt(i);
		}
		return res;
	}
	
	protected double evalPValues(double[] pValues) throws DimensionErrorException
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
	public IEvaluationFunction<ILinearRepresentation<Double>> deepCopy()
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int getNumberOfObjectives() {
		return 1;
	}
	
	
	
}
