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

import jecoli.algorithm.components.evaluationfunction.AbstractMultiobjectiveEvaluationFunction;
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

public class OSPFLinkFailureIntegerEvaluationMO
		extends AbstractMultiobjectiveEvaluationFunction<ILinearRepresentation<Integer>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	double alfa;
	double beta;
	NetworkTopology topology = null;
	Demands[] demands = null;
	DelayRequests delays = null;
	Params.EdgeSelectionOption edgeSelectionOption;
	int[] edgesToFail;

	boolean evaluateDelay = false;

	public OSPFLinkFailureIntegerEvaluationMO(double alfa, double beta, NetworkTopology topology, Demands[] demands,
			DelayRequests delays, Params.EdgeSelectionOption edgeSelectionOption, int[] edgesID,
			boolean evaluateDelay) {
		super(false); // sets isMaximization
		this.alfa = alfa;
		this.beta = beta;
		this.topology = topology;
		this.demands = demands;
		this.delays = delays;
		this.edgesToFail = edgesID;
		this.edgeSelectionOption = edgeSelectionOption;
		this.evaluateDelay = evaluateDelay;
	}

	protected int[] decode(ILinearRepresentation<Integer> solution) {
		int[] res = new int[solution.getNumberOfElements()];
		for (int i = 0; i < solution.getNumberOfElements(); i++) {
			res[i] = solution.getElementAt(i);
		}
		return res;
	}

	protected double[] evalWeightsMO(int[] weights) throws DimensionErrorException, GraphNotConnectedException {
		LinkFailureSimul simul = new LinkFailureSimul(topology);
		INetEdgeSelector selector;
		switch (edgeSelectionOption) {
		case HIGHERLOAD:
			selector = new HigherLoadEdgeSelector(simul);
			break;
		case MOSTUSEDPATH:
			selector = new HigherCentralityEdgeSelector(simul);
			break;
		case HIGHERDIRECTLOAD:
			selector = new HigherLoadDirectEdgeSelector(simul);
			break;
		case HIGHERFAILUREFITNESSIMPACT:
			selector = new CongestionFitnessEdgeSelector(simul);
			break;
		case MOSTDIRECTUSEDRACIO:
			selector = new HigherLoadRacioDirectEdgeSelector(simul);
			break;
		case USERSELECTED:
			selector = new IDEdgeSelector(simul, edgesToFail);
			break;
		default:
			selector = new DefaultEdgeSelector(simul);
			break;
		}
		double[] res = simul.evalWeightsLinkFailureMO(weights, demands[0], delays, false, selector);
		return res;
	}

	@Override
	public void verifyInputData() throws InvalidEvaluationFunctionInputDataException {
		// TODO Auto-generated method stub

	}

	@Override
	public IEvaluationFunction<ILinearRepresentation<Integer>> deepCopy() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNumberOfObjectives() {
		return 2;
	}

	@Override
	public Double[] evaluateMO(ILinearRepresentation<Integer> solution) throws Exception {
		Double[] resultList = new Double[2];

		int[] weights = decode(solution);
		double[] fitness = evalWeightsMO(weights);

		resultList[0] = new Double(fitness[0]);
		resultList[1] = new Double(fitness[1]);

		return resultList;
	}
}
