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
package pt.uminho.algoritmi.netopt.ospf.simulation;

import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class ResultSimul implements Serializable
{
	private ArrayList<Demands> demands;
	private DelayRequests delayReqs;
	private ArrayList<OSPFWeights> weights;
	private ArrayList<NetworkLoads> loads;
	private AverageEndtoEndDelays endToEndDelays;
	

	public ResultSimul() {
		this.demands = new ArrayList<Demands>();
		this.delayReqs = null;
		this.weights = new ArrayList<OSPFWeights>();
		this.loads = new ArrayList<NetworkLoads>();
		this.endToEndDelays = null;
	}

	public void addNetworkLoads(NetworkLoads loads) {
		this.loads.add(loads);
	}

	public ArrayList<NetworkLoads> getNetworkLoads() {
		return loads;
	}

	public void addDemands(Demands demands) {
		this.demands.add(demands);
	}

	public ArrayList<Demands> getDemands() {
		return demands;
	}

	public void setDelayReqs(DelayRequests delayReqs) {
		this.delayReqs = delayReqs;
	}

	public DelayRequests getDelayReqs() {
		return delayReqs;
	}

	public void addWeights(OSPFWeights weights) {
		this.weights.add(weights);
	}

	public ArrayList<OSPFWeights> getWeights() {
		return weights;
	}

	public void setEndToEndDelays(AverageEndtoEndDelays endToEndDelays) {
		this.endToEndDelays = endToEndDelays;
	}

	public AverageEndtoEndDelays getEndToEndDelays() {
		return endToEndDelays;
	}

}
