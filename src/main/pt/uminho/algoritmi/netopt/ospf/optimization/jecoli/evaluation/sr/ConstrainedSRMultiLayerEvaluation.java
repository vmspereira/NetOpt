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
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

import jecoli.algorithm.components.evaluationfunction.AbstractMultiobjectiveEvaluationFunction;
import jecoli.algorithm.components.evaluationfunction.IEvaluationFunction;
import jecoli.algorithm.components.evaluationfunction.InvalidEvaluationFunctionInputDataException;
import jecoli.algorithm.components.representation.linear.ILinearRepresentation;
import pt.uminho.algoritmi.netopt.SystemConf;
import pt.uminho.algoritmi.netopt.ospf.graph.MatDijkstra;
import pt.uminho.algoritmi.netopt.ospf.simulation.Demands;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.OSPFWeights;
import pt.uminho.algoritmi.netopt.ospf.simulation.exception.DimensionErrorException;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetNode;
import pt.uminho.algoritmi.netopt.ospf.simulation.simulators.SRSimul;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.LabelPath;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.SRConfiguration;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.SRPathTranslator;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.SRSimulator;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.SRConfiguration.SRConfigurationType;

public class ConstrainedSRMultiLayerEvaluation extends AbstractMultiobjectiveEvaluationFunction<ILinearRepresentation<Integer>>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/*Number of layers to be evaluated*/
 

	private NetworkTopology topology;
	private Demands[]  demands;
	private double[] pvalues;
	protected int threads = SystemConf.getPropertyInt("threads.number", 4);
	//if a single shortest path should be use for tunneling 
	//if false, SR is used
	protected boolean SSP;
	private Demands fixedLayerDemand;
	private OSPFWeights originalWeights;
	private SRSimulator simulator;
	private SRPathTranslator translator;
	private SRConfiguration initConf;
	
	
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
	 * @throws DimensionErrorException 
	 */

	
	public ConstrainedSRMultiLayerEvaluation(NetworkTopology topology,OSPFWeights fixedWeight, Demands fixedLayerDemand ,Demands[] demands) throws DimensionErrorException {
		super(false);
		this.SSP=true;
		this.topology = topology;	
		this.demands=demands;
		this.originalWeights = fixedWeight;
		this.fixedLayerDemand = fixedLayerDemand;
		this.simulator = new SRSimulator(topology,originalWeights);
		this.translator= new SRPathTranslator(topology,originalWeights);
		SRSimul sim =new SRSimul(topology);
	    sim.setConfigureSRPath(true);
	    sim.computeLoads(originalWeights, fixedLayerDemand);
	     this.initConf = sim.getSRconfiguration();
	    
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
	
		
		
		if(solution.length!=2*n)
			throw new DimensionErrorException("SRMultiLayerEvaluation solution.length="+solution.length+" sould be "+2*n);
		
		
		
		int[] s1=Arrays.copyOfRange(solution, 0,n);
		int[] s2=Arrays.copyOfRange(solution, n,2*n);
		
		
		NetworkTopology topo=topology.copy();
		
	    SRConfiguration conf=new SRConfiguration();
		conf.setType(SRConfigurationType.LINK_FAILURE);

		SRConfiguration conf2=new SRConfiguration();
		conf2.setType(SRConfigurationType.LINK_FAILURE);

	    
		//Translate s1 
		topo.applyWeights(s1);
	    Demands d1= demands[0];
	    MatDijkstra spGraph=topo.getShortestPathGraph();
		
	    for(int source=0;source<topology.getDimension();source++)
	    	for(int dest=0;dest<topology.getDimension();dest++){
	    		if(d1.getDemands(source,dest)!=0){
	    			Vector<Integer> path=spGraph.getPath(source, dest);
	    			List<NetNode> nodePath=path.stream().map(x->topo.getNetGraph().getNodeByID(x)).collect(Collectors.toList());
	    			LabelPath lp=translator.translate(nodePath);
	    			conf.addLabelPath(lp);
	    			LabelPath lp2=translator.translate(nodePath,3);
	    			conf2.addLabelPath(lp2);
	    			
	    		}
	    	}
	    
	   
	    
	    //Translate s2
		topo.applyWeights(s2);
	    Demands d2= demands[1];
	    spGraph=topo.getShortestPathGraph();
	    
	    for(int source=0;source<topology.getDimension();source++)
	    	for(int dest=0;dest<topology.getDimension();dest++){
	    		if(d2.getDemands(source,dest)!=0){
	    			Vector<Integer> path=spGraph.getPath(source, dest);
	    			List<NetNode> nodePath=path.stream().map(x->topo.getNetGraph().getNodeByID(x)).collect(Collectors.toList());
	    			LabelPath lp=translator.translate(nodePath);
	    			conf.addLabelPath(lp);
	    			LabelPath lp2=translator.translate(nodePath,3);
	    			conf2.addLabelPath(lp2);
	    		}
	    	}
	    
		
	    
	    
	    simulator.clear();
	    simulator.apply(initConf, fixedLayerDemand);
	    simulator.apply(conf, d1);
	    simulator.apply(conf2, d2);
	     

		res[0] = simulator.getCongestionValue();
		res[1]=  simulator.getMLU();
		return res;
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
