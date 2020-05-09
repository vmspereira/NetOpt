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
package pt.uminho.algoritmi.netopt.ospf.simulation.simulators;

import java.util.concurrent.CountDownLatch;

import pt.uminho.algoritmi.netopt.ospf.simulation.Demands;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkLoads;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.Simul;


public class MultiTopologySimul {

	NetworkTopology[] topologies;
	int nLayers;
	int topoDimention;
	NetworkLoads loads;
	Simul simul;
	final Worker[] workers;
	
	public MultiTopologySimul(NetworkTopology topology,int n) {
		topologies = new NetworkTopology[n];
		nLayers=n;
		topoDimention = topology.getDimension();
		simul = new Simul(topology);
		workers = new Worker [nLayers];
		
		for(int i=0;i<nLayers;i++){
			topologies[i]=topology.copy();
			workers[i]=new Worker(i,topologies[i]);
		}
	}

	
	public double evalWeightsMT(int[] weights,Demands[] demands,Demands totalDemands) 
	{
		loads= new NetworkLoads(topologies[0]);
		CountDownLatch doneSignal = new CountDownLatch(nLayers);
		
		int size=weights.length/nLayers;
		
		for(int i=0;i<nLayers;i++)
		{
			int[] w=new int[size];
			for(int j=0;j<size;j++)
				w[j]=weights[j+i*size];
			
			WorkerRunnable runner =new WorkerRunnable(i,doneSignal,w,demands[i]); 
			runner.run();
		}
		
		
		try {
			doneSignal.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	
		for(Worker worker : workers)
			loads.addLoads(worker.getLoads());

		
		double res= simul.congestionMeasure(loads,totalDemands);
		return res;
	}
	
	
	class WorkerRunnable implements Runnable{

		int layer;
		final CountDownLatch doneSignal;
		int[] weights;
		Demands demands;
		
		public WorkerRunnable(int i,CountDownLatch doneSignal,int[] w,Demands demands){
			this.layer=i;
			this.doneSignal=doneSignal;
			this.weights=w;
			this.demands = demands;
		}
		@Override
		public void run() {
			workers[layer].doWork(weights, demands);	
			doneSignal.countDown();
		}
		
		
	}
	
	class Worker{
		   NetworkTopology topology;
		   Simul sim;
		   double[][] loads;
		   int id;
		   
		   Worker(int i,NetworkTopology topology){
		      this.id=i;
			  this.topology=topology;
		      this.sim=new Simul(topology);
		   }
		   
		 
		   void doWork(int[] weights,Demands demands)
		   { 
			  topology.applyWeights(weights);
			  topology.shortestDistances();
			  loads= sim.totalLoads(demands);
		   }
		   
		   double[][] getLoads(){
			   return loads;
		   }
		 }
	
	
	/**
	 * 
	 */
	

	public NetworkLoads getLoads() {
		return loads;
	}

	

}
