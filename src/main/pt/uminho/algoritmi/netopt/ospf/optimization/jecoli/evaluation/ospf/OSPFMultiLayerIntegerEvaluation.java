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
import pt.uminho.algoritmi.netopt.ospf.simulation.Demands;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.exception.DimensionErrorException;
import pt.uminho.algoritmi.netopt.ospf.simulation.simulators.MultiTopologySimul;



public class OSPFMultiLayerIntegerEvaluation extends AbstractEvaluationFunction<ILinearRepresentation<Integer>> 
{
	
	
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	double alfa;
	NetworkTopology topology = null;
	Demands[] demands = null;
	int nLayers;
	Demands totalDemands=null;
	Boolean linkFailure=false;
	

	
		
	public OSPFMultiLayerIntegerEvaluation (NetworkTopology topology, 
			Demands[] demands, int nLayers 
			)
	{
		super(false); //sets isMaximization
		this.topology = topology;
		this.demands = demands;
		this.nLayers=nLayers;
		this.totalDemands=new Demands(topology.getDimension());
		for(int i=0;i<demands.length;i++)
			totalDemands.add(demands[i]);
	}
	

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
	
	protected double evalWeights (int[] weights) throws DimensionErrorException
	{
		MultiTopologySimul simul = new MultiTopologySimul(topology,nLayers);
		double fitness= simul.evalWeightsMT(weights,demands, totalDemands);
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
}
