package pt.uminho.algoritmi.netopt.tests;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;

import jecoli.algorithm.components.configuration.InvalidConfigurationException;
import pt.uminho.algoritmi.netopt.ospf.simulation.Demands;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkLoads;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.OSPFWeights;
import pt.uminho.algoritmi.netopt.ospf.simulation.Simul;

public class EvalLoads {
	
	public static void main(String[] args) throws InvalidConfigurationException, Exception {

		System.out.println(Arrays.toString(args));
		
		if(args.length!=4)
			System.exit(1);


		
		String nodesFile = args[0];
		String edgesFile = args[1];
		String demandsFile = args[2];
		String loadFile = args[3];
		
		NetworkTopology topology = new NetworkTopology(nodesFile, edgesFile);
		Demands demands = new Demands(topology.getDimension(), demandsFile);
	
		double[][] loads = new double[topology.getDimension()][topology.getDimension()];
		try (Stream<String> lineStream = Files.lines(Paths.get(loadFile))) { // autoclose
			// stream
				Iterator<String> lines = lineStream.iterator();
				int i =0;
				while (lines.hasNext()) {
					String line = lines.next();
					if (line.isEmpty())
						break; // stop at empty line

					String[] data = line.split(" ");
					for(int j=0;j<topology.getDimension();j++){
						loads[i][j]= Double.parseDouble(data[j]);
					}
					i++;
				}
				
				NetworkLoads l = new NetworkLoads(loads,topology);
				Simul s = new Simul(topology);
				double congestion =s.congestionMeasure(l, demands);
				double mlu = l.getMLU();
				System.out.println("congestion "+congestion+"\nMLU "+mlu);
		}
	
	
	}

	

}
