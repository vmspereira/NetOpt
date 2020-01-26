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
import java.util.Iterator;
import java.util.List;
import jecoli.algorithm.components.configuration.InvalidConfigurationException;
import pt.uminho.algoritmi.netopt.ospf.optimization.Params;
import pt.uminho.algoritmi.netopt.ospf.optimization.Params.AlgorithmSecondObjective;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.JecoliOSPF;
import pt.uminho.algoritmi.netopt.ospf.simulation.Demands;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.NondominatedPopulation;
import pt.uminho.algoritmi.netopt.ospf.simulation.OSPFWeights;
import pt.uminho.algoritmi.netopt.ospf.simulation.Population;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.IntegerSolution;

public class SROptimizationTest {


	public static void main(String[] args) throws InvalidConfigurationException, Exception {

		//System.out.println(Arrays.toString(args));

		//if (args.length != 3)
		//	System.exit(1);

		String nodesFile = args[0];
		String edgesFile = args[1];
		String demandsFile = args[2];
		
		NetworkTopology topology = new NetworkTopology(nodesFile, edgesFile);
		Demands demands = new Demands(topology.getDimension(), demandsFile);
		Population init = new Population(); 
		if(args[3]!= null){
			OSPFWeights w = new OSPFWeights(topology.getDimension());
			String weightsFile=args[3];
			try{
				w.readOSPFWeightsList(topology, weightsFile);
			}
			catch(Exception e){
				w.readOSPFWeights(topology.getDimension(), weightsFile);
			}
			init.add(w);
		}
		
		/*
		Demands[] d = new Demands[1];
		d[0]=demands;
		SRIntegerEvaluationMOLP fo = new SRIntegerEvaluationMOLP(topology, d);
		
		OSPFWeights w = new OSPFWeights(topology.getDimension());
		w.setInvCapWeights(1, 20, topology);
		
		IntegerSolution s = new IntegerSolution(w.asIntArray(),2);
		ISolution<ILinearRepresentation<Integer>>solution =SolutionParser.convert(s, 2);
		Double[] res= fo.evaluateMO(solution.getRepresentation());
		*/
		
		
		Demands[] d = new Demands[1];
		d[0]=demands;
		Params parameters = new Params();
		parameters.setArchiveSize(100);
		parameters.setPopulationSize(100);
		parameters.setNumberGenerations(1);
		parameters.setSecondObjective(AlgorithmSecondObjective.MLU);
		if(init.getNumberOfSolutions()>0){
			parameters.setInitialPopulation(init);
			parameters.setInitialPopulationPercentage(100);
		}
			

		JecoliOSPF ea = new JecoliOSPF(topology, d, null);
		ea.configureSRNSGAII(parameters);
		ea.run();
		Population p = new NondominatedPopulation(ea.getSolutionSet());
		
		save(p);
		
		parameters.setInitialPopulation(p);
		parameters.setInitialPopulationPercentage(100);
		parameters.setNumberGenerations(1);
		
		ea.configureHybridSRNSGAIILP(parameters);
		ea.run();
		p = new NondominatedPopulation(ea.getSolutionSet());
		
		//IntegerSolution s1 = p.getLowestFitnessSolutions(0).get(0);
		//OSPFWeights w = new OSPFWeights(topology.getDimension());
		//w.setWeights(s1.getVariablesArray(), topology);
		
		save(p);
	}
	
	
	public static void save(Population p){
		
		Iterator<IntegerSolution> l=p.iterator() ;
		StringBuffer bf = new StringBuffer();
		while(l.hasNext()){
			IntegerSolution s = l.next();
			bf.append(s.getFitnessValue(0)).append(";");
			bf.append(s.getFitnessValue(1)).append(";");
			List<Integer> v =s.getVariables();
			for(Integer i:v)
				bf.append(i).append(";");
			bf.append("\n");
		}
		FileWriter f;
		try {
			f = new FileWriter(""+System.currentTimeMillis()+".csv", true);
			BufferedWriter W = new BufferedWriter(f);
			W.write(bf.toString());
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
