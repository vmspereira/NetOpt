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

package pt.uminho.algoritmi.netopt.cplex.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class Arcs implements Iterable<Arc>{
	
	List<Arc> arcs;
	
	public Arcs(){
		arcs=new ArrayList<Arc>();
	}
	
	public int add(Arc a){
		this.arcs.add(a);
		return arcs.indexOf(a);
	}

	public Arc add(int fromNode, int toNode, double capacity){
		Arc a = new Arc(fromNode,toNode,capacity);
		this.arcs.add(a);
		return a;
	}
	
	public List<Arc> getAllArcsTo(int n){
		List<Arc> l=new ArrayList<Arc>();
		for(Arc a:arcs)
			if(a.getToNode()==n)
				l.add(a);
		return l;
	}
	
	
	public List<Arc> getAllArcsFrom(int n){
		List<Arc> l=new ArrayList<Arc>();
		for(Arc a:arcs)
			if(a.getFromNode()==n)
				l.add(a);
		return l;
	}
	
	
	public Arc getArc(int from,int to) throws NullPointerException{
		for(Arc a:arcs)
			if(a.getFromNode()==from && a.getToNode()==to)
				return a;
		throw new NullPointerException();
	}
	
	
	public Iterator<Arc> iterator(){
		return arcs.iterator();
	}
	
	public int getNumberOfArcs(){
		return this.arcs.size();
	}
	
	public List<Arc> getArcs(){
		return this.arcs;
	}

	public Arc getArc(int index) {
		return this.arcs.get(index);
	}
}
