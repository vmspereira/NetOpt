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
package pt.uminho.algoritmi.netopt;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import jecoli.algorithm.components.configuration.InvalidConfigurationException;
import pt.uminho.algoritmi.netopt.ospf.graph.Graph;
import pt.uminho.algoritmi.netopt.ospf.graph.MatDijkstra;
import pt.uminho.algoritmi.netopt.ospf.graph.Graph.Status;
import pt.uminho.algoritmi.netopt.ospf.optimization.Params;
import pt.uminho.algoritmi.netopt.ospf.optimization.ResultOptim;
import pt.uminho.algoritmi.netopt.ospf.optimization.Params.AlgorithmSecondObjective;
import pt.uminho.algoritmi.netopt.ospf.optimization.Params.EdgeSelectionOption;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.JecoliOSPF;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.JecoliPValueInteger;
import pt.uminho.algoritmi.netopt.ospf.simulation.DelayRequests;
import pt.uminho.algoritmi.netopt.ospf.simulation.Demands;
import pt.uminho.algoritmi.netopt.ospf.simulation.EdgesLoad;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkLoads;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.NondominatedPopulation;
import pt.uminho.algoritmi.netopt.ospf.simulation.OSPFWeights;
import pt.uminho.algoritmi.netopt.ospf.simulation.PValues;
import pt.uminho.algoritmi.netopt.ospf.simulation.Population;
import pt.uminho.algoritmi.netopt.ospf.simulation.Simul;
import pt.uminho.algoritmi.netopt.ospf.simulation.Simul.LoadBalancer;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetEdge;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetNode;
import pt.uminho.algoritmi.netopt.ospf.simulation.simulators.PDEFTSimul;
import pt.uminho.algoritmi.netopt.ospf.simulation.simulators.SRSimul;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.IntegerSolution;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.LabelPath;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.SRConfiguration;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.SRPathTranslator;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.SRSimulator;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.SRConfiguration.SRConfigurationType;

public class NetOPT {

	public enum Method {
		MULTI_TOPOLOGY,SRMTLFC,LINK_FAILURE, TWO_DEMANDS, DEMAND_DELAY, SR_LINK_FAILURE, SR_LINK_FAILURE_MO, HYBRID_SDN_IP, SRMTLF
	}

	public enum Algorithm {
		SOEA, SPEA2, NSGAII
	}

	private Method method;
	private Algorithm algorithm;

	private String nodesFile = "";
	private String edgesFile = "";
	private String firstDemandFile = "";
	private String secondDemandFile = "";
	private String delayFile = "";
	private String outfile = "outfile.csv";
	private double alpha = 1.0;
	private double beta = 1.0;
	private int iterations = 1000;
	private int numberMT = 2;
	private Demands[] demands;
	private DelayRequests delays;
	private String weightsFile;

	private String SEPARATOR = ";";
	// percentage of SDN nodes for hybrid IP/SDN DEFT
	private double[] NODESP = { 0.4 };

	Params params;
	NetworkTopology topology;

	private final int POPULATION_SIZE = 100;
	private final int ARCHIVE_SIZE = 100;

	public NetOPT() {
		params = new Params();
		params.setArchiveSize(ARCHIVE_SIZE);
		params.setPopulationSize(POPULATION_SIZE);
		Date d = new Date();
		outfile = "out" + d.getTime() + ".csv";
	}

	private final static String usage = "Usage:"
			+ "NetOPT m:<method> o:<algorithm> n:<nodes file> e:<edges file> [options]\n"

			+ "Methods: \n" 
			+ "  lf		link failure\n" 
			+ "  mt		multi topology\n" 
			+ "  2d		two demands\n"
			+ "  dd		demands and delay\n" 
			+ "  srlf  	SR Link Failure\n" 
			+ "  srlfmo	SR Link Failure MO\n"
			+ "  sralf 	SR all LF\n" 
			+ "  hsdn  	Hybrid SDN/IP\n"
			+ "  tm		multi demands optimization\n"
			+ "\n"
			+ "Options:\n"
			+ "  o:[so|spea2|nsgaii]		EA algorithm\n"
			+ "  d:<first demands file>    	file containing demands\n"
			+ "  d2:<second demands file>  	file containing second demands (if needed)\n"
			+ "  lb:[ECMP|DEFT|PEFT]		traffic load balancer\n"		
			+ "  w:<weigth>			file containing a weight configuration\n"
			+ "  dr:<delay file>           	delay request file (if needed)\n"
			+ "  alpha:<int>               	alpha value (default 1.0)\n"
			+ "  beta:<int>                	beta value (default 1.0)\n"
			+ "  g:<int>                   	number of generations (default 1000)\n"
			+ "  nmt:<int>                 	number of Multy Topologies (default 2)\n"
			+ "  c:<double between 0 and 1>	Expected mean congestion (default 0.3)\n"
			+ "  ntm:<int>			number of TM for multi TM optimizatio (default 100)\n"
			+ "  out:<filename>            	output file\n";

	public static void main(String[] args) {

		if (args.length == 0) {
			System.out.println(usage);
			System.exit(0);
		}

		NetOPT netOPT = new NetOPT();
		// parse options
		for (String s : args) {
			System.out.println(s);
		}
		netOPT.parseArgs(args);
		try {
			netOPT.execute();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void execute() throws Exception {
		topology = getTopology();
		readRestrictionFiles();

		switch (this.method) {
		case TWO_DEMANDS:
		case DEMAND_DELAY:
			executeOpt();
			return;
		case MULTI_TOPOLOGY:
			executeMultiTopology();
			return;
		case LINK_FAILURE:
			//executeLinkFailure();
			this.executeDEFTLF();
			return;
		case SR_LINK_FAILURE:
			executeSRLinkFailure();
			return;
		case SR_LINK_FAILURE_MO:
			executeSRLinkFailureMO();
			return;
		case HYBRID_SDN_IP:
			this.executeHybridSDNOptimization();
			return;
		case SRMTLF:
			this.executeSRMTLinkFailure();
			return;
		case SRMTLFC:
			executeCSRMTLinkFailure();
			return;		
		default:
			System.exit(0);
		}
	}

	private void readRestrictionFiles() {
		int dimension = topology.getDimension();
		demands = new Demands[2];
		demands[0] = this.getDemands(dimension);
		demands[1] = this.getSecondDemands(dimension);
		delays = this.getDelays(dimension);
	}

	/**
	 * Two Demands OSPF optimization
	 * 
	 * @throws Exception
	 */

	private void executeOpt() throws Exception {

		JecoliOSPF eaOspf = new JecoliOSPF(topology, demands, delays);
		if (this.method == Method.TWO_DEMANDS) {
			params.setSecondObjective(AlgorithmSecondObjective.DEMANDS);
		}
		if (this.algorithm == Algorithm.SOEA) {
			eaOspf.configureEvolutionaryAlgorithm(params);

		} else if (this.algorithm == Algorithm.NSGAII) {
			eaOspf.configureNSGAII(params);

		} else {
			eaOspf.configureSPEA2(params);
		}

		eaOspf.configureDefaultArchive(params);
		eaOspf.run();

		ResultOptim results = new ResultOptim();
		results.addDemands(demands[0]);
		if (this.method == Method.TWO_DEMANDS) {
			results.addDemands(demands[1]);
		} else
			results.setDelayReqs(delays);

		Population p;
		if (this.algorithm == Algorithm.SOEA)
			p = new Population(eaOspf.getSolutionSet());
		else
			p = new Population(eaOspf.getArchive());

		results.setPopulation(p);

		if (this.algorithm == Algorithm.SOEA) {
			int[] sol = eaOspf.getBestSolutionWeights();
			OSPFWeights weights = new OSPFWeights(topology.getDimension());
			weights.setWeights(sol, topology);
			Simul simul = new Simul(topology);

			double congestion = -1;
			double congestion2 = -1;
			double delaypenaly = -1;

			simul.computeLoads(weights, demands[0]);
			congestion = simul.getLoads().getCongestion();

			if (this.method == Method.TWO_DEMANDS) {
				simul.computeLoads(weights, demands[1]);
				congestion2 = simul.getLoads().getCongestion();
			} else {
				simul.computeDelays(weights, delays);
				delaypenaly = simul.getAverageEndToEndDelays().getDelayPenalties();
			}
			write(congestion, delaypenaly, congestion2, -1);

		} else {
			writeMOEA(p, runInfo(""));
		}

	}

	/**
	 * OSPF Single Link Failure Optimization
	 */
	private void executeLinkFailure() {

		JecoliOSPF eaospf = new JecoliOSPF(topology, demands, delays);
		params.setEdgeSelectionOption(EdgeSelectionOption.HIGHERLOAD);
		try {

			if (this.algorithm == Algorithm.SOEA) {
				eaospf.configureLinkFailureAlgorithm(params);

			} else if (this.algorithm == Algorithm.NSGAII) {
				eaospf.configureNSGAIILinkFailure(params);
			} else {
				eaospf.configureSPEA2LinkFailure(params);
			}

			eaospf.run();

			ResultOptim results = new ResultOptim();
			results.addDemands(demands[0]);

			NondominatedPopulation p;
			if (this.algorithm == Algorithm.SOEA)
				p = new NondominatedPopulation(eaospf.getSolutionSet());
			else
				p = new NondominatedPopulation(eaospf.getArchive());

			results.setPopulation(p);

			if (this.algorithm == Algorithm.SOEA) {

				int[] sol = eaospf.getBestSolutionWeights();
				OSPFWeights weights = new OSPFWeights(topology.getDimension());
				weights.setWeights(sol, topology);
				Simul simul = new Simul(topology);
				simul.computeLoads(weights, demands[0]);
				NetworkLoads loads = simul.getLoads();
				double congestion_before = loads.getCongestion();

				// Determina o link com maior carga
				double max = 0.0;
				int max_from = 0;
				int max_to = 0;
				for (int i = 0; i < loads.getLoads().length; i++) {
					for (int j = 0; j < loads.getLoads().length; j++) {
						if (loads.getLoads(i, j) > max) {
							max = loads.getLoads(i, j);
							max_from = i;
							max_to = j;
						}
					}
				}
				// altera o estado do link para down
				topology.getGraph().setConnection(max_from, max_to, Graph.Status.DOWN);
				topology.getGraph().setConnection(max_to, max_from, Graph.Status.DOWN);

				simul.computeLoads(weights, demands[0]);
				double congestion_after = simul.getLoads().getCongestion();
				// simul.computeDelays(weights, delays);
				// double
				// delaypenaly_after=simul.getAverageEndToEndDelays().getDelayPenalties();

				write(congestion_before, -1, congestion_after, -1);
			} else {
				writeMOEA(p, runInfo(""));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void executeDEFTLF() {


		JecoliOSPF eaospf = new JecoliOSPF(topology, demands, null);
		params.setLoadBalancer(LoadBalancer.DEFT);
		params.setSecondObjective(AlgorithmSecondObjective.MLU);

		try {
			eaospf.configureNSGAII(params);

			// TODO Auto-generated catch block

			eaospf.run();
			Population p = new NondominatedPopulation(eaospf.getSolutionSet());
			writeMOEA(p, runInfo("DEFT LF Optimization"));

			IntegerSolution s = p.getLowestValuedSolutions(0, 1).get(0);
			OSPFWeights w = new OSPFWeights(topology.getDimension());
			w.setWeights(s.getVariablesArray(), topology);
			StringBuffer sb = new StringBuffer();
			int generations = params.getNumberGenerations();
			params.setNumberGenerations(100);
			for (int i = 0; i < topology.getNetGraph().getNEdges(); i++) {
				NetEdge e = topology.getNetGraph().getEdge(i);
				topology.setEdgeStatus(e, Status.DOWN);
				Simul sim = new Simul(topology);
				sim.setLoadBalancer(LoadBalancer.DEFT);
				sim.computeLoads(w, demands[0]);
				double c =sim.getLoads().getCongestion();
				
				JecoliPValueInteger j= new JecoliPValueInteger(topology,demands[0],w);
				j.configureEvolutionaryAlgorithm(params);
				j.run();
				
				double[] pvalues = j.getBestSolutionReal();
				//ResultOptim results = new ResultOptim();
				//results.addDemands(demands[0]);
				//results.addWeights(w);
				//results.setPValues(new PValues(pvalues));
				PDEFTSimul simul = new PDEFTSimul(topology, true);
				double c2 = simul.evalPValues(pvalues, w.asIntArray(), demands[0]);
				sb.append(i).append(SEPARATOR).append(c).append(SEPARATOR).append(c2).append(SEPARATOR).append("\n");
				topology.setEdgeStatus(e, Status.UP);
			}
			try {
				FileWriter f = new FileWriter(this.getOutfile(), true);
				BufferedWriter bfw = new BufferedWriter(f);
				bfw.write(sb.toString());
				bfw.write("\n");
				bfw.flush();
				bfw.close();
				f.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			params.setNumberGenerations(generations);		
			params.setEdgeSelectionOption(EdgeSelectionOption.ALLEDGES);
			eaospf.configureNSGAIILinkFailure(params);
			
			eaospf.run();
			p = new NondominatedPopulation(eaospf.getSolutionSet());
			writeMOEA(p, runInfo("DEFT All EDGES LF Optimization"));

			s = p.getLowestValuedSolutions(0, 1).get(0);
			w = new OSPFWeights(topology.getDimension());
			w.setWeights(s.getVariablesArray(), topology);
			sb = new StringBuffer();
			for (int i = 0; i < topology.getNetGraph().getNEdges(); i++) {

				NetEdge e = topology.getNetGraph().getEdge(i);
				topology.setEdgeStatus(e, Status.DOWN);
				Simul sim = new Simul(topology);
				sim.setLoadBalancer(LoadBalancer.DEFT);
				sim.computeLoads(w, demands[0]);
				double c =sim.getLoads().getCongestion();
				
				
				JecoliPValueInteger j= new JecoliPValueInteger(topology,demands[0],w);
				j.configureEvolutionaryAlgorithm(params);
				j.run();
				
				double[] pvalues = j.getBestSolutionReal();
				ResultOptim results = new ResultOptim();
				results.addDemands(demands[0]);
				results.addWeights(w);
				results.setPValues(new PValues(pvalues));
				PDEFTSimul simul = new PDEFTSimul(topology, true);
				double c2 = simul.evalPValues(pvalues, w.asIntArray(), demands[0]);
				sb.append(i).append(SEPARATOR).append(c).append(SEPARATOR).append(c2).append(SEPARATOR).append("\n");
				topology.setEdgeStatus(e, Status.UP);
			}
			try {
				FileWriter f = new FileWriter(this.getOutfile(), true);
				BufferedWriter bfw = new BufferedWriter(f);
				bfw.write(sb.toString());
				bfw.write("\n");
				bfw.flush();
				bfw.close();
				f.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		
		
		
		
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * OSPF-MT Optimization
	 */
	private void executeMultiTopology() {

		int dimension = topology.getDimension();
		Demands demands = this.getDemands(dimension);
		Demands[] demand = new Demands[numberMT];

		Demands d = new Demands(demands.getDimension());
		d.setFilename(demands.getFilename() + " 1/" + numberMT + " part");

		for (int i = 0; i < demands.getDimension(); i++) {
			for (int j = 0; j < demands.getDimension(); j++) {
				d.setDemands(i, j, demands.getDemands(i, j) / numberMT);
			}
		}
		for (int i = 0; i < numberMT; i++) {
			demand[i] = d;
		}

		JecoliOSPF eaospf = new JecoliOSPF(topology, demand, null);

		try {
			eaospf.configureMultiLayerAlgorithm(params, numberMT);
			eaospf.run();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/*
	 * layered path optimization for SR traffic on link failure. Only traffic
	 * that used the failing link is rerouted
	 */
	private void executeSRMTLinkFailure() {
		try {

			Double[] congestions = new Double[topology.getNetGraph().getNEdges()];

			JecoliOSPF eaospf = new JecoliOSPF(topology, demands, null);
			params.setLoadBalancer(LoadBalancer.DEFT);
			params.setSecondObjective(AlgorithmSecondObjective.MLU);

			OSPFWeights w;
			Population p;
			if(this.weightsFile!=null){
				try{ 
					w= new OSPFWeights(this.weightsFile,topology.getDimension());
				}catch(Exception e){
					w = new OSPFWeights(this.weightsFile,topology);
				}
				
			}else{
				eaospf.configureSRNSGAII(params);
				eaospf.run();
				p = new NondominatedPopulation(eaospf.getSolutionSet());
				writeMOEA(p, runInfo("SR Optimization"));

				IntegerSolution s = p.getLowestValuedSolutions(0, 1).get(0);
				w = new OSPFWeights(topology.getDimension());
				w.setWeights(s.getVariablesArray(), topology);
			}
			// ***********************************
			// PARTIAL ML LOAD OVER SRLF SOLUTION
			// ************************************

			Params parameters = params.copy();
			parameters.setPreviousWeights(w);
			parameters.setNumberGenerations(100);
			
			
			EdgesLoad el = new EdgesLoad();
			el.computeLoads(topology, w, LoadBalancer.DEFT, true);

			StringBuffer sb = new StringBuffer();
		
			for (int i = 0; i < topology.getNetGraph().getNEdges(); i++) {
				System.out.println(""+i);
				NetEdge e = topology.getNetGraph().getEdge(i);
				// demands that need to be rerouted
				Demands[] d = el.getEdgeToEdgePartialDemand(demands[0], e, EdgesLoad.EdgeNodeIn.LEFT_RIGHT);
				topology.setEdgeStatus(e, Status.DOWN);
				JecoliOSPF ea = new JecoliOSPF(topology, d, null);
				ea.configureSRLMTNSGAII(parameters);
				ea.run();
				p = new NondominatedPopulation(ea.getSolutionSet());
				congestions[i] = p.getLowestValuedSolutions(0, 1).get(0).getFitnessValue(0);
				IntegerSolution sol=p.getLowestValuedSolutions(0, 1).get(0);
				
				
				int[] s0=sol.getVariables().stream().mapToInt(Integer::intValue).toArray();
				int[] s1=Arrays.copyOfRange(s0, 0,topology.getNumberEdges());
				int[] s2=Arrays.copyOfRange(s0, topology.getNumberEdges(),2*topology.getNumberEdges());
				
				NetworkTopology topo=topology.copy();
				SRPathTranslator translator= new SRPathTranslator(topology,w);
				MatDijkstra spGraph=topo.getShortestPathGraph();
				spGraph.setSSP(true);
				
				
				SRConfiguration conf=new SRConfiguration();
				conf.addFailedLink(e);
				conf.setType(SRConfigurationType.LINK_FAILURE);
				
				SRConfiguration conf2=new SRConfiguration();
				conf2.addFailedLink(e);
				conf2.setType(SRConfigurationType.LINK_FAILURE);
				
							
				//Translate s1 
				topo.applyWeights(s1);
			    Demands d1= d[1];
			    
			    
			    for(int source=0;source<topology.getDimension();source++)
			    	for(int dest=0;dest<topology.getDimension();dest++){
			    		if(d1.getDemands(source,dest)!=0){
			    			Vector<Integer> path=spGraph.getPath(source, dest);
			    			List<NetNode> nodePath=path.stream().map(x->topology.getNetGraph().getNodeByID(x)).collect(Collectors.toList());
			    			LabelPath lp=translator.translate(nodePath);
			    			conf.addLabelPath(lp);
			    			LabelPath lp2=translator.translate(nodePath,3);
			    			conf2.addLabelPath(lp2);
			    			
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
			    			LabelPath lp2=translator.translate(nodePath,3);
			    			conf2.addLabelPath(lp2);
			    		}
			    	}
			    
				//do something with the new configuration
			    
			    SRSimulator simulator = new SRSimulator(topology,w);
			    SRSimul sim =new SRSimul(topology);
			    sim.setConfigureSRPath(true);
			    sim.computeLoads(w, d[0]);
			    SRConfiguration initConf = sim.getSRconfiguration();
			    simulator.apply(initConf, d[0]);
			    simulator.apply(conf, d[1]);
			    simulator.apply(conf2, d[2]);
			    double v = simulator.getCongestionValue();
			    
			    
			    Properties stat=conf.statistics();
			    sb.append(i).append(SEPARATOR).append(congestions[i]).append(SEPARATOR)
			                                   .append(stat.getProperty("size")).append(SEPARATOR)
			                                   .append(stat.getProperty("mean")).append(SEPARATOR)
			                                   .append(stat.getProperty("min")).append(SEPARATOR)
			                                   .append(stat.getProperty("max")).append(SEPARATOR)
			                                   .append(stat.getProperty("std")).append(SEPARATOR)
			                                   .append(stat.getProperty("n3")).append(SEPARATOR)
			                                   .append(v).append(SEPARATOR)
			                                   .append("\n");
				topology.setEdgeStatus(e, Status.UP);
			}
			writeArray(congestions, "SR + ML OPT Links congestions");
			try {
				FileWriter f = new FileWriter(this.getOutfile(), true);
				BufferedWriter bfw = new BufferedWriter(f);
				bfw.write(sb.toString());
				bfw.write("\n");
				bfw.flush();
				bfw.close();
				f.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


		



			// reroute at PLR
		    /*
			for (int i = 0; i < topology.getNetGraph().getNEdges(); i++) {

				NetEdge e = topology.getNetGraph().getEdge(i);
				// demands that need to be rerouted
				Demands[] d = el.getPointofRecoveryPartialDemand(demands[0], e, EdgesLoad.EdgeNodeIn.LEFT_RIGHT);
				topology.setEdgeStatus(e, Status.DOWN);
				JecoliOSPF ea = new JecoliOSPF(topology, d, null);
				ea.configureSRLMTNSGAII(parameters);
				ea.run();
				p = new NondominatedPopulation(ea.getSolutionSet());
				congestions[i] = p.getLowestValuedSolutions(0, 1).get(0).getFitnessValue(0);
				topology.setEdgeStatus(e, Status.UP);
			}
			writeArray(congestions, "SR + ML OPT Links congestions at PLR");
			*/
	
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			Population pop = new Population(2);

			int[] v=w.asIntArray();
			List<Integer> l= IntStream.of(v).boxed().collect(Collectors.toList());
			l.addAll(IntStream.of(v).boxed().collect(Collectors.toList()));
			IntegerSolution s = new IntegerSolution(l,2);
			for(int i=0;i<50;i++){
				pop.add(s.copy());
			}
			parameters.setInitialPopulation(pop);
			parameters.setInitialPopulationPercentage(100.0);
			
			// reroute at provider's edge
			sb = new StringBuffer();
			
			for (int i = 0; i < topology.getNetGraph().getNEdges(); i++) {
				System.out.println(""+i);
				NetEdge e = topology.getNetGraph().getEdge(i);
				// demands that need to be rerouted
				Demands[] d = el.getEdgeToEdgePartialDemand(demands[0], e, EdgesLoad.EdgeNodeIn.LEFT_RIGHT);
				topology.setEdgeStatus(e, Status.DOWN);
				JecoliOSPF ea = new JecoliOSPF(topology, d, null);
				ea.configureSRLMTNSGAII(parameters);
				ea.run();
				p = new NondominatedPopulation(ea.getSolutionSet());
				congestions[i] = p.getLowestValuedSolutions(0, 1).get(0).getFitnessValue(0);
				IntegerSolution sol=p.getLowestValuedSolutions(0, 1).get(0);
				
				
				int[] s0=sol.getVariables().stream().mapToInt(Integer::intValue).toArray();
				int[] s1=Arrays.copyOfRange(s0, 0,topology.getNumberEdges());
				int[] s2=Arrays.copyOfRange(s0, topology.getNumberEdges(),2*topology.getNumberEdges());
				
				NetworkTopology topo=topology.copy();
				SRPathTranslator translator= new SRPathTranslator(topology,w);
				MatDijkstra spGraph=topo.getShortestPathGraph();
				spGraph.setSSP(true);
				//Translate s1 
				topo.applyWeights(s1);


			    
			    
			    SRConfiguration conf=new SRConfiguration();
				conf.addFailedLink(e);
				conf.setType(SRConfigurationType.LINK_FAILURE);
		
				SRConfiguration conf2=new SRConfiguration();
				conf2.addFailedLink(e);
				conf2.setType(SRConfigurationType.LINK_FAILURE);
		
			    
				//Translate s1 
				topo.applyWeights(s1);
			    Demands d1= d[1];
			    
			    
			    for(int source=0;source<topology.getDimension();source++)
			    	for(int dest=0;dest<topology.getDimension();dest++){
			    		if(d1.getDemands(source,dest)!=0){
			    			Vector<Integer> path=spGraph.getPath(source, dest);
			    			List<NetNode> nodePath=path.stream().map(x->topology.getNetGraph().getNodeByID(x)).collect(Collectors.toList());
			    			LabelPath lp=translator.translate(nodePath);
			    			conf.addLabelPath(lp);
			    			LabelPath lp2=translator.translate(nodePath,3);
			    			conf2.addLabelPath(lp2);
			    			
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
			    			LabelPath lp2=translator.translate(nodePath,3);
			    			conf2.addLabelPath(lp2);
			    		}
			    	}
			    
				//do something with the new configuration
			    
			    SRSimulator simulator = new SRSimulator(topology,w);
			    SRSimul sim =new SRSimul(topology);
			    sim.setConfigureSRPath(true);
			    sim.computeLoads(w, d[0]);
			    SRConfiguration initConf = sim.getSRconfiguration();
			    simulator.apply(initConf, d[0]);
			    simulator.apply(conf, d[1]);
			    simulator.apply(conf2, d[2]);
			    double va = simulator.getCongestionValue();
			    
			    
			    Properties stat=conf.statistics();
			    sb.append(i).append(SEPARATOR).append(congestions[i]).append(SEPARATOR)
			                                   .append(stat.getProperty("size")).append(SEPARATOR)
			                                   .append(stat.getProperty("mean")).append(SEPARATOR)
			                                   .append(stat.getProperty("min")).append(SEPARATOR)
			                                   .append(stat.getProperty("max")).append(SEPARATOR)
			                                   .append(stat.getProperty("std")).append(SEPARATOR)
			                                   .append(stat.getProperty("n3")).append(SEPARATOR)
			                                   .append(va).append(SEPARATOR)
			                                   .append("\n");
				topology.setEdgeStatus(e, Status.UP);
			}
			writeArray(congestions, "SR + ML + Seeding OPT Links congestions");
			try {
				FileWriter f = new FileWriter(this.getOutfile(), true);
				BufferedWriter bfw = new BufferedWriter(f);
				bfw.write(sb.toString());
				bfw.write("\n");
				bfw.flush();
				bfw.close();
				f.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// reroute at PLR
		    /*
			for (int i = 0; i < topology.getNetGraph().getNEdges(); i++) {

				NetEdge e = topology.getNetGraph().getEdge(i);
				// demands that need to be rerouted
				Demands[] d = el.getPointofRecoveryPartialDemand(demands[0], e, EdgesLoad.EdgeNodeIn.LEFT_RIGHT);
				topology.setEdgeStatus(e, Status.DOWN);
				JecoliOSPF ea = new JecoliOSPF(topology, d, null);
				ea.configureSRLMTNSGAII(parameters);
				ea.run();
				p = new NondominatedPopulation(ea.getSolutionSet());
				congestions[i] = p.getLowestValuedSolutions(0, 1).get(0).getFitnessValue(0);
				topology.setEdgeStatus(e, Status.UP);
			}
			writeArray(congestions, "SR + ML OPT Links congestions at PLR");
			*/
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	
	
	
	
	private void executeCSRMTLinkFailure() {
		
		
		
		Double[] congestions = new Double[topology.getNetGraph().getNEdges()];
		
		try {
			JecoliOSPF eaospf = new JecoliOSPF(topology, demands, null);
			params.setLoadBalancer(LoadBalancer.DEFT);
			params.setSecondObjective(AlgorithmSecondObjective.MLU);
			params.setNumberGenerations(1500);

			OSPFWeights w;
			Population p;
			if(this.weightsFile!=null){
				try{ 
					w= new OSPFWeights(this.weightsFile,topology.getDimension());
				}catch(Exception e){
					w = new OSPFWeights(this.weightsFile,topology);
				}
				
			}else{
				eaospf.configureSRNSGAII(params);
				eaospf.run();
				p = new NondominatedPopulation(eaospf.getSolutionSet());
				writeMOEA(p, runInfo("SR Optimization"));

				IntegerSolution s = p.getLowestValuedSolutions(0, 1).get(0);
				w = new OSPFWeights(topology.getDimension());
				w.setWeights(s.getVariablesArray(), topology);
			}
			// ***********************************
			// PARTIAL ML LOAD OVER SRLF SOLUTION
			// ************************************

			
			
			EdgesLoad el = new EdgesLoad();
			el.computeLoads(topology, w, LoadBalancer.DEFT, true);

			
			int nthreads = SystemConf.getPropertyInt("threads.number", 1);
			int n=topology.getNetGraph().getNEdges()/nthreads;
			int t = topology.getNetGraph().getNEdges()%nthreads;
			
			ExecutorService exec = Executors.newFixedThreadPool(nthreads);
			CountDownLatch latch = new CountDownLatch(nthreads);
			CMTComputeUnit[] runnables = new CMTComputeUnit[nthreads];
			
			int from=0;
			for(int i=0;i<nthreads;i++){	
				int to= from+n-1;
				if(t>0){
					to+=1;
					t--;
				}
				int[] a= new int[to-from+1];
				for(int j=0;j<to-from+1;j++)
					a[j]=from+j;
				runnables[i]=new CMTComputeUnit(topology.copy(),w.copy(),demands[0].copy(),a);
				from = to+1;
			}
			// parallel link failure computation
			for(CMTComputeUnit r : runnables) {
			    r.setLatch(latch);
			    exec.execute(r);
			}

			latch.await();
			System.out.println("Shuting down threads");
			exec.shutdown();

			System.out.println("Gathering results");
			for(int i=0;i<nthreads;i++){
				CMTComputeUnit unit =runnables[i];
				int[] ed=unit.getEdges();
				double[] cs=unit.getCongestions();
				for(int j=0;j<ed.length;j++){
					System.out.println(ed[j]+"="+cs[j]);
					congestions[ed[j]]=cs[j];
				}
			}
			
			
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			writeArray(congestions, "SR + ML + Contrained +Seeding OPT Links congestions");
	}

	
	
	private class CMTComputeUnit implements Runnable {

		private NetworkTopology topology;
		private OSPFWeights w;
		private Demands d; 
		private int[] edges;
		private double[] cgs;
		Params parameters;
		private CountDownLatch latch;
		
		
		public CMTComputeUnit(NetworkTopology topology, OSPFWeights weights, Demands demands, int[] edges){
			this.topology=topology;
			this.w=weights;
			this.d= demands;
			this.edges =edges;
			this.cgs = new double[edges.length];
			parameters =new Params();
			parameters.setArchiveSize(100);
			parameters.setPopulationSize(100);
			parameters.setLoadBalancer(LoadBalancer.DEFT);
			parameters.setSecondObjective(AlgorithmSecondObjective.MLU);
			parameters.setPreviousWeights(w);
			parameters.setNumberGenerations(150); ////////////
			
		}
		
		public void setLatch(CountDownLatch latch) {
		    this.latch = latch;
		  }
		
		
		
		@Override
		public void run() {
			System.out.println("Starting runner for edges"+Arrays.toString(edges));
			try {	
			EdgesLoad el = new EdgesLoad();
			el.computeLoads(topology, w, LoadBalancer.DEFT, true);
			
			
			for (int i = 0; i < edges.length; i++) {

				NetEdge e = topology.getNetGraph().getEdge(edges[i]);
				// demands that need to be rerouted
		
				Demands[] dd = el.getEdgeToEdgePartialDemand(this.d, e,EdgesLoad.EdgeNodeIn.LEFT_RIGHT);
	//			this.d.printDemands();
	//			dd[0].printDemands();
	//			dd[1].printDemands();
	//			dd[2].printDemands();
				topology.setEdgeStatus(e, Status.DOWN);
				System.out.println("Edge "+edges[i]+" is down.");
				JecoliOSPF ea = new JecoliOSPF(topology, dd, null);
				ea.configureConstrainedSRLMTNSGAII(parameters);
				ea.run();
				Population p = new NondominatedPopulation(ea.getSolutionSet());
				IntegerSolution sol=p.getLowestValuedSolutions(0, 1).get(0);
				cgs[i] = sol.getFitnessValue(0);
			
				topology.setEdgeStatus(e, Status.UP);
			}

			} catch (Exception e1) { e1.printStackTrace();}
			latch.countDown();

		}
		
		
		public int[] getEdges(){ return this.edges;}
		public double[] getCongestions(){ return this.cgs;}
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Weights are optimized considering the network on a normal state (1st
	 * objective) and in a failing state (2nd objective).
	 */
	private void executeSRLinkFailureMO() {

		try {

			Double[] congestions = new Double[topology.getNetGraph().getNEdges()];

			// **********************
			// Run SRLF optimization
			// ***********************

			JecoliOSPF eaospf = new JecoliOSPF(topology, demands, null);
			params.setLoadBalancer(LoadBalancer.DEFT);

			eaospf.configureSRLinkFailureNSGAII(params);
			eaospf.run();

			Population p = new NondominatedPopulation(eaospf.getSolutionSet());
			writeMOEA(p, runInfo("SRLF Optimization"));

			// select the solution with best MLU after failure
			// s = p.getLowestValuedSolutions(1, 1).get(0);
			IntegerSolution s = p.getLowestTradeOffSolutions(0.5).get(0);
			OSPFWeights w = new OSPFWeights(topology.getDimension());
			w.setWeights(s.getVariablesArray(), topology);
			System.out.println("=>" + s.getFitnessValue(0) + " " + s.getFitnessValue(1));

			// save congestion values for the best solution
			SRSimul simul = new SRSimul(topology);
			StringBuffer sb = new StringBuffer();
			sb.append("\n");
			for (int i = 0; i < topology.getNetGraph().getNEdges(); i++) {
				NetEdge e = topology.getNetGraph().getEdge(i);
				topology.setEdgeStatus(e, Graph.Status.DOWN);
				simul.evalPValues(null, w.asIntArray(), demands[0]);
				NetworkLoads load = simul.getLoads();
				double fit = load.getCongestion();
				double m = load.getMLU();
				sb.append(i).append(SEPARATOR).append(fit).append(SEPARATOR).append(m).append(SEPARATOR).append("\n");
				topology.setEdgeStatus(e, Graph.Status.UP);
			}
			try {
				FileWriter f = new FileWriter(this.getOutfile(), true);
				BufferedWriter bfw = new BufferedWriter(f);
				bfw.write(sb.toString());
				bfw.write("\n");
				bfw.flush();
				bfw.close();
				f.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// ********************************************
			// Run P value Optimization for SRLF Solutions
			// ********************************************

			params.setNumberGenerations(500);
			for (int i = 0; i < topology.getNetGraph().getNEdges(); i++) {
				topology.setEdgeStatus(topology.getNetGraph().getEdge(i), Status.DOWN);
				JecoliPValueInteger jc = new JecoliPValueInteger(topology, demands[0], w);
				jc.configureNSGAIIAlgorithm(params);
				jc.run();
				p = new NondominatedPopulation(jc.getSolutionSet());
				// writeMOEA(p, "link failure " + i);
				congestions[i] = p.getLowestValuedSolutions(0, 1).get(0).getFitnessValue(0);
				topology.setEdgeStatus(topology.getNetGraph().getEdge(i), Status.UP);
			}

			writeArray(congestions, "SRLF + P OPT Links congestions");

		} catch (InvalidConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (Exception e) {

		}
	}

	/**
	 * Segment Routing (SALP-SR) Link Failure Optimization with p-values
	 * optimization
	 */

	private void executeSRLinkFailure() {

		try {

			Double[] congestions = new Double[topology.getNetGraph().getNEdges()];
			Double[] congestions2 = new Double[topology.getNetGraph().getNEdges()];

			/******************************
			 * Simple SR optimization
			 ******************************/

			JecoliOSPF eaospf = new JecoliOSPF(topology, demands, null);
			params.setLoadBalancer(LoadBalancer.DEFT);
			params.setSecondObjective(AlgorithmSecondObjective.MLU);

			OSPFWeights w;
			if(this.weightsFile!=null){
				try{ 
					w= new OSPFWeights(this.weightsFile,topology.getDimension());
				}catch(Exception e){
					w = new OSPFWeights(this.weightsFile,topology);
				}
			}else{
				eaospf.configureSRNSGAII(params);
				eaospf.run();
				Population p = new NondominatedPopulation(eaospf.getSolutionSet());
				writeMOEA(p, runInfo("SR Optimization"));

				IntegerSolution s = p.getLowestValuedSolutions(0, 1).get(0);
				w = new OSPFWeights(topology.getDimension());
				w.setWeights(s.getVariablesArray(), topology);
				System.out.println("Best congestion Solution " + s.getFitnessValue(0));
			}
			// save congestion values for the best SR solution
			SRSimul simul = new SRSimul(topology);
			simul.setConfigureSRPath(true);
			simul.computeLoads(w, demands[0]);
			simul.getLoads().printLoads();
			SRConfiguration SRConf = simul.getSRconfiguration();
			StringBuffer sb = new StringBuffer();
			sb.append("\n");
			
			for (int i = 0; i < topology.getNetGraph().getNEdges(); i++) {
				
				NetEdge e = topology.getNetGraph().getEdge(i);
				topology.setEdgeStatus(e, Graph.Status.DOWN);
				
				simul.evalPValues(null, w.asIntArray(), demands[0]);
				NetworkLoads load = simul.getLoads();
				double fit = load.getCongestion();
				
				double m = load.getMLU();
				sb.append(i).append(SEPARATOR).append(fit).append(SEPARATOR).append(m).append(SEPARATOR).append("\n");
				
				// weights have already been applied
				// reroute NSP traffic on failing link from link src to link dst
				// by SP
				double[][] tloads = simul.totalLoads(demands[0], w, SRConf, false);
				NetworkLoads l = new NetworkLoads(tloads, topology);
				congestions[i] = simul.congestionMeasure(l, demands[0]);

				// reroute NSP traffic on failing link from edge src to edge dst
				// by SP
				tloads = simul.totalLoads(demands[0], w, SRConf, true);
				l = new NetworkLoads(tloads, topology);
				System.out.println("");
				//l.printLoads();
				congestions2[i] = simul.congestionMeasure(l, demands[0]);

				topology.setEdgeStatus(e, Graph.Status.UP);
			}
			FileWriter f;
			try {
				f = new FileWriter(this.getOutfile(), true);
				BufferedWriter W = new BufferedWriter(f);
				W.write("SR with recomputed paths");
				W.write("\n");
				W.write(sb.toString());
				W.write("\n");
				W.flush();
				W.close();
				f.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			this.writeArray(congestions, "SR without recomputed paths (link reroute)");
			this.writeArray(congestions2, "SR without recomputed paths (edge-to-edge reroute)");

			// ********************************************
			// Run P value Optimization for SR Solutions
			// ********************************************

			/*
			 * // params.setNumberGenerations(500); for (int i = 0; i <
			 * topology.getNetGraph().getNEdges(); i++) {
			 * topology.setEdgeStatus(topology.getNetGraph().getEdge(i),
			 * Status.DOWN); JecoliPValueInteger jc = new
			 * JecoliPValueInteger(topology, demands[0], w);
			 * jc.configureNSGAIIAlgorithm(params); jc.run(); p = new
			 * NondominatedPopulation(jc.getSolutionSet()); congestions[i] =
			 * p.getLowestValuedSolutions(0, 1).get(0).getFitnessValue(0); //
			 * writeMOEA(p, "SR P-value optimization for link failure " + // i);
			 * topology.setEdgeStatus(topology.getNetGraph().getEdge(i),
			 * Status.UP); }
			 * 
			 */
			// writeArray(congestions, "SR + P Links congestions");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	
	
	/**
	 * PEFT/DEFT Hybrid SDN/IP Optimization with distinct k SDN nodes
	 * 
	 * @throws Exception
	 */
	private void executeHybridSDNOptimization() throws Exception {

		params.setLoadBalancer(LoadBalancer.DEFT);
		params.setSecondObjective(AlgorithmSecondObjective.MLU);
		for (int i = 0; i < NODESP.length; i++) {
			// number of SDN enable nodes
			int k = (int) (NODESP[i] * topology.getDimension());
			params.setNumberSDNNodes(k);
			JecoliOSPF eaospf = new JecoliOSPF(topology, demands, null);
			eaospf.configureHybridNSGAII(params);
			eaospf.run();

			Population population = new NondominatedPopulation(eaospf.getSolutionSet());
			String s = "K=" + k;
			IntegerSolution sol=population.getLowestValuedSolutions(0,1).get(0);
			OSPFWeights w = new OSPFWeights(topology.getDimension());
			w.setWeights(sol.getVariablesArray(), topology);
			

			writeMOEA(population, runInfo(s));
		}

	}

	private NetworkTopology getTopology() {
		NetworkTopology topology = null;
		try {
			topology = new NetworkTopology(this.getNodesFile(), this.getEdgesFile());
		} catch (Exception e) {
			System.out.println("Error creating topology :" + e.getMessage());
			// e.printStackTrace();
			System.exit(0);
		}
		return topology;
	}

	private Demands getDemands(int dimension) {
		Demands dem = null;
		if (getFirstDemandFile().length() > 0)
			try {
				dem = new Demands(dimension, this.getFirstDemandFile());
			} catch (Exception e) {
				e.printStackTrace();
			}
		return dem;
	}

	private Demands getSecondDemands(int dimension) {
		Demands dem = null;
		if (getSecondDemandFile().length() > 0)
			try {
				dem = new Demands(dimension, this.getSecondDemandFile());
			} catch (Exception e) {
				e.printStackTrace();
			}
		return dem;
	}

	private DelayRequests getDelays(int dimension) {
		DelayRequests dr = null;
		if (getDelayFile().length() > 0)
			try {
				dr = new DelayRequests(dimension, this.getDelayFile());
			} catch (Exception e) {
				e.printStackTrace();
			}
		return dr;
	}

	/**
	 * Parse command line arguments
	 * 
	 * @param args
	 */
	private void parseArgs(String[] args) {

		for (int i = 0; i < args.length; i++) {

			StringTokenizer st = new StringTokenizer(args[i], ":");
			String o = st.nextToken();
			if (o.trim().equals("m")) { // method
				String m = st.nextToken();
				if (m.equals("lf"))
					this.setMethod(Method.LINK_FAILURE);
				else if (m.equals("mt"))
					this.setMethod(Method.MULTI_TOPOLOGY);
				else if (m.equals("2d"))
					this.setMethod(Method.TWO_DEMANDS);
				else if (m.equals("dd"))
					this.setMethod(Method.DEMAND_DELAY);
				else if (m.equals("srlf"))
					this.setMethod(Method.SR_LINK_FAILURE);
				else if (m.equals("srlfmo"))
					this.setMethod(Method.SR_LINK_FAILURE_MO);
				else if (m.equals("srmt"))
					this.setMethod(Method.SRMTLF);
				else if (m.equals("srmtc"))
					this.setMethod(Method.SRMTLFC);
				else if (m.equals("hsdn"))
					this.setMethod(Method.HYBRID_SDN_IP);
				else {
					System.out.println("Invalid method: " + m);
					System.exit(0);
				}
			} else if (o.equals("o")) { // algorithm
				String m = st.nextToken();
				if (m.equals("so"))
					this.setAlgorithm(Algorithm.SOEA);
				else if (m.equals("spea2"))
					this.setAlgorithm(Algorithm.SPEA2);
				else if (m.equals("nsgaii"))
					this.setAlgorithm(Algorithm.NSGAII);
				else {
					System.out.println("Invalid algorithm: " + m);
					System.exit(0);
				}
			} else if (o.equals("n")) { // nodes
				this.setNodesFile(st.nextToken());
			} else if (o.equals("e")) { // edges
				this.setEdgesFile(st.nextToken());
			} else if (o.equals("d")) { // first demand
				this.setFirstDemandFile(st.nextToken());
			} else if (o.equals("d2")) { // second demand
				this.setSecondDemandFile(st.nextToken());
			} else if (o.equals("dr")) { // second demand
				this.setDelayFile(st.nextToken());
			}else if (o.equals("w")) { // second demand
				this.setWeightsFile(st.nextToken());
			} 
			else if (o.equals("alpha")) { // alpha
				try {
					this.setAlpha(Double.parseDouble(st.nextToken()));
				} catch (Exception e) {
					System.out.println("Bad alpha: " + args[i]);
					System.exit(0);
				}
				if (this.getAlpha() > 1.0 || this.getAlpha() < 0.0) {
					System.out.println("Alpha " + this.getAlpha() + " must be in [0,1]");
					System.exit(0);
				}
				params.setAlfa(getAlpha());

			} else if (o.equals("beta")) { // beta
				try {
					this.setBeta(Double.parseDouble(st.nextToken()));
				} catch (Exception e) {
					System.out.println("Bad beta: " + args[i]);
					System.exit(0);
				}
				if (this.getBeta() > 1.0 || this.getBeta() < 0.0) {
					System.out.println("Beta " + this.getBeta() + " must be in [0,1]");
					System.exit(0);
				}
				params.setBeta(getBeta());
			} else if (o.equals("g")) { // iterations
				try {
					this.setIterations(Integer.parseInt(st.nextToken()));
				} catch (Exception e) {
					System.out.println("Bad number of Generations: " + args[i]);
					System.exit(0);
				}
				if (this.getIterations() < 0) {
					System.out.println("Generations " + this.getIterations() + " is not a valid integer value");
					System.exit(0);
				}
				params.setNumberGenerations(this.getIterations());
			} else if (o.equals("out")) {
				this.setOutfile(st.nextToken());
			} else {
				System.out.println("Ilegal argument: " + args[i]);
				System.exit(0);
			}

		}

	}

	private void setWeightsFile(String nextToken) {
		this.weightsFile = nextToken;
	}

	private String getDelayFile() {
		return delayFile;
	}

	private void setDelayFile(String delayFile) {
		this.delayFile = delayFile;
	}

	private String getSecondDemandFile() {
		return secondDemandFile;
	}

	private void setSecondDemandFile(String secondDemandFile) {
		this.secondDemandFile = secondDemandFile;
	}

	private String getFirstDemandFile() {
		return firstDemandFile;
	}

	private void setFirstDemandFile(String firstDemandFile) {
		this.firstDemandFile = firstDemandFile;
	}

	private String getEdgesFile() {
		return edgesFile;
	}

	private void setEdgesFile(String edgesFile) {
		this.edgesFile = edgesFile;
	}

	private String getNodesFile() {
		return nodesFile;
	}

	private void setNodesFile(String nodesFile) {
		this.nodesFile = nodesFile;
	}

	public Method getMethod() {
		return method;
	}

	private void setMethod(Method method) {
		this.method = method;
	}

	private double getAlpha() {
		return alpha;
	}

	private void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	private double getBeta() {
		return beta;
	}

	private void setBeta(double beta) {
		this.beta = beta;
	}

	private int getIterations() {
		return iterations;
	}

	private void setIterations(int iterations) {
		this.iterations = iterations;
	}

	private int getNumberMT() {
		return numberMT;
	}

	private String methodToString(Method m) {
		switch (m) {
		case MULTI_TOPOLOGY:
			return "MULTI_TOPOLOGY";
		case LINK_FAILURE:
			return "LINK_FAILURE";
		case TWO_DEMANDS:
			return "TWO_DEMANDS";
		case DEMAND_DELAY:
			return "DEMAND_DELAY";
		case SR_LINK_FAILURE:
			return "SR_LINK_FAILURE";
		case HYBRID_SDN_IP:
			return "HYBRID_SDN_IP";
		default:
			return "ERROR";
		}
	}

	private String runInfo(String title) {
		StringBuffer sb = new StringBuffer();
		sb.append(title).append(SEPARATOR);
		String s_algorithm = "SOEA";
		if (this.algorithm == Algorithm.NSGAII)
			s_algorithm = "NSGAII";
		else if (this.algorithm == Algorithm.SPEA2)
			s_algorithm = "SPEA2";
		sb.append(methodToString(method)).append(SEPARATOR).append(s_algorithm).append(SEPARATOR);
		sb.append(this.nodesFile).append(SEPARATOR).append(this.edgesFile).append(SEPARATOR)
				.append(this.firstDemandFile).append(SEPARATOR);
		return sb.toString();

	}

	private void writeArray(Object[] objs, String text) {
		String file = this.getOutfile();
		// method nodes edges demand1 demand2 delay alfa beta nmt cong1 delayp1
		// cong2 delay2
		StringBuffer sb = new StringBuffer();
		sb.append(text).append("\n");
		int n = objs.length;
		for (int i = 0; i < n; i++) {
			sb.append(i).append(SEPARATOR).append(objs[i].toString()).append("\n");
		}

		FileWriter f;
		try {
			f = new FileWriter(file, true);
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

	private void writeMOEA(Population p, String text) {
		String file = this.getOutfile();
		// method nodes edges demand1 demand2 delay alfa beta nmt cong1 delayp1
		// cong2 delay2
		StringBuffer sb = new StringBuffer();
		sb.append(text).append("\n");
		int n = p.getNumberOfSolutions();
		int n_obj = p.getNumberOfObjectives();
		for (int i = 0; i < n; i++) {
			IntegerSolution s = p.getSolution(i);
			StringBuffer ss = new StringBuffer();
			for (int j = 0; j < n_obj; j++) {
				ss.append(s.getFitnessValue(j)).append(SEPARATOR);
			}
			List<Integer> v = s.getVariables();
			for (Integer a : v) {
				ss.append(a.intValue()).append(SEPARATOR);
			}
			sb.append(ss.toString()).append("\n");
		}

		FileWriter f;
		try {
			f = new FileWriter(file, true);
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

	private void write(double c1, double d1, double c2, double d2) {

		String file = this.getOutfile();
		String SEPARATOR = ";";
		// method nodes edges demand1 demand2 delay alfa beta nmt cong1 delayp1
		// cong2 delay2
		StringBuffer sb = new StringBuffer();
		sb.append(methodToString(method)).append(SEPARATOR);
		sb.append(this.getNodesFile()).append(SEPARATOR);
		sb.append(this.getEdgesFile()).append(SEPARATOR);
		sb.append(this.getFirstDemandFile()).append(SEPARATOR);
		sb.append(this.getSecondDemandFile()).append(SEPARATOR);
		sb.append(this.getDelayFile()).append(SEPARATOR);
		sb.append(this.getAlpha()).append(SEPARATOR);
		sb.append(this.getBeta()).append(SEPARATOR);
		sb.append(this.getNumberMT()).append(SEPARATOR);
		sb.append(d1).append(SEPARATOR);
		sb.append(c1).append(SEPARATOR);
		sb.append(d2).append(SEPARATOR);
		sb.append(c2).append(SEPARATOR);

		FileWriter f;
		try {
			f = new FileWriter(file, true);
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

	private String getOutfile() {
		return outfile;
	}

	private void setOutfile(String outfile) {
		this.outfile = outfile;
	}

	private void setAlgorithm(Algorithm algorithm) {
		this.algorithm = algorithm;
	}

}
