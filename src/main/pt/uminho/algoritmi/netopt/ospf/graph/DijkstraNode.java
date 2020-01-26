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
import java.util.Vector;


/** Holds information about graph nodes within the shortest path algorithm (Dijkstra) */
@SuppressWarnings("serial")
public class DijkstraNode implements Serializable
{
	int node; // number of node in Graph (Graph should be a instance of WGraph)
	int source; // source node from the algorithm (redundant ?)
	int predec; // predecessor of node
	double distance; // distance between
	boolean alternative; // if true there is an alternative path between source and node with equal distance

	public DijkstraNode(int n, int s, int pred, double d)
	{
		this.node = n;
		this.source = s;
		this.predec = pred;
		this.distance = d;
		this.alternative = false;
	}	

	public int getNode()
	{	
		return this.node;
	}

	public void setSource(int s)
	{
		this.source = s;
	}

	public int getSource()
	{	
		return this.source;
	}

	public void setPred(int p)
	{
		this.predec = p;
	}

	public int getPred()
	{	
		return this.predec;
	}

	public void setDist(double d)
	{
		this.distance = d;
	}

	public double getDist()
	{	
		return this.distance;
	}

	public boolean isAlternative ()
	{
		return this.alternative;
	}

	public void setAlternative (boolean val)
	{
		this.alternative = val;
	}	

	public void print()
	{
		System.out.println("Source: " + source + "; Node: " + node + "; Pred. " + predec + "; Dist.:" + distance);  
	}

	public void print_simp ()
	{
		System.out.print(predec + "-" + node);  	
	}
	
	public static void print (Vector<?> nodes)
	{
		for (int i=0; i< nodes.size(); i++)
				((DijkstraNode)nodes.get(i)).print();
	}
	
	public static void print_simp (Vector<?> nodes)
	{
		for (int i=0; i< nodes.size(); i++)
		{
				((DijkstraNode)nodes.get(i)).print_simp();
				System.out.print(" ");
		}
		System.out.println("");
	}

}
