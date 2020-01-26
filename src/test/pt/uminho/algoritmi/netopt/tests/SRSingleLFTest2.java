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
package pt.uminho.algoritmi.netopt.tests;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import pt.uminho.algoritmi.netopt.ospf.graph.Graph;
import pt.uminho.algoritmi.netopt.ospf.graph.Graph.Status;
import pt.uminho.algoritmi.netopt.ospf.optimization.Params;
import pt.uminho.algoritmi.netopt.ospf.optimization.Params.AlgorithmSecondObjective;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.JecoliOSPF;
import pt.uminho.algoritmi.netopt.ospf.simulation.Demands;
import pt.uminho.algoritmi.netopt.ospf.simulation.EdgesLoad;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkLoads;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.NondominatedPopulation;
import pt.uminho.algoritmi.netopt.ospf.simulation.OSPFWeights;
import pt.uminho.algoritmi.netopt.ospf.simulation.Population;
import pt.uminho.algoritmi.netopt.ospf.simulation.Simul.LoadBalancer;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetEdge;
import pt.uminho.algoritmi.netopt.ospf.simulation.simulators.SRSimul;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.SRConfiguration;

public class SRSingleLFTest2 {

	private static String nodesFile = "/Users/vmsap/Documents/OSPFfiles/50_4/isno_50_4.nodes";
	private static String edgesFile = "/Users/vmsap/Documents/OSPFfiles/50_4/isno_50_4.edges";
	private static String demandsFile = "/Users/vmsap/Documents/OSPFfiles/50_4/isno_50_4-D0.3.dem";
	private static String weightsFile = "/Users/vmsap/Documents/OSPFfiles/50_4/50_4.listw";

	public static void main(String[] args) {

		try {
			NetworkTopology topology = new NetworkTopology(nodesFile, edgesFile);
			Demands demands = new Demands(topology.getDimension(), demandsFile);
			OSPFWeights w = new OSPFWeights(topology.getDimension());
			w.readOSPFWeightsList(topology, weightsFile);

			SRSimul simul = new SRSimul(topology);
			simul.setConfigureSRPath(true);
			simul.evalPValues(null, w.asIntArray(), demands); 
			SRConfiguration sr = simul.getSRconfiguration();

			System.out.println("\nInitial evaluation ");
			System.out.println(simul.getLoads().getCongestion());

			double sum = 0.0;
			for (int i = 0; i < topology.getNetGraph().getNEdges(); i++) {

				NetEdge e = topology.getNetGraph().getEdge(i);
				topology.setEdgeStatus(e, Graph.Status.DOWN);

				double[][] tloads = simul.totalLoads(demands, w, sr, false);
				NetworkLoads l = new NetworkLoads(topology);
				l.setLoads(tloads);
				double d = simul.congestionMeasure(l, demands);
				System.out.println(d);
				sum += d;
				topology.setEdgeStatus(e, Graph.Status.UP);
			}
			System.out.println("média=" + sum / topology.getNetGraph().getNEdges());
			sum = 0.0;
			for (int i = 0; i < topology.getNetGraph().getNEdges(); i++) {
				NetEdge e = topology.getNetGraph().getEdge(i);
				topology.setEdgeStatus(e, Graph.Status.DOWN);

				double[][] tloads = simul.totalLoads(demands, w, sr, false);
				NetworkLoads l = new NetworkLoads(topology);
				l.setLoads(tloads);
				double d = simul.congestionMeasure(l, demands);
				System.out.println(d);
				sum += d;
				topology.setEdgeStatus(e, Graph.Status.UP);
			}
			System.out.println("media=" + sum / topology.getNetGraph().getNEdges());

			Params params = new Params();
			params.setArchiveSize(100);
			params.setLoadBalancer(LoadBalancer.DEFT);
			params.setNumberGenerations(100);
			params.setPopulationSize(100);
			params.setPreviousWeights(w);
			params.setSecondObjective(AlgorithmSecondObjective.MLU);

			params.setPreviousWeights(w);

			EdgesLoad el = new EdgesLoad();
			el.computeLoads(topology, w, LoadBalancer.DEFT, true);

			ArrayList<Double> results =new ArrayList<Double>();
			// reroute at provider's edge
			double soma=0.0;
			for (int i = 0; i < topology.getNetGraph().getNEdges(); i++) {

				NetEdge e = topology.getNetGraph().getEdge(i);
				// demands that need to be rerouted
				Demands[] d = el.getEdgeToEdgePartialDemand(demands, e, EdgesLoad.EdgeNodeIn.BOTH);
				topology.setEdgeStatus(e, Status.DOWN);
				JecoliOSPF ea = new JecoliOSPF(topology, d, null);
				ea.configureSRLMTNSGAII(params);
				ea.run();
				Population p = new NondominatedPopulation(ea.getSolutionSet());
				double r = p.getLowestValuedSolutions(0, 1).get(0).getFitnessValue(0);
				results.add(r);
				soma+=r;
				topology.setEdgeStatus(e, Status.UP);
			}
			double avg=(soma/results.size());
			
						
			String filename="out"+System.currentTimeMillis()+".csv";
			StringBuffer sb = new StringBuffer();
			int n = results.size();
			for (int i = 0; i < n; i++) {
				sb.append(i).append(";").append(results.get(i)).append("\n");
			}
			sb.append(avg).append(";").append(avg).append("\n");
			
			FileWriter f;
			try {
				f = new FileWriter(filename, true);
				BufferedWriter W = new BufferedWriter(f);
				W.write(sb.toString());
				W.write("\n");
				W.flush();
				W.close();
				f.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
