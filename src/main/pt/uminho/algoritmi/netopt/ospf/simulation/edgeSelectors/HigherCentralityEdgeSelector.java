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
package pt.uminho.algoritmi.netopt.ospf.simulation.edgeSelectors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import pt.uminho.algoritmi.netopt.ospf.graph.MatDijkstra;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetEdge;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetPath;
import pt.uminho.algoritmi.netopt.ospf.simulation.simulators.ISimulator;

/**
 * 
 * @author Vitor
 *
 * Selector by occurrence in paths  
 *
 */


public class HigherCentralityEdgeSelector extends DefaultEdgeSelector implements 
Comparator<NetEdge> {

	int[][] edges;

	
	public HigherCentralityEdgeSelector(ISimulator simul) {
		super(simul);
		int dimension = simul.getTopology().getDimension();
		edges = new int[dimension][dimension];
	}

	@Override
	public Iterator<NetEdge> getIterator() {

		MatDijkstra dijkstra = simul.getTopology().getEuclidianDistanceSPGraph();
		int dimension = simul.getTopology().getDimension();
		edges = new int[dimension][dimension];
		for (int source = 0; source < dimension; source++)
			for (int dest = 0; dest < dimension; dest++) {
				NetPath p = new NetPath(dijkstra.getAllPaths(source, dest));
				int size = p.getSize();
				for (int i = 0; i < size; i++) {
					NetEdge e = p.getEdge(i);
					edges[e.getFrom()][e.getTo()]++;
				}
			}
		ArrayList<NetEdge> list = new ArrayList<NetEdge>(
				Arrays.asList(simul.getTopology().getNetGraph().getEdges()));
		Collections.sort(list, this);
		Collections.reverse(list);
		return list.iterator();
	}

	@Override
	public int compare(NetEdge e1, NetEdge e2) {
		return (edges[e1.getFrom()][e1.getTo()]+edges[e1.getTo()][e1.getFrom()])- 
			   (edges[e2.getFrom()][e2.getTo()]+edges[e2.getTo()][e2.getFrom()]);
	}
}
