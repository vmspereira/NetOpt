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

import java.util.Arrays;
import jecoli.algorithm.components.evaluationfunction.AbstractMultiobjectiveEvaluationFunction;
import jecoli.algorithm.components.evaluationfunction.IEvaluationFunction;
import jecoli.algorithm.components.evaluationfunction.InvalidEvaluationFunctionInputDataException;
import jecoli.algorithm.components.representation.linear.ILinearRepresentation;
import pt.uminho.algoritmi.netopt.SystemConf;
import pt.uminho.algoritmi.netopt.ospf.simulation.Demands;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkLoads;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.OSPFWeights;
import pt.uminho.algoritmi.netopt.ospf.simulation.Simul;
import pt.uminho.algoritmi.netopt.ospf.simulation.Simul.LoadBalancer;
import pt.uminho.algoritmi.netopt.ospf.simulation.exception.DimensionErrorException;
import pt.uminho.algoritmi.netopt.ospf.simulation.simulators.ISimulator;
import pt.uminho.algoritmi.netopt.ospf.simulation.simulators.SRSimul;

public class SRMultiLayerEvaluation extends AbstractMultiobjectiveEvaluationFunction<ILinearRepresentation<Integer>>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/*Number of layers to be evaluated*/
	private int nLayers; 
	private boolean useDEFT= true;
	private NetworkTopology topology;
	private Demands[]  demands;
	private double[][] inicialLoads;
	private ISimulator simul;
	private Demands totalDemands;
	private double[] pvalues;
	protected int threads = SystemConf.getPropertyInt("threads.number", 4);
	//if a single shortest path should be use for tunneling 
	//if false, SR is used
	protected boolean SSP;
	
	/**
	 * Evaluate N-SR-layers. The number of layers is defined by the number of traffic demand matrices
	 * 
	 * @param topology
	 * @param demands
	 */
	public SRMultiLayerEvaluation(NetworkTopology topology,Demands[] demands,LoadBalancer lb) {
		super(false);
		this.topology = topology;
		this.demands=demands;
		this.SSP=false;
		nLayers=demands.length;
		inicialLoads = new double[topology.getDimension()][topology.getDimension()];
		totalDemands = new Demands(topology.getDimension());
		for(Demands d:demands)
			totalDemands.add(d);
		simul = new SRSimul(topology,lb);
	}
	
	
	
	/**
	 * 
	 * @param topology
	 * @param fixedWeight
	 * @param fixedLayerDemand
	 * @param demands array
	 * 
	 * Multi-layer SR optimization evaluation. 
	 * A layer has a fixed configuration of demands and weights.
	 * The remaining layers traffic necessities are set in the array of traffic demands, a Demand object for each layer 
	 */
	public SRMultiLayerEvaluation(NetworkTopology topology,OSPFWeights fixedWeight, Demands fixedLayerDemand ,Demands[] demands,LoadBalancer lb) {
		super(false);
		this.SSP=false;
		this.topology = topology;
		this.demands=demands;
		nLayers=demands.length;
		
		
		SRSimul sim = new SRSimul(topology,lb);
		sim.evalPValues(null,fixedWeight.asIntArray(),fixedLayerDemand);
		inicialLoads = sim.getLoads().getLoads();
		
		totalDemands = new Demands(topology.getDimension());
		for(Demands d:demands)
			totalDemands.add(d);
		totalDemands.add(fixedLayerDemand);
		
		simul = new Simul(topology);
		simul.setLoadBalancer(LoadBalancer.NOLB);
	}
	
	
	public SRMultiLayerEvaluation(NetworkTopology topology,OSPFWeights fixedWeight, Demands fixedLayerDemand ,Demands[] demands,boolean SSP) {
		super(false);
		this.SSP=SSP;
		this.topology = topology;
		this.demands=demands;
		nLayers=demands.length;
		
		SRSimul sim = new SRSimul(topology);
		sim.evalPValues(null,fixedWeight.asIntArray(),fixedLayerDemand);
		inicialLoads = sim.getLoads().getLoads();
		
		totalDemands = new Demands(topology.getDimension());
		for(Demands d:demands)
			totalDemands.add(d);
		totalDemands.add(fixedLayerDemand);
		if(SSP){
			simul = new Simul(topology);
			simul.setLoadBalancer(LoadBalancer.NOLB);
		}else{
			simul = new SRSimul(topology);
		}
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

	
	public Double evaluate(ILinearRepresentation<Integer> solutionRepresentation) throws Exception {
		Double[] result = evaluateMO(solutionRepresentation);
		return result[0];
	}
	
	
	public void setPValues(double[] pvalues){
		this.pvalues=pvalues;
	}
	
	public double[] getPValues(){
		return this.pvalues;
	}
	
	public Double[] evaluateMO(ILinearRepresentation<Integer> solutionRepresentation) throws Exception {
		
		int[] solution=decode(solutionRepresentation);
		int n = topology.getNumberEdges();
		Double[] res = new Double[2];
	
		
		if(solution.length!=this.nLayers*n)
			throw new DimensionErrorException("SRMultiLayerEvaluation solution.length="+solution.length+" sould be "+this.nLayers*n);
		
		
		NetworkLoads l= new NetworkLoads(topology);
		l.addLoads(this.inicialLoads);
		for(int i=0;i<nLayers;i++){	
			int[] weights=Arrays.copyOfRange(solution, i*n,(i+1)*n);
			Demands d = demands[i];
			OSPFWeights w = new OSPFWeights(this.topology.getDimension());
			w.setWeights(weights, topology);
			simul.computeLoads(w, d);
			l.addLoads(simul.getLoads().getLoads());
		}
		
		
		
		res[0] = ((Simul)simul).congestionMeasure(l, totalDemands);
		res[1]= l.getMLU();
		return res;
	}


	public int getNLayers() {
		return nLayers;
	}


	@Override
	public int getNumberOfObjectives() {
		return 2;
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
}
