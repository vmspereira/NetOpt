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
package pt.uminho.algoritmi.netopt.ospf.graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class SPFElement implements Serializable{

    private int node;
    private float cost;
    private List<Integer> nextHops;
    private float bandwidth;
    

    
    public SPFElement(){
    	this.nextHops = new ArrayList<Integer>();

    }
    
    public SPFElement(int node){
    	this.nextHops = new ArrayList<Integer>();
        this.nextHops.add(node);
    }
    
    public SPFElement(int node, float cost, int nextHop, float bandwidth) {
        this.node = node;
        this.cost = cost;
        
        this.nextHops = new ArrayList<Integer>();
        this.nextHops.add(nextHop);
        
        this.bandwidth = bandwidth;
    }

    /**
     * Get the id of the object
     *
     * @return the object's id
     */
    public int getId() {
        return node;
    }

    /**
     * Get the key of the object
     *
     * @return the object's key
     */
    public float getKey() {
        return cost;
    }

    public int getNode() {
        return node;
    }

    public void setNode(int node) {
        this.node = node;
    }

    public float getCost() {
        return cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    public int getNextHop() {
        return nextHops.get(0);
    }

    public void setNextHop(int nextHop) {
        this.nextHops.set(0,nextHop);
    }

    public List<Integer> getNextHops() {
        return nextHops;
    }

    public void addNextHop(int nextHop) {
        this.nextHops.add(nextHop);
    }

    public float getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(float bandwidth) {
        this.bandwidth = bandwidth;
    }

    public String toString(){
    	StringBuffer s= new StringBuffer();
    	for(int i=0;i< nextHops.size();i++)
    		s.append(nextHops.get(i)).append(" ");
    	return s.toString();
    }

	public int countHops() {
		return this.nextHops.size();
	}

	public int getNextHop(int i) {
		return this.nextHops.get(i);
	}

	public void removeAll() {
		this.nextHops =  new ArrayList<Integer>();
	}
}

