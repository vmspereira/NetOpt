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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import jecoli.algorithm.components.configuration.InvalidConfigurationException;
import pt.uminho.algoritmi.netopt.SystemConf;
import pt.uminho.algoritmi.netopt.ospf.optimization.Params;
import pt.uminho.algoritmi.netopt.ospf.optimization.Params.AlgorithmSecondObjective;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.JecoliOSPF;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.JecoliPValueInteger;
import pt.uminho.algoritmi.netopt.ospf.simulation.Demands;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkLoads;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.NondominatedPopulation;
import pt.uminho.algoritmi.netopt.ospf.simulation.OSPFWeights;
import pt.uminho.algoritmi.netopt.ospf.simulation.Population;
import pt.uminho.algoritmi.netopt.ospf.simulation.Simul.LoadBalancer;
import pt.uminho.algoritmi.netopt.ospf.simulation.exception.DimensionErrorException;
import pt.uminho.algoritmi.netopt.ospf.simulation.simulators.PDEFTSimul;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.IntegerSolution;

public class MOSROptimizationTest2 {

	/*
	 * private static String nodesFile =
	 * "/Users/vmsap/Documents/OSPFfiles/50_4/isno_50_4.nodes"; private static
	 * String edgesFile =
	 * "/Users/vmsap/Documents/OSPFfiles/50_4/isno_50_4.edges"; private static
	 * String demandsFile =
	 * "/Users/vmsap/Documents/OSPFfiles/50_4/isno_50_4-D0.3.dem"; private
	 * static String weightsFile =
	 * "/Users/vmsap/Documents/OSPFfiles/50_4/50_4.listw";
	 */

	private NetworkTopology topology;
	private double factor;
	private String id;
	private final int WEIGHTS_ITERATIONS = 2000;
	private final int P_ITERATIONS = 300;
	private final double AMP = 0.08;
	private static final Logger LOGGER = Logger.getLogger( MOSROptimizationTest2.class.getName() );

	public static void main(String[] args) {

		System.out.println(Arrays.toString(args));

		if (args.length != 3)
			System.exit(1);

		String nodesFile = args[0];
		String edgesFile = args[1];
		String sfactor = args[2];

		double factor = 0.30;
		try {
			factor = Double.valueOf(sfactor);
		} catch (Exception e) {
		}

		try {
			NetworkTopology topology = new NetworkTopology(nodesFile, edgesFile);
			MOSROptimizationTest2 t = new MOSROptimizationTest2(topology, factor, nodesFile);
			t.run();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public MOSROptimizationTest2(NetworkTopology topology, double factor, String id) {
		this.topology = topology;
		this.factor = factor;
		this.id = id;
	}

	
	public void run() throws InvalidConfigurationException, Exception {
		// Generate 100 TM
		
		LOGGER.info("Creating random matrices");
		
		Demands dmax = new Demands(topology.getDimension());
		dmax.setRandomDemands(factor + Math.random() * AMP, topology);
		Demands dmin = new Demands(topology.getDimension());
		dmin.setRandomDemands(factor + Math.random() * AMP, topology);

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


		for (int k = 2; k < 100; k++) {
			Demands d = new Demands(topology.getDimension());
			d.setRandomDemands(factor + Math.random() * AMP, topology);
			for (int i = 0; i < topology.getDimension(); i++)
				for (int j = 0; j < topology.getDimension(); j++) {
					if (dmax.getDemands(i, j) < d.getDemands(i, j)) {
						dmax.setDemands(i, j, d.getDemands(i, j));
					} else if (dmin.getDemands(i, j) > d.getDemands(i, j)) {
						dmin.setDemands(i, j, d.getDemands(i, j));
					}
				}
			allDemands.add(d);
		}

	
		Demands[] dparam = new Demands[2];
		// the TMs used for optimization
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

	    
		Params params = new Params();
		params.setLoadBalancer(LoadBalancer.DEFT);
		params.setSecondObjective(AlgorithmSecondObjective.DEMANDS);
		params.setArchiveSize(100);
		params.setPopulationSize(100);
		params.setNumberGenerations(WEIGHTS_ITERATIONS);

		/////////////////////////
		// kmeans TM

		// Teste3 t = new Teste3();
		LOGGER.info("Identifying centroids");
		Centroid[] centroids;
		centroids = kmeans(allDemands, d1, d2);
		d1 = centroids[0].computeCentroid();
		d2 = centroids[1].computeCentroid();

		double alpha = (double) (centroids[0].count()) / 100;

		dparam[0] = d1;
		dparam[1] = d2;

		JecoliOSPF ea = new JecoliOSPF(topology, dparam, null);
		ea.configureNSGAII(params);
		ea.run();
		Population p = new NondominatedPopulation(ea.getSolutionSet());
		IntegerSolution s3 = p.getLowestTradeOffSolutions(alpha).get(0);
		OSPFWeights w = new OSPFWeights(topology.getDimension());
		w.setWeights(s3.getVariablesArray(), topology);
		LOGGER.info( "Initial congestion = "+s3.getFitnessValue(0));
		
		
		int nthreads = SystemConf.getPropertyInt("threads.number", 1);
		CMTComputeUnit[] runnables = new CMTComputeUnit[nthreads];
		int n=100/nthreads;
		int t = 100%nthreads;
		
		ExecutorService exec = Executors.newFixedThreadPool(nthreads);
		CountDownLatch latch = new CountDownLatch(nthreads);
		
		double[][] ckm= new double[100][4]; 
		
		int from=0;
		for(int i=0;i<nthreads;i++){	
			int to= from+n-1;
			if(t>0){
				to+=1;
				t--;
			}
			Demands[] a= new Demands[to-from+1];
			for(int j=0;j<to-from+1;j++)
				a[j]=allDemands.get(from+j);
			runnables[i]=new CMTComputeUnit(topology.copy(),w.copy(),a);
			from = to+1;
		}
		
		LOGGER.info("Running node-p optimization with "+nthreads+" threads");
		
		// parallel node-p values computation
		for(CMTComputeUnit r : runnables) {
		    r.setLatch(latch);
		    exec.execute(r);
		}

		latch.await();
		
		LOGGER.info("Shuting down threads");
		exec.shutdown();
		int count =0;
		LOGGER.info("Gathering results");
		for(int i=0;i<nthreads;i++){
			CMTComputeUnit unit =runnables[i];
			double[][] cs=unit.getCongestions();
			for(int j=0;j<cs.length;j++){
				ckm[count][0]=cs[j][0];
				ckm[count][1]=cs[j][1];
				ckm[count][2]=cs[j][2];
				ckm[count][3]=cs[j][3];
				count++;
			}
		}
		
	
		
		////////////////////////// One Demand average

		d1 = new Demands(topology.getDimension());
		for (int i = 0; i < 100; i++)
			d1.add(allDemands.get(i));
		d1.divide(100);

		dparam[0] = d1;
		dparam[1] = null;
		
		ea = new JecoliOSPF(topology, dparam, null);
		params.setSecondObjective(AlgorithmSecondObjective.MLU);
		ea.configureNSGAII(params);
		ea.run();
		p = new NondominatedPopulation(ea.getSolutionSet());
		IntegerSolution s5 = p.getLowestValuedSolutions(0, 1).get(0);
		w = new OSPFWeights(topology.getDimension());
		w.setWeights(s5.getVariablesArray(), topology);

		
		double[][] cav= new double[100][4]; 
		
		
		runnables = new CMTComputeUnit[nthreads];
		t = 100%nthreads;
		
		exec = Executors.newFixedThreadPool(nthreads);
		latch = new CountDownLatch(nthreads);
		
		
		
		from=0;
		for(int i=0;i<nthreads;i++){	
			int to= from+n-1;
			if(t>0){
				to+=1;
				t--;
			}
			Demands[] a= new Demands[to-from+1];
			for(int j=0;j<to-from+1;j++)
				a[j]=allDemands.get(from+j);
			runnables[i]=new CMTComputeUnit(topology.copy(),w.copy(),a);
			from = to+1;
		}
		
		
		for(CMTComputeUnit r : runnables) {
		    r.setLatch(latch);
		    exec.execute(r);
		}

		latch.await();
		
		System.out.println("Shuting down threads");
		exec.shutdown();
		
		count =0;
		System.out.println("Gathering results");
		for(int i=0;i<nthreads;i++){
			CMTComputeUnit unit =runnables[i];
			double[][] cs=unit.getCongestions();
			for(int j=0;j<cs.length;j++){
				cav[count][0]=cs[j][0];
				cav[count][1]=cs[j][1];
				cav[count][2]=cs[j][2];
				cav[count][3]=cs[j][3];
				count++;
			}
		}

		
		
		
		// print results

		String filename = "out" + System.currentTimeMillis() + ".csv";
		StringBuffer sb = new StringBuffer();
		sb.append(id).append(";\n");
		n = 100;
		for (int i = 0; i < n; i++) {
			sb.append(ckm[i][0]).append(";")
			.append(ckm[i][0]).append(";")
			.append(ckm[i][1]).append(";")
			.append(ckm[i][2]).append(";")
			.append(ckm[i][3]).append(";")
			.append(cav[i][0]).append(";")
			.append(cav[i][1]).append(";")
			.append(cav[i][2]).append(";")
			.append(cav[i][3]).append(";");
			sb.append(";\n");

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
			d.divide(demands.size());
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
		Demands dini1 = allDemands.get(pos1);
		Demands dini2 = allDemands.get(pos2);
		return kmeans(allDemands, dini1, dini2);
	}

	public Centroid[] kmeans(ArrayList<Demands> allDemands, Demands dini1, Demands dini2)
			throws DimensionErrorException {

		Demands[] centroids = new Demands[2];
		centroids[0] = dini1;
		centroids[1] = dini2;
		Demands d0;
		Centroid[] a = new Centroid[2];
		do {
			d0 = centroids[0];
			a = kmeansIt(centroids, allDemands);
			centroids[0] = a[0].computeCentroid();
			centroids[1] = a[1].computeCentroid();
		} while (d0.equals(centroids[0]) || d0.equals(centroids[1]));
		return a;
	}

	private class CMTComputeUnit implements Runnable {

		private NetworkTopology topology;
		private OSPFWeights w;
		private Demands[] demands;
		private double[][] cgs;
		Params parameters;
		private CountDownLatch latch;

		public CMTComputeUnit(NetworkTopology topology, OSPFWeights weights, Demands[] demands) {
			this.topology = topology;
			this.w = weights;
			this.demands = demands;
			this.cgs = new double[demands.length][4];
			parameters = new Params();
			parameters.setArchiveSize(100);
			parameters.setPopulationSize(100);
			parameters.setLoadBalancer(LoadBalancer.DEFT);
			parameters.setSecondObjective(AlgorithmSecondObjective.MLU);
			parameters.setPreviousWeights(w);
			parameters.setNumberGenerations(P_ITERATIONS); ////////////

		}

		public void setLatch(CountDownLatch latch) {
			this.latch = latch;
		}

		@Override
		public void run() {
			try {
				for (int i = 0; i < demands.length; i++) {
					PDEFTSimul simul = new PDEFTSimul(topology, true);
					simul.computeLoads(w, demands[i]);
					NetworkLoads loads = simul.getLoads();
					cgs[i][0] = loads.getCongestion();
					cgs[i][1] = loads.getMLU();

					JecoliPValueInteger j = new JecoliPValueInteger(topology, demands[i], w);
					IntegerSolution sol;
					j.setMinMaxPvalues(-100, 100, 10);
					j.configureNSGAIIAlgorithm(parameters);
					j.run();
					sol = j.getPolulation().getLowestValuedSolutions(0, 1).get(0);
					cgs[i][2] = sol.getFitnessValue(0);
					cgs[i][3] = sol.getFitnessValue(1);
				}

			} catch (Exception e1) {
				e1.printStackTrace();
			}
			latch.countDown();

		}

		public Demands[] getDemands() {
			return this.demands;
		}

		public double[][] getCongestions() {
			return this.cgs;
		}

	}

}
