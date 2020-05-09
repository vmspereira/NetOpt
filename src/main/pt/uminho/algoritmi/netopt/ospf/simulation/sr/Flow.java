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
package pt.uminho.algoritmi.netopt.ospf.simulation.sr;

import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;



@SuppressWarnings("serial")
public class Flow implements Serializable{

	
	public static enum FlowType {

		SALP("SAPL"), USER("USER"), TILFA("TI-FLA"),E2E("E2E");

		private final String name;

		FlowType(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}
		
		public String toString(){
			return name;
		}
	}

	
	// source and destination are identified by node IDs
	private int source;
	private int destination;
	//src and dst ports not really necessary 
	private int srcPort;
	private int dstPort;
	// for DSCP / Cisco CS
	private ClassType classType;
	//traffic requirement
	private double demand;
	private double fraction;
	private boolean aggregated;
	private FlowType flowType;
	
	private final int MINPORT=0;
	private final int MAXPORT=65535;
	
	public Flow(int source,int destination,FlowType type){
		this.source=source;
		this.destination=destination;
		this.demand=0;
		this.classType=ClassType.AF11;
		this.setAggregated(true);
		this.fraction=1;
		this.flowType=type;
		this.srcPort= ThreadLocalRandom.current().nextInt(MINPORT, MAXPORT);
		this.dstPort= ThreadLocalRandom.current().nextInt(MINPORT, MAXPORT);
	} 
	
	
	public int getSource(){
		return this.source;
	}
	
	public int getDestination(){
		return this.destination;
	}
	
	public void setSource(int source){
		this.source=source;
	}
	
	public void setDestination(int destination){
		this.destination=destination;
	}
	
	public double getDemand(){
		return this.demand;
	}
	
	public void setDemand(double bandwidth){
		this.demand=bandwidth;
	}

		
	public String toString(){
		
		return ""+source+" - "+destination+"  bw = "+demand+" ";
	}


	public int getSrcPort() {
		return srcPort;
	}


	public void setSrcPort(int srcPort) {
		this.srcPort = srcPort;
	}


	public int getDstPort() {
		return dstPort;
	}


	public void setDstPort(int dstPort) {
		this.dstPort = dstPort;
	}


	public ClassType getClassType() {
		return classType;
	}


	public void setClassType(ClassType flowType) {
		this.classType = flowType;
	}
	
	
	@Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (this.getSource() ^ (this.getSource() >>> 32));
        result = prime * result + this.getDestination();
        result = prime * result + this.getSrcPort();
        result = prime * result + this.getDstPort();
        result = prime * result + this.getClassType().getValue();
        return result;
    }


	public boolean isAggregated() {
		return aggregated;
	}


	public void setAggregated(boolean aggregated) {
		this.aggregated = aggregated;
	}


	public double getFraction() {
		return fraction;
	}


	public void setFraction(double fraction) {
		this.fraction = fraction;
	}
	
	public FlowType getFlowType(){
		return this.flowType;
	}
	
	public void setFlowType(FlowType type){
		this.flowType=type;
	}
	
	public boolean equals(Flow f){
		return this.hashCode()==f.hashCode();
	}
}
