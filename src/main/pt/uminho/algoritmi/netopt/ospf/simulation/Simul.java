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
package pt.uminho.algoritmi.netopt.ospf.simulation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.Vector;

import pt.uminho.algoritmi.netopt.SystemConf;
import pt.uminho.algoritmi.netopt.ospf.graph.Graph;
import pt.uminho.algoritmi.netopt.ospf.graph.MatDijkstra;
import pt.uminho.algoritmi.netopt.ospf.graph.SPFElement;
import pt.uminho.algoritmi.netopt.ospf.graph.WGraph;
import pt.uminho.algoritmi.netopt.ospf.simulation.exception.DimensionErrorException;
import pt.uminho.algoritmi.netopt.ospf.simulation.loadballancer.ECMPLoadBalancer;
import pt.uminho.algoritmi.netopt.ospf.simulation.loadballancer.GammaLoadBalancer;
import pt.uminho.algoritmi.netopt.ospf.simulation.loadballancer.ILoadBalancer;
import pt.uminho.algoritmi.netopt.ospf.simulation.loadballancer.SSPLoadBalancer;
import pt.uminho.algoritmi.netopt.ospf.simulation.simulators.ISimulator;


@SuppressWarnings("serial")
public class Simul implements Serializable, ISimulator {
	
	protected NetworkTopology topology;
	protected NetworkLoads loads;
	protected AverageEndtoEndDelays endToEndDelays;
	protected boolean fullDebug;
	protected LoadBalancer lbo;
	protected String lbs;
	protected double deftThreshold;
	protected boolean filterDeftThreshold = false;



	public static enum  LoadBalancer{
		
		
		ECMP("OSPF Equal Cost Multi Path","OSPF ECMP"),
		NOLB("OSPF Single Shortest Path","OSPF SSP"),
		DEFT("Distributed Exponentially-weighted Flow-spliTting","DEFT"),
		PEFT("Penalizing Exponential Flow-spliTting","PEFT");
		
		private final String name;
		private final String shortname;
		private final boolean confString;
		
		LoadBalancer(String name,String shortname){
			this.name=name;
			this.shortname=shortname;
			this.confString=false;
		}
		
		LoadBalancer(String name,String shortname,boolean c){
			this.name=name;
			this.shortname=shortname;
			this.confString=c;
		}
		
		public String toString(){
			return shortname;
		}
		
		public String getName(){
			return this.name;
		}
		
		public String getShortName(){
			return this.shortname;
		}
		
		public boolean useConfigurationString(){
			return this.confString;
		}
	}
	
	
	
	public Simul(NetworkTopology topology) {
		this.topology = topology;
		this.loads = null;
		this.endToEndDelays = null;
		this.fullDebug = false;
		this.lbo = LoadBalancer.ECMP;
		this.deftThreshold=SystemConf.getPropertyDouble("deft.threshold",0.01);
		this.filterDeftThreshold=SystemConf.getPropertyBoolean ("deft.applythreshold",false);
	}
	
	
	public void setLoadBalancer(LoadBalancer loadBalancer){
		this.lbo=loadBalancer;
	}

	public NetworkLoads getLoads() {
		return loads;
	}

	public void setLoads(NetworkLoads loads) {
		this.loads = loads;
	}

	public AverageEndtoEndDelays getAverageEndToEndDelays() {
		return endToEndDelays;
	}

	public void setAverageEndToEndDelays(AverageEndtoEndDelays endToEndDelays) {
		this.endToEndDelays = endToEndDelays;
	}

	/**
	 * Compute partial loads to a given destination; assumes shortest distances
	 * are calculated
	 * 
	 */

	public double[][] partialLoads(int dest, Demands demands) {
		
		ILoadBalancer lb;
		switch(lbo){
		case DEFT:
			GammaLoadBalancer gdeft = new GammaLoadBalancer(topology,dest,LoadBalancer.DEFT);
			gdeft.computeGamma();
			lb=gdeft;
			break;
		case PEFT:
			GammaLoadBalancer gpeft= new GammaLoadBalancer(topology,dest,LoadBalancer.PEFT);
			gpeft.computeGamma();
			lb=gpeft;
			break;
		case NOLB:
			lb= new SSPLoadBalancer(topology.getShortestPathGraph().getArcsShortestPath(dest),topology.getWeights());
			break;
		case ECMP:
		default:
			lb= new ECMPLoadBalancer(topology.getShortestPathGraph().getArcsShortestPath(dest));
			break;
		}
				
		// SP distances
		double[][] dists = topology.getShortestPathGraph().getShortestPathDistances();
		// Initialize loads to 0.0
		double[][] ploads = new double[topology.getDimension()][topology.getDimension()];
		for (int j = 0; j < topology.getDimension(); j++)
			for (int k = 0; k < topology.getDimension(); k++)
				ploads[j][k] = 0.0;

		//Suppose the network is connected
		Vector<Integer> nodes = topology.getShortestPathGraph().getNodesForDest(dest);
		
		while (nodes.size() > 0) {
			// choose node with maximum distance
			Integer dn1 = (Integer) nodes.get(0);
			double ddn = dists[dn1.intValue()][dest];
			int v = dn1.intValue();
			for (int i = 1; i < nodes.size(); i++) {
				Integer newdn = (Integer) nodes.get(i);
				double nddn = dists[newdn.intValue()][dest];
				if (nddn > ddn) {
					dn1 = (Integer) nodes.get(i);
					ddn = nddn;
					v = newdn.intValue();
				}
			}

			// for each arc leaving from this node
			for (int w = 0; w < topology.getDimension(); w++){
				if (topology.getNetGraph().existEdge(v,w) && topology.getNetGraph().getEdge(v,w).isUP()){
					double sum=0.0;
					for(int u=0;u<topology.getDimension(); u++)
						if(topology.getNetGraph().existEdge(u,v) && topology.getNetGraph().getEdge(u,v).isUP())
							sum+=ploads[u][v];
						
					ploads[v][w]=lb.getSplitRatio(v, dest, v,w)*(demands.getDemands(v, dest)+sum);
				}
			}
				
			nodes.remove(dn1);
		}
		return ploads;
	}
	
	
	
	
	
	
		
	
	
	
	/**
	 * compute loads in the graph using partialLoads method; sums all loads; to
	 * get results do getLoads
	 */
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

	
	
	// lists overloaded links and returns total overload
	public double listOverloadedLinks() {
		double r = 0.0;
		for (int i = 0; i < topology.getGraph().getDimension(); i++)
			for (int j = 0; j < topology.getGraph().getDimension(); j++)
				if (i != j) {
					if (loads.getLoads(i, j) > topology.getGraph().getCapacity(i, j)) {
						r += (loads.getLoads(i, j) - topology.getGraph().getCapacity(i, j));
					}
				}
		return r;
	}

	
	
	public double delayPenalties(DelayRequests delayReqs) {
		double res = 0.0;
		for (int i = 0; i < topology.getGraph().getDimension(); i++)
			for (int j = 0; j < topology.getGraph().getDimension(); j++) {
				if (delayReqs.getDelayReq(i, j) > 0)
					res += delayPenalty(i, j, delayReqs);
			}
		return (res / sumEndtoEndDelays());
	}

	
	
	
	public double delayPenalty(int s, int d, DelayRequests delayReqs) {
		int[][] sparse = topology.getGraph().getAllEdges();
		Vector<Vector<Integer>> paths;

		boolean[][] alts = topology.getShortestPathGraph().getSolAlternatives();

		if (alts[s][d]) // alternative routes
		{
			Graph opt = topology.getShortestPathGraph().getArcsShortestPath(s, d, sparse);

			// for each path get the sum of the delays
			paths = opt.getAllPaths(s, d);
		} else // one single route
		{

			paths = new Vector<Vector<Integer>>();
			Vector<Integer> nodes = topology.getShortestPathGraph().getPath(s, d);
			paths.add(nodes);
		}

		double[] ac_delays = new double[paths.size()];
		for (int k = 0; k < paths.size(); k++) {
			Vector<Integer> path = (Vector<Integer>) paths.get(k);
			ac_delays[k] = 0.0;
			for (int i = 0; i < path.size() - 1; i++) {
				int node = ((Integer) path.get(i)).intValue();
				int next = ((Integer) path.get(i + 1)).intValue();
				ac_delays[k] += topology.getNetGraph().getDelays()[node][next];
			}
		}
		double res = 0.0;
		for (int k = 0; k < ac_delays.length; k++)
			res += delayPen(ac_delays[k], delayReqs.getDelayReq(s, d));
		res /= paths.size(); // media das penalties ou penalty da media ??? para
								// ja a primeira !!

		return res;
	}

	/** congestion measure proposed by Fortz and Thorup */
	public double congestionMeasure(NetworkLoads loads, Demands demands) {
		double sum = 0.0;

		loads.computeU(topology.getGraph()); // compute congestion for each link

		for (int j = 0; j < topology.getGraph().getDimension(); j++)
			for (int k = 0; k < topology.getGraph().getDimension(); k++)
				if (topology.getGraph().getConnection(j, k).equals(Graph.Status.UP))
					sum += phi(loads.getU(j, k), loads.getLoads(j, k), topology.getGraph().getCapacity(j, k));
		return (sum / phiUncap(demands));
	}

	public double congestionMeasure(NetworkLoads loads, double[][] demands) {
		double sum = 0.0;
		loads.computeU(topology.getGraph()); // compute congestion for each link
		for (int j = 0; j < topology.getGraph().getDimension(); j++)
			for (int k = 0; k < topology.getGraph().getDimension(); k++)
				if (topology.getGraph().getConnection(j, k).equals(Graph.Status.UP))
					sum += phi(loads.getU(j, k), loads.getLoads(j, k), topology.getGraph().getCapacity(j, k));
		return (sum / phiUncap(demands));		
	}
	
	public double phi(double load, double cap) {
		return penalty(load, cap, 3, 10, 70, 500, 5000);
	}

	public double phi(double u, double load, double cap) {
		return penalty(u, load, cap, 3, 10, 70, 500, 5000);
	}

	public double delayPen(double delay, double delayReq) {
		return penalty(delay, delayReq, 3, 10, 70, 500, 5000);
	}

	public double penalty(double u, double real, double aim, double c1, double c2, double c3, double c4, double c5) {

		// penalty for links with 0 bw
		if (aim == 0)
			return real * 10000000000.0;

		if (u < 1.0 / 3.0)
			return real;
		else if (u < 2.0 / 3.0)
			return (c1 * real - 2.0 / 3.0 * aim);
		else if (u < 0.9)
			return (c2 * real - 16.0 / 3.0 * aim);
		else if (u < 1.0)
			return (c3 * real - 178.0 / 3.0 * aim);
		else if (u < 1.1)
			return (c4 * real - 1468.0 / 3.0 * aim);
		else
		    return (c5 * real - 16318.0 / 3.0 * aim);
	}

	public double penalty(double real, double aim, double c1, double c2, double c3, double c4, double c5) {
		double u = real / aim;
		return penalty(u, real, aim, c1, c2, c3, c4, c5);
	}

	public double phiUncap(Demands demands) {
		int n = topology.getGraph().getDimension();
		double sum = 0.0;

		topology.setUnitWeights();
		topology.shortestDistances();

		double[][] dists = topology.getShortestPathGraph().getShortestPathDistances();

		for (int s = 0; s < n; s++)
			for (int t = 0; t < n; t++)
				if (demands.getDemands(s, t) > 0)
					sum += demands.getDemands(s, t) * dists[s][t];

		return sum;
	}
	
	public double phiUncap(double[][] demands) {
		int n = topology.getGraph().getDimension();
		double sum = 0.0;

		topology.setUnitWeights();
		topology.shortestDistances();

		double[][] dists = topology.getShortestPathGraph().getShortestPathDistances();

		for (int s = 0; s < n; s++)
			for (int t = 0; t < n; t++)
				if (demands[s][t] > 0)
					sum += demands[s][t] * dists[s][t];

		return sum;
	}

	public double sumEndtoEndDelays() {
		WGraph delay_gr = topology.getNetGraph().createDelayGraph();
		MatDijkstra del = new MatDijkstra(delay_gr);
		del.execute();
		SPFElement[][] preds = del.getSolPreds();
		double[][] dists = del.getShortestPathDistances();

		double sum = 0.0;
		for (int s = 0; s < preds.length; s++)
			for (int t = 0; t < preds.length; t++)
				
				if (s != t && preds[s][t] != null) 
					sum += dists[s][t];

		return sum;
	}

	
	public double[][] getEndtoEndDelays() {
		WGraph delay_gr = topology.getNetGraph().createDelayGraph();
		MatDijkstra del = new MatDijkstra(delay_gr);
		del.execute();
		double[][] dists = del.getShortestPathDistances();
		return dists;
	}

	
	
	
	// Nao usada agora ... serve para escrever ficheiro com problema de
	// linear prog tal como paper do Fortz
	// problema depois é resolvido por um solver
	public void createLPfile(String file, Demands demands) throws Exception {
		int maxLineSize = 250;
		String str = "";

		FileWriter f = new FileWriter(file);
		BufferedWriter W = new BufferedWriter(f);
		W.write("\\* " + file + " *\\\n\n");

		int nnodes = topology.getGraph().getDimension();
		int nedges = topology.getGraph().countEdges();
		int[][] sparse = topology.getGraph().getAllEdges();

		// objective function
		W.write("Minimize\n   value: ");

		int nc = 10;
		for (int a = 0; a < nedges; a++) {
			str = "phi" + a;
			if (a < nedges - 1)
				str = str + "+";
			nc += str.length();
			if (nc < maxLineSize)
				W.write(str);
			else {
				W.write("\n" + str);
				nc = str.length();
			}
		}
		W.write("\n");

		W.write("\n\nSubject To\n");

		for (int s = 0; s < nnodes; s++)
			for (int t = 0; t < nnodes; t++)
				if (demands.getDemands(s, t) > 0) {
					for (int v = 0; v < nnodes; v++) {
						nc = 0;
						boolean ex = false;
						for (int a = 0; a < nedges; a++) {
							if (sparse[a][1] == v) {
								str = " + f" + a + "_" + s + "_" + t;
								// W.write(" + f" + a + "_" + s + "_" + t );
								nc += str.length();
								if (nc < maxLineSize)
									W.write(str);
								else {
									W.write("\n" + str);
									nc = str.length();
								}
								ex = true;
							}
							if (sparse[a][0] == v) {
								str = " - f" + a + "_" + s + "_" + t;
								// W.write(" - f" + a + "_" + s + "_" + t );
								nc += str.length();
								if (nc < maxLineSize)
									W.write(str);
								else {
									W.write("\n" + str);
									nc = str.length();
								}
								ex = true;
							}
						}
						if (v == s)
							str = " = - " + demands.getDemands(s, t);
						else if (v == t)
							str = " = " + demands.getDemands(s, t);
						else if (ex)
							str = " = 0.0";
						nc += str.length();
						if (nc < maxLineSize)
							W.write(str + "\n");
						else
							W.write("\n" + str + "\n");
		
					}
				}

		for (int a = 0; a < nedges; a++) {
			nc = 0;
			W.write("l" + a);
			for (int s = 0; s < nnodes; s++)
				for (int t = 0; t < nnodes; t++)
					if (demands.getDemands(s, t) > 0) {
						str = " - f" + a + "_" + s + "_" + t;
						nc += str.length();
						if (nc < maxLineSize)
							W.write(str);
						else {
							W.write("\n" + str);
							nc = str.length();
						}
					}
			// W.write("=0.0\n");
			str = "=0.0";
			nc += str.length();
			if (nc < maxLineSize)
				W.write(str + "\n");
			else
				W.write("\n" + str + "\n");
		}
		for (int a = 0; a < nedges; a++) // nunca ultrapassam os 250 chars
		{
			double cap = topology.getGraph().getCapacity(sparse[a][0], sparse[a][1]);
			W.write("phi" + a + " - " + "l" + a + " >=0.0\n");
			double c1 = -2.0 / 3.0 * cap;
			W.write("phi" + a + " - " + "3 l" + a + ">= " + c1 + "\n");
			double c2 = -16.0 / 3.0 * cap;
			W.write("phi" + a + " - " + "10 l" + a + ">= " + c2 + "\n");
			double c3 = -178.0 / 3.0 * cap;
			W.write("phi" + a + " - " + "70 l" + a + " >= " + c3 + "\n");
			double c4 = -1468.0 / 3.0 * cap;
			W.write("phi" + a + " - " + "500 l" + a + " >= " + c4 + "\n");
			double c5 = -16318.0 / 3.0 * cap;
			W.write("phi" + a + " - " + "5000 l" + a + ">= " + c5 + "\n");
		}

		W.write("Bounds\n");

		for (int s = 0; s < nnodes; s++)
			for (int t = 0; t < nnodes; t++)
				if (demands.getDemands(s, t) > 0)
					for (int a = 0; a < nedges; a++)
						W.write("f" + a + "_" + s + "_" + t + " >=0\n");

		W.write("\nEnd\n");
		W.write("\n\\* eof *\\\n");
		W.flush();
		W.close();
	}

	
	
	
	public double evalWeights(int[] weights, double alfa, Demands demands, DelayRequests delayReqs)
			throws DimensionErrorException {
		return evalWeights(weights, alfa, false, demands, delayReqs);
	}

	
	
	
	
	protected double evalWeights(int[] weights, double alfa, boolean timeDebug, Demands demands,
			DelayRequests delayReqs) throws DimensionErrorException {
		double[] res = evalWeightsMO(weights, alfa > 0, alfa < 1, timeDebug, demands, delayReqs);
		if (fullDebug) {
			System.out.println("" + (alfa * res[0] + (1.0 - alfa) * res[1]) + "->obj1=" + res[0] + " obj2=" + res[1]);
		}
		return (alfa * res[0] + (1.0 - alfa) * res[1]);
	}

	
	
	
	public Double[] evalWeightsAndUsageRange(int[] weights,Demands demands){
		NetworkLoads loads;
		topology.applyWeights(weights);
		topology.shortestDistances();
		
		loads = new NetworkLoads(totalLoads(demands),topology);
		
		Double[] result = new Double[2];
		result[0] = loads.getCongestion();
		result[1] = loads.getUsageRange();
		return result;
	}  
	
	
	
	public double[] evalWeightsMO(int[] weights, boolean computeCong, boolean computeDelay, boolean timeDebug,
			Demands demands, DelayRequests delayReqs) throws DimensionErrorException {
		if (fullDebug) {
			System.out.println("Weights:");
			for (int i = 0; i < weights.length; i++)
				System.out.print((weights[i]) + " ");
			System.out.println("");
		}

		//NetworkLoads loads;

		topology.applyWeights(weights);
		topology.shortestDistances();
	

		double res = 0.0;
		if (computeCong) {
			loads = new NetworkLoads(totalLoads(demands),topology);
			if (fullDebug)
				loads.printLoads(); // debug

			res = congestionMeasure(loads, demands);
		}

		double res2 = 0.0;
		if (computeDelay) {
				OSPFWeights w = new OSPFWeights(topology.getDimension());
			w.setWeights(weights, topology);

			res2 = computeDelays(w, delayReqs);
		}
		
		
		
		double[] result = new double[2];
		result[0] = res;
		result[1] = res2;
		return result;
	}

	// Compute loads and congestion
	// Neste caso usa-se apenas
	// uma matriz de demands e calcula-se matriz de loads na rede + medidas da
	// congestao
	public void computeLoads(OSPFWeights weights, Demands demands) throws DimensionErrorException {	
		this.loads = new NetworkLoads(topology);
		topology.applyWeights(weights); // apply weights on graph
		this.loads.setLoads(totalLoads(demands));
		this.loads.setCongestion(congestionMeasure(loads, demands));
	}
	
	
	public void computeLoads(int[] weights, Demands d) {
		this.loads = new NetworkLoads(topology);
		topology.applyWeights(weights); // apply weights on graph
		this.loads.setLoads(totalLoads(d));
		this.loads.setCongestion(congestionMeasure(loads, d));
	}

	// Compute average delays
	// Neste caso usa-se apenas uma matriz de DR e calcula-se matriz de
	// average delays na rede+ medidas correspondentes
	public double computeDelays(OSPFWeights weights, DelayRequests delayReqs) throws DimensionErrorException {
		this.endToEndDelays = new AverageEndtoEndDelays(topology.getDimension());

		topology.applyWeights(weights);
		this.endToEndDelays.setEndToEndDelays(this.getEndtoEndDelays());
		this.endToEndDelays.setSumDelays(this.sumEndtoEndDelays());
		this.endToEndDelays.setDelayPenalties(this.delayPenalties(delayReqs));
		return this.endToEndDelays.getDelayPenalties();
	}


	/**
	 * For the already computed loads, considering a weight configuration
	 * a traffic demand matrix,
	 * the method computes the averaged experienced delay
	 * 
	 *TODO: is the average delay a good measure? 
	 *TODO: consider queue effect of delay in function of congestion?  
	 * @return average delay
	 * @throws DimensionErrorException
	 */
	public double computeAverageDelay() throws DimensionErrorException {

		double[][] mloads=this.loads.loads;
		double[][] mdelays=this.getTopology().getNetGraph().getDelays();
		double tload=0.0;
		double tdelay =0.0;
		for(int i=0;i<mloads.length;i++)
			for(int j=0;j<mloads.length;j++){
				tload+=mloads[i][j];
				tdelay+=(mloads[i][j]*mdelays[i][j]);
			}
		return tdelay/tload;
	}

	

	// DIRTY! SIMPLIFY METHOD! REUTILIZATION?! Method not necessary!!!!
	// Compute loads and delays

	public void computeLoadsDelays(OSPFWeights weights, Demands demands, DelayRequests delayReqs)
			throws DimensionErrorException {
		computeLoads(weights, demands);
		computeDelays(weights, delayReqs);
	}

	public ResultSimul simulate(OSPFWeights weights, Demands demands, DelayRequests delayReqs)
			throws DimensionErrorException {
		ResultSimul result = new ResultSimul();
		result.addWeights(weights);
		result.addDemands(demands);
		result.setDelayReqs(delayReqs);
		computeLoadsDelays(weights, demands, delayReqs);
		result.setEndToEndDelays(endToEndDelays);
		result.addNetworkLoads(loads);
		return result;
	}

	@Override
	public NetworkTopology getTopology() {
		return this.topology;
	}
	
	public LoadBalancer getLoadBalancer(){
		return this.lbo;
	}


	


	
	
	

}
