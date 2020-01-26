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

/**
 * 
 * 
 */

import jecoli.algorithm.components.evaluationfunction.AbstractEvaluationFunction;
import jecoli.algorithm.components.evaluationfunction.IEvaluationFunction;
import jecoli.algorithm.components.evaluationfunction.InvalidEvaluationFunctionInputDataException;
import jecoli.algorithm.components.representation.linear.ILinearRepresentation;
import pt.uminho.algoritmi.netopt.ospf.optimization.Params;
import pt.uminho.algoritmi.netopt.ospf.simulation.DelayRequests;
import pt.uminho.algoritmi.netopt.ospf.simulation.Demands;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.edgeSelectors.CongestionFitnessEdgeSelector;
import pt.uminho.algoritmi.netopt.ospf.simulation.edgeSelectors.DefaultEdgeSelector;
import pt.uminho.algoritmi.netopt.ospf.simulation.edgeSelectors.HigherLoadDirectEdgeSelector;
import pt.uminho.algoritmi.netopt.ospf.simulation.edgeSelectors.HigherLoadEdgeSelector;
import pt.uminho.algoritmi.netopt.ospf.simulation.edgeSelectors.HigherLoadRacioDirectEdgeSelector;
import pt.uminho.algoritmi.netopt.ospf.simulation.edgeSelectors.INetEdgeSelector;
import pt.uminho.algoritmi.netopt.ospf.simulation.edgeSelectors.IDEdgeSelector;
import pt.uminho.algoritmi.netopt.ospf.simulation.edgeSelectors.HigherCentralityEdgeSelector;
import pt.uminho.algoritmi.netopt.ospf.simulation.exception.DimensionErrorException;
import pt.uminho.algoritmi.netopt.ospf.simulation.exception.GraphNotConnectedException;
import pt.uminho.algoritmi.netopt.ospf.simulation.simulators.LinkFailureSimul;


public class OSPFLinkFailureIntegerEvaluation extends
		AbstractEvaluationFunction<ILinearRepresentation<Integer>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	double alfa;
	double beta;
	NetworkTopology topology = null;
	Demands[] demands = null;
	DelayRequests delays = null;
	int[] edgesToFail;
	Params.EdgeSelectionOption edgeSelectionOption;
	
	boolean evaluateDelay=false;

	public OSPFLinkFailureIntegerEvaluation(double alfa, double beta,
			NetworkTopology topology, Demands[] demands, DelayRequests delays,
			Params.EdgeSelectionOption edgeSelectionOption,int[] edgesID,boolean evaluateDelay) {
		super(false); // sets isMaximization
		this.alfa = alfa;
		this.beta = beta;
		this.topology = topology;
		this.demands = demands;
		this.delays = delays;
		this.edgeSelectionOption = edgeSelectionOption;
		this.edgesToFail=edgesID;
		this.evaluateDelay=evaluateDelay;
	}

	@Override
	public double evaluate(ILinearRepresentation<Integer> solution)
			throws DimensionErrorException, GraphNotConnectedException {
		
		int[] weights = decode(solution);
		double fitness = evalWeights(weights);
		return fitness;
	}

	protected int[] decode(ILinearRepresentation<Integer> solution) {
		int[] res = new int[solution.getNumberOfElements()];
		for (int i = 0; i < solution.getNumberOfElements(); i++) {
			res[i] = solution.getElementAt(i);
		}
		return res;
	}

	protected double evalWeights(int[] weights) throws DimensionErrorException, GraphNotConnectedException {
		LinkFailureSimul simul = new LinkFailureSimul(topology);
		double fitness = 0.0;
		INetEdgeSelector selector;
		
		switch (edgeSelectionOption) {
		case HIGHERLOAD : selector= new HigherLoadEdgeSelector(simul);break;
		case MOSTUSEDPATH: selector= new HigherCentralityEdgeSelector(simul);break; 
		case HIGHERDIRECTLOAD:selector = new HigherLoadDirectEdgeSelector(simul);break;
		case HIGHERFAILUREFITNESSIMPACT: selector = new CongestionFitnessEdgeSelector(simul);
		case MOSTDIRECTUSEDRACIO: selector= new HigherLoadRacioDirectEdgeSelector(simul);break;
		case USERSELECTED: selector=new IDEdgeSelector(simul,edgesToFail);break;
		default: selector= new DefaultEdgeSelector(simul);break;
		}
		
		
		fitness = simul.evalWeightsLinkFailure(weights, demands[0], delays,
				alfa, beta, selector);
		return fitness;
	}
	
	
	protected double[] evalWeightsMO(int[] weights) throws DimensionErrorException, GraphNotConnectedException {
		LinkFailureSimul simul = new LinkFailureSimul(topology);
		INetEdgeSelector selector;
		switch (edgeSelectionOption) {
		case HIGHERLOAD : selector= new HigherLoadEdgeSelector(simul);break;
		case MOSTUSEDPATH: selector= new HigherCentralityEdgeSelector(simul);break; 
		case HIGHERDIRECTLOAD:selector = new HigherLoadDirectEdgeSelector(simul);break;
		case HIGHERFAILUREFITNESSIMPACT: selector = new CongestionFitnessEdgeSelector(simul);break;
		case MOSTDIRECTUSEDRACIO: selector= new HigherLoadRacioDirectEdgeSelector(simul);break;
		case USERSELECTED: selector=new IDEdgeSelector(simul,edgesToFail);break;
		default: selector= new DefaultEdgeSelector(simul);break;
		}
		double[] res = simul.evalWeightsLinkFailureMO(weights, demands[0], delays,evaluateDelay, selector); 
		return res;
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
