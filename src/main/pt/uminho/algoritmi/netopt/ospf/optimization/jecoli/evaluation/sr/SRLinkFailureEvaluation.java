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
import java.util.OptionalDouble;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jecoli.algorithm.components.evaluationfunction.AbstractMultiobjectiveEvaluationFunction;
import jecoli.algorithm.components.evaluationfunction.IEvaluationFunction;
import jecoli.algorithm.components.evaluationfunction.InvalidEvaluationFunctionInputDataException;
import jecoli.algorithm.components.representation.linear.ILinearRepresentation;
import pt.uminho.algoritmi.netopt.SystemConf;
import pt.uminho.algoritmi.netopt.ospf.graph.Graph;
import pt.uminho.algoritmi.netopt.ospf.simulation.Demands;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkLoads;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.Simul.LoadBalancer;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetEdge;
import pt.uminho.algoritmi.netopt.ospf.simulation.simulators.SRSimul;
import pt.uminho.algoritmi.netopt.ospf.utils.MathUtils;

public class SRLinkFailureEvaluation extends AbstractMultiobjectiveEvaluationFunction<ILinearRepresentation<Integer>>{

	
	public enum LFObjectives{
		CONGESTION_MLU, MLU_CONGESTION, CONGESTION_CONGESTION;
	}
	
	
	private NetworkTopology topology;
	private Demands demands;
	private LoadBalancer loadBalancer;
	private LFObjectives objectives;
	private int[] worstEdgeCount;
	
	
	private double EVALUATE_ALL_EDGES_PROBABILITY = 0.1;
	private double LINKS_SAMPLE_FRACTION = 0.3;
	private int NTHREADS = 4;
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public SRLinkFailureEvaluation(NetworkTopology topology, Demands demands,LoadBalancer loadBalancer) {
		this(topology,demands,loadBalancer,LFObjectives.CONGESTION_CONGESTION);
	}
	
	
	public SRLinkFailureEvaluation(NetworkTopology topology, Demands demands,LoadBalancer loadBalancer, LFObjectives objectives) {
		super(false);
		this.topology = topology;
		this.demands =demands;
		this.loadBalancer =loadBalancer;
		this.objectives = objectives;
		this.worstEdgeCount =new int[topology.getNetGraph().getEdges().length];
		
		// configuration parameters
		this.NTHREADS=SystemConf.getPropertyInt("threads.number", 4);
		this.EVALUATE_ALL_EDGES_PROBABILITY =SystemConf.getPropertyDouble("sr.lfevalallprobability",0.1);
		this.LINKS_SAMPLE_FRACTION =SystemConf.getPropertyDouble("sr.lfsamplefraction", 0.3);
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
		int[] weights = decode(solution);
		Double[] fitness;
		if(NTHREADS<=1)
			fitness= evaluateLinkFailure(weights);
		else
			fitness= evaluateLinkFailureMultiThread(NTHREADS,weights);
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
	
	
	
	
	private Double[] evaluateLinkFailure(int[] weights){
		
		Double[] res=new Double[2];	
		//evaluate the congestion for the network on a normal state
		SRSimul simul = new SRSimul(topology,this.loadBalancer);
		double r0=simul.evalWeights(weights, demands);
		if(this.objectives == LFObjectives.MLU_CONGESTION)
			res[0] = simul.getLoads().getMLU();
		else
			res[0] = r0;
		int worstEdgeIndex=0;
		double mlu = 0;
		double congestion=0;
		NetworkTopology t = topology.copy();
		
		for(int i=0;i<t.getNetGraph().getNEdges();i++){
			SRSimul s = new SRSimul(t,this.loadBalancer);
			NetEdge e =t.getNetGraph().getEdge(i);
			t.setEdgeStatus(e, Graph.Status.DOWN);
			
			s.evalWeights(weights, demands);		
			NetworkLoads load=s.getLoads();
			
			double _mlu = load.getMLU();
			double _cong = load.getCongestion();
			
			//reset link status 
			t.setEdgeStatus(e, Graph.Status.UP);
			if(_mlu>mlu)
				mlu=_mlu;
			if(_cong>congestion){
				congestion=_cong;
				worstEdgeIndex=i;
			}
		}
		
		if(this.objectives == LFObjectives.CONGESTION_MLU)
			res[1]=mlu ;
		else
			res[1] = congestion;
		worstEdgeCount[worstEdgeIndex]++;
		return res;
	}

	
	
	public Double[] evaluateLinkFailureMultiThread(int threads,int[] weights) throws InterruptedException{
		
		ExecutorService exec = Executors.newFixedThreadPool(threads);
		CountDownLatch latch = new CountDownLatch(threads);
		ComputeUnit[] runnables = new ComputeUnit[threads];
		Double[] res = new Double[2];
		boolean useDEFT = this.loadBalancer==LoadBalancer.DEFT;
		//first objective
		SRSimul simul = new SRSimul(topology,this.loadBalancer);
		simul.evalPValues(null, weights, demands);
		
		if(this.objectives == LFObjectives.MLU_CONGESTION)
			res[0] = simul.getLoads().getMLU();
		else
			res[0] = simul.getLoads().getCongestion();
			
		/**
		 * 
		 * Randomly select n failing links.
		 * 
		 * In some iterations, all link failure may be evaluated.
		 * The probability of at an iteration all link failure be evaluates
		 * is user defined EVALUATE_ALL_EDGES_PROBABILITY.
		 * 
		 * When a sample is used the link with more worst evaluations is
		 * added to the sample
		 *  
		 */
		// probability of all edges evaluation
		int size =topology.getNetGraph().getNEdges();
		int sampleSize = size;
		if(Math.random()>this.EVALUATE_ALL_EDGES_PROBABILITY){
			// 
			sampleSize= (int)(size*LINKS_SAMPLE_FRACTION);
		}
		// only evaluates edges with indexes in the 
		// first sampleSize of the permutation array		
		int[] edges = Arrays.copyOfRange(MathUtils.give_rand_perm(size),0,sampleSize);
		//include worst edge
		if(sampleSize<size){
			int e=this.getWortEdge();
			edges[0]=e;
		}
		
			
		//multi-threaded second objective computation
		for(int i=0;i<edges.length;i++)
			edges[i]=i;
		int from=0;
		int n = edges.length/threads-1;
		int t = edges.length%threads;
		//split edges among threads
		for(int i=0;i<threads;i++){
			int to= from+n;
			if(t>0)
				to+=1;
			int[] a=Arrays.copyOfRange(edges, from, to);
			runnables[i]=new ComputeUnit(topology.copy(),Arrays.copyOf(weights,weights.length),demands,a,this.loadBalancer);
			from = to+1;
			t--;
		}
		// parallel link failure computation
		for(ComputeUnit r : runnables) {
		    r.setLatch(latch);
		    exec.execute(r);
		}

		latch.await();
		exec.shutdown();
		
		if(this.objectives == LFObjectives.CONGESTION_MLU){
			OptionalDouble mlu = Arrays.stream(runnables).mapToDouble(r -> r.getMaxMLU()).max();
			res[1]=mlu.getAsDouble();
		}
		else{
			double max=runnables[0].getMaxCong();
			int e = runnables[0].getWorstEdge();
			for(int i=1;i<runnables.length;i++){
				if(runnables[i].getMaxCong()>max){
					max=runnables[i].getMaxCong();
					e = runnables[i].getWorstEdge();
				}
			}
			this.worstEdgeCount[e]++;	
			res[1]=max;
		}	
		return res;
	}
	
	
	public int getWortEdge(){
		int pos=0;
		int max= this.worstEdgeCount[0];
		for(int i=1;i<worstEdgeCount.length;i++)
			if(worstEdgeCount[i]>max){
				max=worstEdgeCount[i];
				pos=i;
			}
		return pos;
	}
	
	
	private class ComputeUnit implements Runnable {

		
		private NetworkTopology topo;
		private int[] w;
		private Demands d; 
		private int[] e;
		private double m;
		private double c;
		private LoadBalancer lb;
		private CountDownLatch latch;
		int worstEdge;

		
		public ComputeUnit(NetworkTopology topology, int[] weights, Demands demands, int[] edges,LoadBalancer lb){
			this.topo=topology;
			this.w=weights;
			this.d= demands;
			this.e =edges;
			this.m=0.0;
			this.lb=lb;
		}
		
		public void setLatch(CountDownLatch latch) {
		    this.latch = latch;
		  }
		
		public double getMaxMLU(){
			return this.m;
		}
		
		public double getMaxCong(){
			return this.c;
		}
		
		public int getWorstEdge(){
			return this.worstEdge;
		}
		
		@Override
		public void run() {
			SRSimul simul = new SRSimul(topo,this.lb);
			double maxMLU=0.0;
			double maxCong =0.0;
			worstEdge =e[0];
			for(int i=0;i<e.length;i++){
				NetEdge edge= topo.getNetGraph().getEdge(e[i]);  
				topo.setEdgeStatus(edge, Graph.Status.DOWN);
				simul.evalPValues(null, w, d);		// p-values are set to default
				NetworkLoads load=simul.getLoads();
				double mlu = load.getMLU();
				double c= load.getCongestion();
				if(mlu>maxMLU)
					maxMLU=mlu;
				if(c>maxCong){
					maxCong = c;
					worstEdge=e[i];
				}
				topo.setEdgeStatus(edge, Graph.Status.UP);
			}
			this.m=maxMLU;
			this.c = maxCong;
			latch.countDown();
		}	
	}
	
}
