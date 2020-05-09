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
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

import jecoli.algorithm.components.configuration.InvalidConfigurationException;
import pt.uminho.algoritmi.netopt.ospf.graph.Graph;
import pt.uminho.algoritmi.netopt.ospf.graph.Graph.Status;
import pt.uminho.algoritmi.netopt.ospf.graph.MatDijkstra;
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
import pt.uminho.algoritmi.netopt.ospf.simulation.Simul;
import pt.uminho.algoritmi.netopt.ospf.simulation.Simul.LoadBalancer;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetEdge;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetNode;
import pt.uminho.algoritmi.netopt.ospf.simulation.simulators.SRSimul;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.IntegerSolution;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.LabelPath;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.SRConfiguration;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.SRPathTranslator;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.SRConfiguration.SRConfigurationType;

public class ExportToMatlab {

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
		try{
			w.readOSPFWeightsList(topology, weightsFile);
		}
		catch(Exception e){
			w.readOSPFWeights(topology.getDimension(), weightsFile);
		}
		
		
		String separator=" ";

		/*
		StringBuffer bf= new StringBuffer();
		
		for(int i=0;i<topology.getDimension();i++)
			for(int j=0;j<topology.getDimension();j++)
				if(topology.getNetGraph().existEdge(i,j)){
					bf.append(i+1).append(separator);
					bf.append(j+1).append(separator);
					bf.append(w.getWeight(i,j)).append(separator);
					bf.append(topology.getNetGraph().getEdge(i,j).getBandwidth()).append(separator);
					bf.append(topology.getNetGraph().getEdge(i,j).getLength()).append(separator);
					bf.append(";\n");
				}
		
		System.out.println("G=["+bf.toString()+"];");
		
		
		StringBuffer d= new StringBuffer();
		
		for(int i=0;i<topology.getDimension();i++)
			for(int j=0;j<topology.getDimension();j++)
				if(i!=j){
					d.append(i+1).append(separator);
					d.append(j+1).append(separator);
					d.append(demands.getDemands(i, j)).append(separator);
					d.append(";\n");
				}

		
		
		System.out.println("% demand matrix \nD=["+d.toString()+"];");
		*/
		
		StringBuffer wb = new StringBuffer();
		StringBuffer adj = new StringBuffer();
		topology.applyWeights(w);
		double[][] wg=topology.getWeights();
		for(int i=0;i<topology.getDimension();i++){
			for(int j=0;j<topology.getDimension();j++){
					wb.append(wg[i][j]).append(separator);
					adj.append(wg[i][j]>0?1:0).append(separator);	
			}
			wb.append(";\n");
			adj.append(";\n");
			}
				
		System.out.println("% weights adjacency matrix \nW=["+wb.toString()+"];");
		
		System.out.println("% Adjacency matrix \nA=["+adj.toString()+"];");
		
		
		StringBuffer cb = new StringBuffer();
		double[][] cp=topology.getNetGraph().createGraph().getCapacitie();
		for(int i=0;i<topology.getDimension();i++){
			for(int j=0;j<topology.getDimension();j++)
					cb.append(cp[i][j]).append(separator);
			cb.append(";\n");
			}
				
		System.out.println("% capacity matrix \nC=["+cb.toString()+"];");
		
		
		
		
		StringBuffer db = new StringBuffer();
		topology.applyWeights(w);
		for(int i=0;i<topology.getDimension();i++){
			for(int j=0;j<topology.getDimension();j++)
					db.append(demands.getDemands(i, j)).append(separator);
			db.append(";\n");
			}
		
		System.out.println("% Demands \nD=["+db.toString()+"];");
		
		
		StringBuffer code = new StringBuffer();
		code.append("%% plot the topology");
		code.append("g = digraph(W);");
		code.append("plot(g,'EdgeLabel',g.Edges.Weight);");
		
		
		
		
		
		
		
	}

}
