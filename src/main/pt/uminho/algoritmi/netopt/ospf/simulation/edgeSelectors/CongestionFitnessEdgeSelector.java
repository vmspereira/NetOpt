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

import pt.uminho.algoritmi.netopt.ospf.graph.Graph;
import pt.uminho.algoritmi.netopt.ospf.simulation.Demands;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.Simul;
import pt.uminho.algoritmi.netopt.ospf.simulation.exception.DimensionErrorException;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetEdge;
import pt.uminho.algoritmi.netopt.ospf.simulation.simulators.ISimulator;



/**
 * 
 * @author Vitor
 * Edge Selector by failure fitness for a given OSPF weights configuration
 *
 */

public class CongestionFitnessEdgeSelector extends DefaultEdgeSelector
		implements Comparator<NetEdge> {

	double[][] fitness;
	Demands demands;

	public CongestionFitnessEdgeSelector(ISimulator simul) {
		super(simul);
		int dimension = simul.getTopology().getDimension();
		fitness = new double[dimension][dimension];
	}

	@Override
	public Iterator<NetEdge> getIterator() {

		// evaluates fitness for each link failure
		NetworkTopology topology = this.simul.getTopology().copy();
		Simul fsimul = new Simul(topology);
		int dimension = simul.getTopology().getDimension();
		for (int i = 0; i < dimension; i++)
			for (int j = i + 1; j < dimension; j++) {
				if (fsimul.getTopology().getGraph().getConnection(i, j)
						.equals(Graph.Status.UP)) {
					fsimul.getTopology().getGraph().setConnection(i, j,Graph.Status.DOWN);
					try {
						fsimul.computeLoads(this.weights, demands);
						double congestion=fsimul.getLoads().getCongestion();
						fitness[i][j]=congestion;
					} catch (DimensionErrorException e) {
						e.printStackTrace();
					}
					fsimul.getTopology().getGraph().setConnection(i, j,Graph.Status.UP);
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
		double d1 = fitness[e1.getFrom()][e1.getTo()];
		double d2 = fitness[e2.getFrom()][e2.getTo()];
		return Double.compare(d1, d2);
	}
}
