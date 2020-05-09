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

import pt.uminho.algoritmi.netopt.ospf.simulation.AverageEndtoEndDelays;
import pt.uminho.algoritmi.netopt.ospf.simulation.DelayRequests;
import pt.uminho.algoritmi.netopt.ospf.simulation.Demands;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkLoads;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.OSPFWeights;
import pt.uminho.algoritmi.netopt.ospf.simulation.Simul.LoadBalancer;
import pt.uminho.algoritmi.netopt.ospf.simulation.exception.DimensionErrorException;

public interface ISimulator {
	
	
	public NetworkLoads getLoads();
	public double[][] partialLoads(int destination, Demands demands);
	public AverageEndtoEndDelays getAverageEndToEndDelays();
	public NetworkTopology getTopology();
	public double evalWeights(int[] weights, double alfa, Demands demands, DelayRequests delayReqs)
			throws DimensionErrorException;
	public void computeLoads(OSPFWeights weights, Demands demands) throws DimensionErrorException;
	public double computeDelays(OSPFWeights weights, DelayRequests delays) throws DimensionErrorException;
	public void setLoadBalancer(LoadBalancer loadBalancer);
		

}
