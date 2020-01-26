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
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

import jecoli.algorithm.components.configuration.InvalidConfigurationException;
import pt.uminho.algoritmi.netopt.ospf.graph.Graph.Status;
import pt.uminho.algoritmi.netopt.ospf.graph.MatDijkstra;
import pt.uminho.algoritmi.netopt.ospf.optimization.Params;
import pt.uminho.algoritmi.netopt.ospf.optimization.Params.AlgorithmSecondObjective;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.JecoliOSPF;
import pt.uminho.algoritmi.netopt.ospf.simulation.Demands;
import pt.uminho.algoritmi.netopt.ospf.simulation.EdgesLoad;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.NondominatedPopulation;
import pt.uminho.algoritmi.netopt.ospf.simulation.OSPFWeights;
import pt.uminho.algoritmi.netopt.ospf.simulation.Population;
import pt.uminho.algoritmi.netopt.ospf.simulation.Simul.LoadBalancer;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetEdge;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetNode;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.IntegerSolution;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.LabelPath;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.SRConfiguration;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.SRPathTranslator;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.SRConfiguration.SRConfigurationType;

public class TesteSRLFPLRReroute {

	/*
	private static String nodesFile = "/Users/vmsap/Documents/OSPFfiles/50_4/isno_50_4.nodes";
	private static String edgesFile = "/Users/vmsap/Documents/OSPFfiles/50_4/isno_50_4.edges";
	private static String demandsFile = "/Users/vmsap/Documents/OSPFfiles/50_4/isno_50_4-D0.3.dem";
	private static String weightsFile = "/Users/vmsap/Documents/OSPFfiles/50_4/50_4.listw";
	*/
	public static void main(String[] args) throws InvalidConfigurationException, Exception {

		System.out.println(Arrays.toString(args));
		
		if(args.length!=4)
			System.exit(1);


		
		String nodesFile = args[0];
		String edgesFile = args[1];
		String demandsFile = args[2];
		String weightsFile = args[3];
		
		NetworkTopology topology = new NetworkTopology(nodesFile, edgesFile);
		Demands demands = new Demands(topology.getDimension(), demandsFile);
		OSPFWeights w = new OSPFWeights(topology.getDimension());
		w.readOSPFWeightsList(topology, weightsFile);

		Params parameters = new Params();
		parameters.setLoadBalancer(LoadBalancer.DEFT);
		parameters.setSecondObjective(AlgorithmSecondObjective.MLU);
		parameters.setArchiveSize(100);
		parameters.setPopulationSize(100);
		parameters.setPreviousWeights(w);
		parameters.setNumberGenerations(10);

		// pre-convergence congestion values
		double[] congestions =new double[topology.getNetGraph().getNEdges()];

		EdgesLoad el = new EdgesLoad();
		el.computeLoads(topology, w, LoadBalancer.DEFT, true);
		
		// reroute at provider's edge
		for (int i = 0; i < topology.getNetGraph().getNEdges(); i++) {

			NetEdge e = topology.getNetGraph().getEdge(i);
			// demands that need to be rerouted
			Demands[] d = el.getPointofRecoveryPartialDemand(demands, e,EdgesLoad.EdgeNodeIn.LEFT_RIGHT);
			topology.setEdgeStatus(e, Status.DOWN);
			JecoliOSPF ea = new JecoliOSPF(topology, d, null);
			ea.configureSRLMTNSGAII(parameters);
			ea.run();
			Population p = new NondominatedPopulation(ea.getSolutionSet());
			IntegerSolution sol=p.getLowestValuedSolutions(0, 1).get(0);
			
			
			int[] s=sol.getVariables().stream().mapToInt(Integer::intValue).toArray();
			int[] s1=Arrays.copyOfRange(s, 0,topology.getNumberEdges());
			int[] s2=Arrays.copyOfRange(s, topology.getNumberEdges(),2*topology.getNumberEdges());
			
			NetworkTopology topo=topology.copy();
			SRPathTranslator translator= new SRPathTranslator(topology,w);
			MatDijkstra spGraph=topo.getShortestPathGraph();
			spGraph.setSSP(true);
			//Translate s1 
			topo.applyWeights(s1);
		    Demands d1= d[1];
		    
		    
		    SRConfiguration conf=new SRConfiguration();
			conf.addFailedLink(e);
			conf.setType(SRConfigurationType.LINK_FAILURE);
			
		    
		    for(int source=0;source<topology.getDimension();source++)
		    	for(int dest=0;dest<topology.getDimension();dest++){
		    		if(d1.getDemands(source,dest)!=0){
		    			Vector<Integer> path=spGraph.getPath(source, dest);
		    			List<NetNode> nodePath=path.stream().map(x->topology.getNetGraph().getNodeByID(x)).collect(Collectors.toList());
		    			LabelPath lp=translator.translate(nodePath);
		    			conf.addLabelPath(lp);
		    		}
		    	}
		    
		   //Translate s2
			topo.applyWeights(s2);
		    Demands d2= d[2];
		    for(int source=0;source<topology.getDimension();source++)
		    	for(int dest=0;dest<topology.getDimension();dest++){
		    		if(d2.getDemands(source,dest)!=0){
		    			Vector<Integer> path=spGraph.getPath(source, dest);
		    			List<NetNode> nodePath=path.stream().map(x->topology.getNetGraph().getNodeByID(x)).collect(Collectors.toList());
		    			LabelPath lp=translator.translate(nodePath);
		    			conf.addLabelPath(lp);
		    		}
		    	}
		    
			//do something with the new configuration
		    System.out.println(conf.statistics().toString());
		    
			congestions[i] = sol.getFitnessValue(0);
			topology.setEdgeStatus(e, Status.UP);
		}
		
		String filename="out"+System.currentTimeMillis()+".csv";
		StringBuffer sb = new StringBuffer();
		int n = congestions.length;
		for (int i = 0; i < n; i++) {
			sb.append(i).append(";").append(congestions[i]).append("\n");
		}
		
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
	}

}
