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
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import jecoli.algorithm.components.configuration.InvalidConfigurationException;
import pt.uminho.algoritmi.netopt.ospf.graph.Graph.Status;
import pt.uminho.algoritmi.netopt.ospf.graph.MatDijkstra;
import pt.uminho.algoritmi.netopt.ospf.optimization.Params;
import pt.uminho.algoritmi.netopt.ospf.optimization.Params.AlgorithmSecondObjective;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.JecoliOSPF;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.JecoliPValueInteger;
import pt.uminho.algoritmi.netopt.ospf.simulation.Demands;
import pt.uminho.algoritmi.netopt.ospf.simulation.EdgesLoad;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkLoads;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.NondominatedPopulation;
import pt.uminho.algoritmi.netopt.ospf.simulation.OSPFWeights;
import pt.uminho.algoritmi.netopt.ospf.simulation.Population;
import pt.uminho.algoritmi.netopt.ospf.simulation.Simul.LoadBalancer;
import pt.uminho.algoritmi.netopt.ospf.simulation.exception.DimensionErrorException;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetEdge;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetNode;
import pt.uminho.algoritmi.netopt.ospf.simulation.simulators.PDEFTSimul;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.IntegerSolution;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.LabelPath;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.SRConfiguration;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.SRPathTranslator;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.SRConfiguration.SRConfigurationType;

public class MOSROptimizationTest {


	public static void main(String[] args) throws InvalidConfigurationException, Exception {

		System.out.println(Arrays.toString(args));

		if (args.length != 3)
			System.exit(1);

		String nodesFile = args[0];
		String edgesFile = args[1];
		String sfactor = args[2];

		double factor = 0.35;
		try {
			factor = Double.valueOf(sfactor);
		} catch (Exception e) {
		}

		NetworkTopology topology = new NetworkTopology(nodesFile, edgesFile);
		// Generate 100 TM
		Demands dmax = new Demands(topology.getDimension());
		dmax.setRandomDemands(factor, topology);
		Demands dmin = new Demands(topology.getDimension());
		dmin.setRandomDemands(factor, topology);

		ArrayList<Demands> allDemands = new ArrayList<Demands>();
		allDemands.add(dmax.copy());
		allDemands.add(dmin.copy());

		for (int i = 0; i < topology.getDimension(); i++)
			for (int j = 0; j < topology.getDimension(); j++) {
				if (dmax.getDemands(i, j) < dmin.getDemands(i, j)) {
					double min = dmax.getDemands(i, j);
					dmax.setDemands(i, j, dmin.getDemands(i, j));
					dmin.setDemands(i, j, min);
				}
			}

		// dmax.printDemands();
		// dmin.printDemands();

		for (int k = 2; k < 100; k++) {
			Demands d = new Demands(topology.getDimension());
			d.setRandomDemands(factor, topology);
			for (int i = 0; i < topology.getDimension(); i++)
				for (int j = 0; j < topology.getDimension(); j++) {
					if (dmax.getDemands(i, j) < d.getDemands(i, j)) {
						dmax.setDemands(i, j, d.getDemands(i, j));
					} else if (dmin.getDemands(i, j) > d.getDemands(i, j)) {
						dmin.setDemands(i, j, d.getDemands(i, j));
					}
				}
			allDemands.add(d);
			System.out.print(" " + k);
		}

		// dmax.printDemands();
		// dmin.printDemands();

		Demands[] dparam = new Demands[2];
		Demands d1 = new Demands(topology.getDimension());
		Demands d2 = new Demands(topology.getDimension());

		/////////////////////////////////////////////////
		// Max Min TM

		for (int i = 0; i < topology.getDimension(); i++)
			for (int j = 0; j < topology.getDimension(); j++) {
				double r = Math.random();
				if (r > 0.5) {
					d1.setDemands(i, j, dmax.getDemands(i, j));
					d2.setDemands(i, j, dmin.getDemands(i, j));
				} else {
					d2.setDemands(i, j, dmax.getDemands(i, j));
					d1.setDemands(i, j, dmin.getDemands(i, j));
				}
			}

		dparam[0] = d1;
		dparam[1] = d2;

		Params parameters = new Params();
		parameters.setLoadBalancer(LoadBalancer.DEFT);
		parameters.setSecondObjective(AlgorithmSecondObjective.DEMANDS);
		parameters.setArchiveSize(100);
		parameters.setPopulationSize(100);
		parameters.setNumberGenerations(1500);

		JecoliOSPF ea = new JecoliOSPF(topology, dparam, null);
		ea.configureNSGAII(parameters);
		ea.run();
		Population p = new NondominatedPopulation(ea.getSolutionSet());
		IntegerSolution s1 = p.getLowestTradeOffSolutions(0.5).get(0);
		OSPFWeights w = new OSPFWeights(topology.getDimension());
		w.setWeights(s1.getVariablesArray(), topology);

		double[] congestions1 = new double[100];
		double[] congestions2 = new double[100];
		Params params = new Params();
		params.setLoadBalancer(LoadBalancer.DEFT);
		params.setSecondObjective(AlgorithmSecondObjective.MLU);
		params.setArchiveSize(100);
		params.setPopulationSize(100);
		params.setNumberGenerations(150);

		for (int k = 0; k < 100; k++) {
			System.out.print("=>" + k);
			PDEFTSimul simul = new PDEFTSimul(topology, true);
			simul.computeLoads(w, allDemands.get(k));
			NetworkLoads loads = simul.getLoads();
			congestions1[k] = loads.getCongestion();

			JecoliPValueInteger j = new JecoliPValueInteger(topology, allDemands.get(k), w);
			j.configureNSGAIIAlgorithm(params);
			j.run();

			IntegerSolution sol = j.getPolulation().getLowestValuedSolutions(0, 1).get(0);
			congestions2[k] = sol.getFitnessValue(0);
		}

		////////////////////////////
		// Random TM
		int pos1 = 0;
		int pos2 = 0;
		while (pos1 == pos2) {
			pos1 = (int) (Math.random() * (allDemands.size() - 1));
			pos2 = (int) (Math.random() * (allDemands.size() - 1));
		}
		d1 = allDemands.get(pos1);
		d2 = allDemands.get(pos2);

		ea = new JecoliOSPF(topology, dparam, null);
		ea.configureNSGAII(parameters);
		ea.run();
		p = new NondominatedPopulation(ea.getSolutionSet());
		IntegerSolution s2 = p.getLowestTradeOffSolutions(0.5).get(0);
		w = new OSPFWeights(topology.getDimension());
		w.setWeights(s2.getVariablesArray(), topology);

		double[] congestions3 = new double[100];
		double[] congestions4 = new double[100];

		for (int k = 0; k < 100; k++) {
			System.out.print("=>" + k);
			PDEFTSimul simul = new PDEFTSimul(topology, true);
			simul.computeLoads(w, allDemands.get(k));
			NetworkLoads loads = simul.getLoads();
			congestions3[k] = loads.getCongestion();

			JecoliPValueInteger j = new JecoliPValueInteger(topology, allDemands.get(k), w);
			j.configureNSGAIIAlgorithm(params);
			j.run();

			IntegerSolution sol = j.getPolulation().getLowestValuedSolutions(0, 1).get(0);
			congestions4[k] = sol.getFitnessValue(0);
		}

		/////////////////////////
		// kmeans TM

		MOSROptimizationTest t = new MOSROptimizationTest();
		Centroid[] centroids = t.kmeans(allDemands);
		d1 = centroids[0].computeCentroid();
		d2 = centroids[1].computeCentroid();

		double alpha = (double) (centroids[0].count()) / (double) (centroids[0].count() + centroids[0].count());

		dparam[0] = d1;
		dparam[1] = d2;

		ea = new JecoliOSPF(topology, dparam, null);
		ea.configureNSGAII(parameters);
		ea.run();
		p = new NondominatedPopulation(ea.getSolutionSet());
		IntegerSolution s3 = p.getLowestTradeOffSolutions(alpha).get(0);
		w = new OSPFWeights(topology.getDimension());
		w.setWeights(s3.getVariablesArray(), topology);

		double[] congestions5 = new double[100];
		double[] congestions6 = new double[100];

		for (int k = 0; k < 100; k++) {
			System.out.print("=>" + k);
			PDEFTSimul simul = new PDEFTSimul(topology, true);
			simul.computeLoads(w, allDemands.get(k));
			NetworkLoads loads = simul.getLoads();
			congestions5[k] = loads.getCongestion();

			JecoliPValueInteger j = new JecoliPValueInteger(topology, allDemands.get(k), w);
			j.configureNSGAIIAlgorithm(params);
			j.run();

			IntegerSolution sol = j.getPolulation().getLowestValuedSolutions(0, 1).get(0);
			congestions6[k] = sol.getFitnessValue(0);
		}

		////////////////////////// One Demand random

		pos1 = (int) (Math.random() * (allDemands.size() - 1));
		d1 = allDemands.get(pos1);
		dparam[0] = d1;
		dparam[1] = null;
		parameters.setSecondObjective(AlgorithmSecondObjective.MLU);

		ea = new JecoliOSPF(topology, dparam, null);
		ea.configureNSGAII(parameters);
		ea.run();
		p = new NondominatedPopulation(ea.getSolutionSet());
		IntegerSolution s4 = p.getLowestValuedSolutions(0, 1).get(0);
		w = new OSPFWeights(topology.getDimension());
		w.setWeights(s4.getVariablesArray(), topology);

		double[] congestions7 = new double[100];
		double[] congestions8 = new double[100];

		for (int k = 0; k < 100; k++) {
			System.out.print("=>" + k);
			PDEFTSimul simul = new PDEFTSimul(topology, true);
			simul.computeLoads(w, allDemands.get(k));
			NetworkLoads loads = simul.getLoads();
			congestions7[k] = loads.getCongestion();

			JecoliPValueInteger j = new JecoliPValueInteger(topology, allDemands.get(k), w);
			j.configureNSGAIIAlgorithm(params);
			j.run();

			IntegerSolution sol = j.getPolulation().getLowestValuedSolutions(0, 1).get(0);
			congestions8[k] = sol.getFitnessValue(0);
		}

		////////////////////////// One Demand average

		
		d1 = new Demands(topology.getDimension());
		for(int i=0;i<100;i++)
			d1.add(allDemands.get(i));
		d1.divide(100);
		
		dparam[0] = d1;
		dparam[1] = null;
		parameters.setSecondObjective(AlgorithmSecondObjective.MLU);

		ea = new JecoliOSPF(topology, dparam, null);
		ea.configureNSGAII(parameters);
		ea.run();
		p = new NondominatedPopulation(ea.getSolutionSet());
		IntegerSolution s5 = p.getLowestValuedSolutions(0, 1).get(0);
		w = new OSPFWeights(topology.getDimension());
		w.setWeights(s5.getVariablesArray(), topology);

		double[] congestions9 = new double[100];
		double[] congestions10 = new double[100];

		for (int k = 0; k < 100; k++) {
			System.out.print("=>" + k);
			PDEFTSimul simul = new PDEFTSimul(topology, true);
			simul.computeLoads(w, allDemands.get(k));
			NetworkLoads loads = simul.getLoads();
			congestions9[k] = loads.getCongestion();

			JecoliPValueInteger j = new JecoliPValueInteger(topology, allDemands.get(k), w);
			j.configureNSGAIIAlgorithm(params);
			j.run();

			IntegerSolution sol = j.getPolulation().getLowestValuedSolutions(0, 1).get(0);
			congestions10[k] = sol.getFitnessValue(0);
		}

		// print results

		String filename = "out" + System.currentTimeMillis() + ".csv";
		StringBuffer sb = new StringBuffer();
		int n = 100;
		sb.append(s1.getFitnessValue(0)).append(";").append(s1.getFitnessValue(1)).append(";;");
		sb.append(s2.getFitnessValue(0)).append(";").append(s2.getFitnessValue(1)).append(";;");
		sb.append(s3.getFitnessValue(0)).append(";").append(s3.getFitnessValue(1)).append(";;");
		sb.append(s4.getFitnessValue(0)).append(";").append(s4.getFitnessValue(1)).append(";;");
		sb.append(s5.getFitnessValue(0)).append(";").append(s5.getFitnessValue(1)).append(";;").append("\n");
		for (int i = 0; i < n; i++) {
			sb.append(i).append(";").append(congestions1[i]).append(";").append(congestions2[i]).append(";;");
			sb.append(congestions3[i]).append(";").append(congestions4[i]).append(";;");
			sb.append(congestions5[i]).append(";").append(congestions6[i]).append(";;");
			sb.append(congestions7[i]).append(";").append(congestions8[i]).append(";;");
			sb.append(congestions9[i]).append(";").append(congestions10[i]).append(";\n");
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

	
	
	
	
	
	
	
	
	private class ComputeUnit implements Runnable {

		private NetworkTopology topology;
		private OSPFWeights w;
		private double[] congestions1;
		private double[] congestions2;
		private Demands[] initial;
		private ArrayList<Demands> allDemands;
		private double alpha;
		private CountDownLatch latch;
		IntegerSolution s;
		
		
		
		public ComputeUnit(NetworkTopology topology, Demands[] initial , ArrayList<Demands> allDemands, double alpha){
			
			this.topology=topology;
			congestions1 = new double[100];
			congestions2 = new double[100];
			this.allDemands= allDemands;
			this.initial = initial;
			this.alpha =alpha;
		}
		
		public void setLatch(CountDownLatch latch) {
		    this.latch = latch;
		  }
		
		
		
		@Override
		public void run() {
			
			try{

			Params parameters = new Params();
			parameters.setLoadBalancer(LoadBalancer.DEFT);
			if(initial.length==2)
				parameters.setSecondObjective(AlgorithmSecondObjective.DEMANDS);
			else
				parameters.setSecondObjective(AlgorithmSecondObjective.MLU);
			parameters.setArchiveSize(100);
			parameters.setPopulationSize(100);
			parameters.setNumberGenerations(1500);

			
			JecoliOSPF ea = new JecoliOSPF(topology, initial, null);
			ea.configureNSGAII(parameters);
			ea.run();
			Population p = new NondominatedPopulation(ea.getSolutionSet());
			s = p.getLowestTradeOffSolutions(alpha).get(0);
		
			w = new OSPFWeights(topology.getDimension());
			w.setWeights(s.getVariablesArray(), topology);

			
			
			Params params = new Params();
			params.setLoadBalancer(LoadBalancer.DEFT);
			params.setSecondObjective(AlgorithmSecondObjective.MLU);
			params.setArchiveSize(100);
			params.setPopulationSize(100);
			params.setNumberGenerations(150);

			for (int k = 0; k < 100; k++) {
				PDEFTSimul simul = new PDEFTSimul(topology, true);
				simul.computeLoads(w, allDemands.get(k));
				NetworkLoads loads = simul.getLoads();
				congestions1[k] = loads.getCongestion();
				JecoliPValueInteger j = new JecoliPValueInteger(topology, allDemands.get(k), w);
				j.configureNSGAIIAlgorithm(params);
				j.run();
				IntegerSolution sol = j.getPolulation().getLowestValuedSolutions(0, 1).get(0);
				congestions2[k] = sol.getFitnessValue(0);
			}

			}catch(Exception e){}
			
			latch.countDown();

		}
		
		
		
		public double[] getCongestionBefore(){ return this.congestions1;}
		public double[] getCongestionAfter(){ return this.congestions2;}
		public IntegerSolution getSolution(){ return this.s;}
		
		
	}
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	private double distance(Demands d1, Demands d2) throws DimensionErrorException {
		if (d1.getDimension() != d2.getDimension())
			throw new DimensionErrorException();
		double sum = 0;
		for (int i = 0; i < d1.getDimension(); i++)
			for (int j = 0; j < d1.getDimension(); j++) {
				double d = d1.getDemands(i, j) - d2.getDemands(i, j);
				sum += d * d;
			}
		return Math.sqrt(sum / d1.getDimension());
	}

	private class Centroid {

		int dimension;
		Demands centroid;
		ArrayList<Demands> demands;

		public Centroid(int dimension) {
			this.dimension = dimension;
			demands = new ArrayList<Demands>();
		}

		public void add(Demands d) {
			demands.add(d);
		}

		public Demands computeCentroid() {
			Demands d = new Demands(dimension);
			for (int i = 0; i < demands.size(); i++)
				d.add(demands.get(i));
			d.divide(dimension);
			centroid = d;
			return centroid;
		}

		public int count() {
			return this.demands.size();
		}

	}

	public Centroid[] kmeansIt(Demands[] centroids, ArrayList<Demands> allDemands) throws DimensionErrorException {
		Centroid[] c = new Centroid[2];
		Centroid c0 = new Centroid(centroids[0].getDimension());
		Centroid c1 = new Centroid(centroids[0].getDimension());
		for (int i = 0; i < allDemands.size(); i++) {
			double d0 = distance(centroids[0], allDemands.get(i));
			double d1 = distance(centroids[1], allDemands.get(i));
			if (d0 < d1)
				c0.add(allDemands.get(i));
			else
				c1.add(allDemands.get(i));
		}
		c[0] = c0;
		c[1] = c1;
		return c;
	}

	public Centroid[] kmeans(ArrayList<Demands> allDemands) throws DimensionErrorException {

		int pos1 = 0;
		int pos2 = 0;
		while (pos1 == pos2) {
			pos1 = (int) (Math.random() * (allDemands.size() - 1));
			pos2 = (int) (Math.random() * (allDemands.size() - 1));
		}
		Demands[] centroids = new Demands[2];
		centroids[0] = allDemands.get(pos1);
		centroids[1] = allDemands.get(pos2);
		Demands d0;
		Demands d1;
		Centroid[] a = new Centroid[2];
		do {
			d0 = centroids[0];
			d1 = centroids[1];
			a = kmeansIt(centroids, allDemands);
			centroids[0] = a[0].computeCentroid();
			centroids[1] = a[1].computeCentroid();
		} while (d0.equals(centroids[0]) || d0.equals(centroids[1]));
		return a;
	}

}
