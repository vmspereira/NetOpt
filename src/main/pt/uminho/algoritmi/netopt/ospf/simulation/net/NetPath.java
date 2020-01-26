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
package pt.uminho.algoritmi.netopt.ospf.simulation.net;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class NetPath {
	
	ArrayList<NetEdge> path;
	
	public NetPath(){
		path=new ArrayList<NetEdge>(); 
	}
	
	public NetPath(List<Vector<Integer>> l){
		path=new ArrayList<NetEdge>();
		for(int i=0;i<l.size();i++){
			Vector<Integer> v=l.get(i);
		if(v.size()>1){
			int from=v.get(0);
			for(int j=1;j<v.size();j++){
				int to=v.get(j);
				NetEdge be=new NetEdge(from,to);
				if(!contains(new NetEdge(from,to)))
					path.add(be);
				from=to;
			}
		}
		}
	}
	
	
	public int getSize(){
		return path.size();
	}
	
	public boolean contains(NetEdge e){
		Iterator<NetEdge> it=path.iterator();
		while(it.hasNext()){
			NetEdge be=it.next();
			if(be.equals(e))
				return true;
		}
		return false;
	}
	
	public boolean containsInverse(NetEdge e){
		NetEdge inv=new NetEdge(e.getTo(),e.getFrom());
		return contains(inv);
	}
	
	
	public NetEdge getEdge(int n){
		return this.path.get(n);
	}
	
	
	public ArrayList<NetEdge> intersect(NetPath p){
		ArrayList<NetEdge> list=new ArrayList<NetEdge>();
	    for(int i=0; i<p.getSize();i++){
	    	if(this.contains(p.getEdge(i)))
	    		list.add(p.getEdge(i));
	    }
		
		return list;
	}
	

}
