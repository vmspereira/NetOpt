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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import pt.uminho.algoritmi.netopt.ospf.simulation.Demands;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkLoads;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.OSPFWeights;
import pt.uminho.algoritmi.netopt.ospf.simulation.PValues;
import pt.uminho.algoritmi.netopt.ospf.simulation.Simul;
import pt.uminho.algoritmi.netopt.ospf.simulation.exception.DimensionErrorException;
import pt.uminho.algoritmi.netopt.ospf.simulation.loadballancer.GammaLoadBalancer;

// DEFT/ PEFT use a different distribution of traffic across the links topology

@SuppressWarnings("serial")
public class PDEFTSimul extends Simul implements ISimulator, IMultiDemandSimulator {

	

	public PDEFTSimul(NetworkTopology topology) {
		this(topology, LoadBalancer.DEFT);
	}

	
	public PDEFTSimul(NetworkTopology topology, boolean usedeft) {
		super(topology);
		LoadBalancer lb = (usedeft==true)?LoadBalancer.DEFT:LoadBalancer.PEFT;
		this.setLoadBalancer(lb);
	}
	
	public PDEFTSimul(NetworkTopology topology, LoadBalancer lb) {
		super(topology);
		this.setLoadBalancer(lb);
	}

	@Override
	public void computeLoads(OSPFWeights weights, Demands demands) throws DimensionErrorException {

		this.loads = new NetworkLoads(topology);
		topology.applyWeights(weights);
		double[][] tl = totalLoads(demands);
		this.loads.setLoads(tl);
		this.loads.setCongestion(congestionMeasure(loads, demands));
	}

	
	public void computeLoads(OSPFWeights weights, Demands demands,PValues pvalues) throws DimensionErrorException {

		this.loads = new NetworkLoads(topology);
		topology.applyWeights(weights);
		double[][] tl = totalLoads(pvalues.getPValues(),demands);
		this.loads.setLoads(tl);
		this.loads.setCongestion(congestionMeasure(loads, demands));
	}

	
	@Override
	public double[][] totalLoads(Demands demands) {
		
		int dimension = topology.getGraph().getDimension();
		double[][] tLoads = new double[dimension][dimension];
		for (int i = 0; i < dimension; i++)
			for (int j = 0; j < dimension; j++)
				tLoads[i][j] = 0.0;

		for (int d = 0; d < dimension; d++) {
			if (topology.getGraph().inDegree(d) > 0) {
				double[][] ploads = partialLoads(d, demands);
				for (int i = 0; i < dimension; i++)
					for (int j = 0; j < dimension; j++)
						tLoads[i][j] += ploads[i][j];
			}
		}

		return tLoads;
	}


	@Override
	public double[][] partialLoads(int dest, Demands demands) {
		GammaLoadBalancer g = new GammaLoadBalancer(topology, dest, this.getLoadBalancer());
		g.computeGamma();
		double[][] ploads = new double[topology.getDimension()][topology.getDimension()];
		for (int j = 0; j < topology.getDimension(); j++)
			for (int k = 0; k < topology.getDimension(); k++)
				ploads[j][k] = 0.0;
		ArrayList<Integer> nodes = new ArrayList<Integer>(topology.getShortestPathGraph().getNodesForDest(dest));

		// order nodes by distance to destination
		nodes.sort(new Comparator<Integer>() {
			double[][] dists = topology.getShortestPathGraph().getShortestPathDistances();

			@Override
			public int compare(Integer arg0, Integer arg1) {
				double d0 = dists[arg0][dest];
				double d1 = dists[arg1][dest];
				if (d0 - d1 > 0)
					return -1;
				else if (d0 == d1)
					return 0;
				else
					return 1;
			}
		});

		Iterator<Integer> it = nodes.iterator();
		while (it.hasNext()) {

			int current = it.next();
			// sum of all traffic that arrives to the current node v
			double suml = 0.0;
			for (int u = 0; u < topology.getDimension(); u++) {
				if (topology.getNetGraph().existEdge(u, current) && topology.getNetGraph().getEdge(u, current).isUP())
					suml += ploads[u][current];
			}
			// load to be forwarded from current
			double load = demands.getDemands(current, dest) + suml;
			// split load
			for (int i = 0; i < topology.getDimension(); i++) {
				ploads[current][i] = g.getSplit(current, i) * load;
			}
		}
		return ploads;
	}

	public double[] evalWeightsMO(int[] weights, boolean computeDemands1, boolean computeDemands2, boolean timeDebug,
			Demands demands1, Demands demands2) {

		NetworkLoads loads;

		double res1 = 0.0;
		double res2 = 0.0;

		if (computeDemands1) {
			topology.applyWeights(weights);
			loads = new NetworkLoads(totalLoads(demands1), topology);
			res1 = congestionMeasure(loads, demands1);
		}

		if (computeDemands2) {
			topology.applyWeights(weights);
			loads = new NetworkLoads(totalLoads(demands2), topology);
			res2 = congestionMeasure(loads, demands2);
		}

		double[] result = new double[2];
		result[0] = res1;
		result[1] = res2;
		return result;
	}

	public double evalWeights(int[] weights, double alfa, Demands demands, Demands demands2) {
		double[] res = evalWeightsMO(weights, alfa > 0, alfa < 1, false, demands, demands2);
		return (alfa * res[0] + (1.0 - alfa) * res[1]);
	}

	public double evalPValues(double[] pValues, int[] weights, Demands demands) {
		// NetworkLoads loads;

		topology.applyWeights(weights);
		topology.shortestDistances();
		double res = 0.0;
		loads = new NetworkLoads(totalLoads(pValues, demands), topology);
		res = congestionMeasure(loads, demands);
		loads.setCongestion(res);
		return res;
	}

	public double[][] totalLoads(double[] pvalues, Demands demands) {
		int dimension = topology.getDimension();

		double[][] tLoads = new double[dimension][dimension];
		for (int i = 0; i < dimension; i++)
			for (int j = 0; j < dimension; j++)
				tLoads[i][j] = 0.0;

		for (int d = 0; d < dimension; d++) {
			if (topology.getGraph().inDegree(d) > 0) {
				double[][] ploads = this.partialLoads(d, demands, pvalues);
				for (int i = 0; i < dimension; i++)
					for (int j = 0; j < dimension; j++)
						tLoads[i][j] += ploads[i][j];
			}
		}
		return tLoads;
	}

	public double[][] partialLoads(int dest, Demands demands, double[] pvalues) {

		GammaLoadBalancer gam = new GammaLoadBalancer(this.topology, dest, this.getLoadBalancer(), pvalues);
		gam.computeGamma();

		double[][] ploads = new double[topology.getDimension()][topology.getDimension()];
		for (int j = 0; j < topology.getDimension(); j++)
			for (int k = 0; k < topology.getDimension(); k++)
				ploads[j][k] = 0.0;
		ArrayList<Integer> nodes = new ArrayList<Integer>(topology.getShortestPathGraph().getNodesForDest(dest));

		// order nodes by distance to destination
		nodes.sort(new Comparator<Integer>() {
			double[][] dists = topology.getShortestPathGraph().getShortestPathDistances();

			@Override
			public int compare(Integer arg0, Integer arg1) {
				double d0 = dists[arg0][dest];
				double d1 = dists[arg1][dest];
				if (d0 - d1 > 0)
					return -1;
				else if (d0 == d1)
					return 0;
				else
					return 1;
			}
		});

		Iterator<Integer> it = nodes.iterator();
		while (it.hasNext()) {

			int current = it.next();
			if (current != dest) {
				// sum of all traffic that arrives to the current node v
				double suml = 0.0;
				for (int u = 0; u < topology.getDimension(); u++) {
					if (topology.getNetGraph().existEdge(u, current)
							&& topology.getNetGraph().getEdge(u, current).isUP())
						suml += ploads[u][current];
				}
				// load to be forwarded from current
				double load = demands.getDemands(current, dest) + suml;
				// split load
				for (int i = 0; i < topology.getDimension(); i++) {
					ploads[current][i] = gam.getSplit(current, i) * load;
				}
			}
		}
		return ploads;

	}

	@Override
	public double evalWeightsMLU(int[] weights, double alpha, Demands demands) {
		double[] res= evalWeightsMOMLU(weights,demands);
		//needs a normalization
		return (alpha * res[0] + (1.0 - alpha) * res[1]);
	}

	@Override
	public double[] evalWeightsMOMLU(int[] weights, Demands demands) {
		NetworkLoads loads;

		double res1 = 0.0;
		double res2 = 0.0;

		topology.applyWeights(weights);
		loads = new NetworkLoads(totalLoads(demands), topology);
		res1 = congestionMeasure(loads, demands);
		res2 = loads.getMLU();
		double[] result = new double[2];
		result[0] = res1;
		result[1] = res2;
		return result;
	}

	
	@Override
	public double evalWeightsALU(int[] weights, double alpha, Demands demands) {
		double[] res= evalWeightsMOALU(weights,demands);
		//needs a normalization
		return (alpha * res[0] + (1.0 - alpha) * res[1]);
	}

	@Override
	public double[] evalWeightsMOALU(int[] weights, Demands demands) {
		NetworkLoads loads;

		double res1 = 0.0;
		double res2 = 0.0;

		topology.applyWeights(weights);
		loads = new NetworkLoads(totalLoads(demands), topology);
		res1 = congestionMeasure(loads, demands);
		res2 = loads.getALU();
		double[] result = new double[2];
		result[0] = res1;
		result[1] = res2;
		return result;
	}
}
