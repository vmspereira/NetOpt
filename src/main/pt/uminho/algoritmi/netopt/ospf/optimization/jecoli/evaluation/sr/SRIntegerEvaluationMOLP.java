/*******************************************************************************
 * Copyright 2012-2019,
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
import pt.uminho.algoritmi.netopt.cplex.SRLoadBalancingSolver;
import pt.uminho.algoritmi.netopt.ospf.simulation.Demands;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.OSPFWeights;
import pt.uminho.algoritmi.netopt.ospf.simulation.simulators.SRSimul;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.SRConfiguration;

public class SRIntegerEvaluationMOLP extends AbstractMultiobjectiveEvaluationFunction<ILinearRepresentation<Integer>> {

	/**
	 * SR paths are all alternatives paths as defined in [1], however load
	 * balancing fractions between parallel SR paths are obtained by solving the
	 * LP problem that minimizes the network MLU (SRLoadBalancingSolver)
	 * 
	 * [1] Optimizing Segment Routing using Evolutionary Computation, Pereira et
	 * al.
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected NetworkTopology topology;
	protected Demands[] demands;

	private double alpha;

	public SRIntegerEvaluationMOLP(NetworkTopology topology, Demands[] demands) {
		this(topology, demands, true);
	}

	public SRIntegerEvaluationMOLP(NetworkTopology topology, Demands[] demands, boolean hybrid) {
		super(false);
		this.topology = topology;
		this.demands = demands;
		this.alpha = 0.5;
	}

	public Double evaluate(ILinearRepresentation<Integer> solution) throws Exception {
		int[] weights = decode(solution);
		Double[] fitness = evalWeightsMO(weights);
		return alpha * fitness[0] + (1 - alpha) * fitness[1];
	}

	@Override
	public Double[] evaluateMO(ILinearRepresentation<Integer> solution) throws Exception {
		int[] weights = decode(solution);
		Double[] fitness = evalWeightsMO(weights);
		return fitness;
	}

	protected int[] decode(ILinearRepresentation<Integer> solution) {
		int[] res = new int[solution.getNumberOfElements()];
		for (int i = 0; i < solution.getNumberOfElements(); i++) {
			res[i] = solution.getElementAt(i);
		}
		return res;
	}

	protected Double[] evalWeightsMO(int[] weights) throws Exception {
		double congestion = Double.MAX_VALUE;
		double mlu = Double.MAX_VALUE;
		Double[] fitness = new Double[2];
		fitness[0]=Double.MAX_VALUE; fitness[1]=Double.MAX_VALUE;
		topology.applyWeights(weights);
		SRSimul simul = new SRSimul(topology);
		SRConfiguration config = simul.getSRConfigurationLP();
		//System.out.println("SR Config:");
		//System.out.println(config.toString());
		
		OSPFWeights w = new OSPFWeights(topology.getDimension());
		w.setWeights(weights, topology);
		SRLoadBalancingSolver solver = new SRLoadBalancingSolver(topology, w, config, demands[0]);
		solver.setSaveLoads(true);
		mlu = solver.optimize();
		if(solver.hasSolution()){
			congestion=simul.congestionMeasure(solver.getNetworLoads(), demands[0]);		
			fitness[0] = congestion;
			fitness[1] = mlu;
		}
		
		return fitness;
	}

	@Override
	public void verifyInputData() throws InvalidEvaluationFunctionInputDataException {
		// TODO Auto-generated method stub

	}

	@Override
	public IEvaluationFunction<ILinearRepresentation<Integer>> deepCopy() throws Exception {
		Demands[] d = new Demands[demands.length];
		for(int i=0; i<demands.length;i++)
			d[i]=demands[i].copy();
		return new SRIntegerEvaluationMOLP(this.topology.copy(),d);
	}

	@Override
	public int getNumberOfObjectives() {
		return 2;
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

}
