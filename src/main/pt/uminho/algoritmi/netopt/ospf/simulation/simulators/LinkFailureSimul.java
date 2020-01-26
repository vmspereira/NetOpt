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

import java.util.Iterator;

import pt.uminho.algoritmi.netopt.ospf.simulation.DelayRequests;
import pt.uminho.algoritmi.netopt.ospf.simulation.Demands;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkLoads;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.OSPFWeights;
import pt.uminho.algoritmi.netopt.ospf.simulation.Simul;
import pt.uminho.algoritmi.netopt.ospf.simulation.edgeSelectors.INetEdgeSelector;
import pt.uminho.algoritmi.netopt.ospf.simulation.exception.DimensionErrorException;
import pt.uminho.algoritmi.netopt.ospf.simulation.exception.GraphNotConnectedException;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetEdge;
import pt.uminho.algoritmi.netopt.ospf.graph.Graph;

public class LinkFailureSimul extends Simul{

	//BriteEdgeSelector selector;
	
	public enum ValuesAggredation{
		average,max
	} 
	
	public LinkFailureSimul(NetworkTopology topology) {
		super(topology);
//		this.selector= new MorePathsEdgeSelector(this);
	}

/*	
	public void setBriteEdgeSelector(BriteEdgeSelector edgeSelector){
		this.selector=edgeSelector;
	}
*/
	public double[] evalWeightsLinkFailureMO(int[] weights,Demands demands,DelayRequests delayReqs,boolean computeDelay,INetEdgeSelector edgeSelector) throws DimensionErrorException, GraphNotConnectedException 
	{
		INetEdgeSelector selector=edgeSelector;
		double res1,res2; //congestion
		double res3,res4; //delays
		
		res1=res2=res3=res4=0.0;
		
		
		
		// compute loads
		topology.applyWeights(weights);
		topology.shortestDistances();
		loads = new NetworkLoads(totalLoads(demands),topology);
		// congestion measure
		res1= congestionMeasure(loads,demands);
		// compute delay
		if (computeDelay) {
			OSPFWeights w = new OSPFWeights(topology.getDimension());
			w.setWeights(weights, topology);			
  		    res3 = computeDelays(w,delayReqs);
		}
		
		
		
		// select failed link
		// and compute congestion/delay measure
		OSPFWeights ws = new OSPFWeights(topology.getDimension());
		ws.setWeights(weights, topology);
		selector.setWeights(ws);
		Iterator<NetEdge> it=selector.getIterator();
		boolean connected;
		NetEdge e;
		int lf_from;int lf_to;
		do{
			e=it.next();
			lf_from=e.getFrom();
			lf_to=e.getTo();	
			topology.getGraph().setConnection(lf_from,lf_to , Graph.Status.DOWN);
			topology.getGraph().setConnection(lf_to,lf_from, Graph.Status.DOWN);
		
			// TODO
			// Question: what should be done in case of unconnected graph after link failure?
			// for now just throw exception
			connected = topology.getGraph().isConnected();
		}while(!connected && it.hasNext());
		if(!connected){
			throw new GraphNotConnectedException();
		}
		topology.applyWeights(weights);
		loads = new NetworkLoads(totalLoads(demands),topology);
		res2= congestionMeasure(loads,demands);
		if (computeDelay) {
			OSPFWeights w = new OSPFWeights(topology.getDimension());
			w.setWeights(weights, topology);			
  		    res4 = computeDelays(w,delayReqs);
		}
		
		topology.getGraph().setConnection(lf_from,lf_to , Graph.Status.UP);
		topology.getGraph().setConnection(lf_to,lf_from, Graph.Status.UP);

		double[] res;
		if(computeDelay){
			res=new double[4];
			res[0]=res1;res[1]=res2;res[2]=res3;res[3]=res4;
		}
		else{
			res=new double[2];
			res[0]=res1;res[1]=res2;
		}
		return res;
	}
	
	
	
	
	public double evalWeightsLinkFailure(int[] weights,Demands demands,DelayRequests delayReqs,double alfa,double beta,INetEdgeSelector edgeSelector) throws DimensionErrorException, GraphNotConnectedException 
	{
		boolean evaluateDelay=false;
		if(beta<1)
			evaluateDelay=true;
		double[] res=evalWeightsLinkFailureMO(weights,demands,delayReqs,evaluateDelay,edgeSelector);
		if(res.length==4)
			return (alfa * (beta * res[0] + (1.0-beta) * res[2] ) + (1.0 - alfa) * (beta * res[1] + (1.0-beta) * res[3]));
		else 
			return (alfa * res[0] + (1.0 - alfa) * res[1]);
					
	}
	
	

	/**
	 * Evaluate a set of weights for the single link failure of edges on the topology
	 * 
	 * @param weights
	 * @param demands
	 * @param delayReqs
	 * @param computeDelay
	 * @param edgeSelector
	 * @return
	 * @throws DimensionErrorException
	 * @throws GraphNotConnectedException
	 */
	public double evalWeightsAllLinkFailureMO(int[] weights,Demands demands,DelayRequests delayReqs,boolean computeDelay,ValuesAggredation s) throws DimensionErrorException, GraphNotConnectedException 
	{
		
						
		// compute loads
		topology.applyWeights(weights);
		topology.shortestDistances();
		
		
		// select failed link
		// and compute congestion/delay measure
		OSPFWeights ws = new OSPFWeights(topology.getDimension());
		ws.setWeights(weights, topology);
		NetEdge[] edges=topology.getNetGraph().getEdges();
		Double[] values=new Double[edges.length];
		
		for(int countedge=0;countedge<edges.length;countedge++){
			NetEdge e=edges[countedge];
			boolean connected;
			int lf_from;int lf_to;
			
			do{
				lf_from=e.getFrom();
				lf_to=e.getTo();	
				topology.getGraph().setConnection(lf_from,lf_to , Graph.Status.DOWN);
				topology.getGraph().setConnection(lf_to,lf_from, Graph.Status.DOWN);
				// TODO
				// Question: what should be done in case of unconnected graph after link failure?
				// for now just throw exception
				connected = topology.getGraph().isConnected();
			}while(!connected);
			
			if(!connected){
				values[countedge]=null;
			}else{
				topology.applyWeights(weights);
				loads = new NetworkLoads(totalLoads(demands),topology);
				values[countedge]= congestionMeasure(loads,demands);
			}
			topology.getGraph().setConnection(lf_from,lf_to , Graph.Status.UP);
			topology.getGraph().setConnection(lf_to,lf_from, Graph.Status.UP);
		}
		
		if(s.equals(LinkFailureSimul.ValuesAggredation.average))
			return 0.0;
		else
			return 0.0;
		
	}
	
	
	
	
	
	
	
	
	private static final long serialVersionUID = 1L;

}
