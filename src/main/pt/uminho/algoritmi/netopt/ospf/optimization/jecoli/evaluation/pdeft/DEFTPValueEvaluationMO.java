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



import jecoli.algorithm.components.evaluationfunction.AbstractMultiobjectiveEvaluationFunction;
import jecoli.algorithm.components.evaluationfunction.IEvaluationFunction;
import jecoli.algorithm.components.evaluationfunction.InvalidEvaluationFunctionInputDataException;
import jecoli.algorithm.components.representation.linear.ILinearRepresentation;
import pt.uminho.algoritmi.netopt.ospf.simulation.Demands;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.exception.DimensionErrorException;
import pt.uminho.algoritmi.netopt.ospf.simulation.simulators.PDEFTSimul;


public class DEFTPValueEvaluationMO extends AbstractMultiobjectiveEvaluationFunction<ILinearRepresentation<Double>> 
{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	protected NetworkTopology topology;
	protected Demands demands;
	protected int[] weights;
	protected boolean use_DEFT;
	
		
	public DEFTPValueEvaluationMO (NetworkTopology topology, Demands demand, int[] weights)
	{
		super(false); //sets isMaximization
		this.topology = topology;
		this.demands = demand;
		this.weights = weights;
		this.use_DEFT=true;
		
	}
	

	public DEFTPValueEvaluationMO (NetworkTopology topology, Demands demand, int[] weights,boolean use_DEFT)
	{
		super(false); //sets isMaximization
		this.topology = topology;
		this.demands = demand;
		this.weights = weights;
		this.use_DEFT=use_DEFT;
	}

	
	
	@Override
	public Double[] evaluateMO(ILinearRepresentation<Double> solution) throws DimensionErrorException {
		if(solution.getNumberOfElements()!=topology.getDimension())
			throw new DimensionErrorException();
		double[] pvalues = decode(solution);
		Double[] fitness = evalPValues(pvalues);
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
	
	protected Double[] evalPValues(double[] pValues) throws DimensionErrorException
	{
		double res1=0.0;
		double res2=0.0;
		PDEFTSimul simul = new PDEFTSimul(topology,use_DEFT);
		res1= simul.evalPValues(pValues,weights,this.demands);
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
	public IEvaluationFunction<ILinearRepresentation<Double>> deepCopy()
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int getNumberOfObjectives() {
		return 2;
	}
	
	
	
}
