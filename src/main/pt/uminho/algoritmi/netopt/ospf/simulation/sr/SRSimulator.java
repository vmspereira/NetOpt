package pt.uminho.algoritmi.netopt.ospf.simulation.sr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import pt.uminho.algoritmi.netopt.SystemConf;
import pt.uminho.algoritmi.netopt.ospf.graph.Graph;
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
import pt.uminho.algoritmi.netopt.ospf.simulation.PValues;
import pt.uminho.algoritmi.netopt.ospf.simulation.Population;
import pt.uminho.algoritmi.netopt.ospf.simulation.Simul.LoadBalancer;
import pt.uminho.algoritmi.netopt.ospf.simulation.exception.DimensionErrorException;
import pt.uminho.algoritmi.netopt.ospf.simulation.loadballancer.ECMPLoadBalancer;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetEdge;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetNode.NodeType;
import pt.uminho.algoritmi.netopt.ospf.simulation.simulators.SRSimul;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.IntegerSolution;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.Flow.FlowType;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.Segment.SegmentType;


public class SRSimulator {

	public static enum PathCorrection {

		TILFA("TI-LFA", 0), E2ESP("Edge-to-Edge Shortest Path", 1), SALP("SALP", 2);

		private final String name;
		private final int id;

		PathCorrection(String name, int id) {
			this.name = name;
			this.id = id;
		}

		public String getName() {
			return this.name;
		}

		public int getId() {
			return id;
		}
	}

	
	protected OSPFWeights weights;
	protected NetworkTopology topology;
	protected double[][] loads;
	protected Map<Flow, LabelPath> flowPath;
	// Correct paths if failing links are explicitly
	// used, that is, the failing link is path Adjacent segment
	protected boolean CORRECT_PATH;
	protected PathCorrection pathCorrection;
	protected EdgesLoad edgesLoad;
	protected SRConfiguration currentConf;
	
	
	private final static Logger LOGGER = Logger.getLogger(SRSimulator.class.getName());

	public SRSimulator(NetworkTopology topology) {
		this.topology = topology;
		topology.getNetGraph().setAllNodesType(NodeType.SDN_SR);
		loads = new double[topology.getDimension()][topology.getDimension()];
		flowPath = Collections.synchronizedMap(new HashMap<Flow, LabelPath>());
		CORRECT_PATH = true;
		selectLFRecovery();
		edgesLoad = new EdgesLoad();
		currentConf = new SRConfiguration();
		
	}

	private void selectLFRecovery() {
		int lf = SystemConf.getPropertyInt("srsimulator.lf", 0);
		switch (lf) {
		case 0:
			this.pathCorrection = PathCorrection.TILFA;
			break;
		case 1:
			this.pathCorrection = PathCorrection.E2ESP;
			break;
		case 2:
			this.pathCorrection = PathCorrection.SALP;
			break;
		default:
			this.pathCorrection = PathCorrection.TILFA;
			break;
		}
	}

	public SRSimulator(NetworkTopology topology, OSPFWeights weights) {
		this(topology);
		this.weights = weights;
		try {
			this.topology.applyWeights(weights);
		} catch (DimensionErrorException e) {
			e.printStackTrace();
		}
		try {
			edgesLoad.computeLoads(topology, weights.asIntArray(), LoadBalancer.DEFT, true);
		} catch (DimensionErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public double[][] getLoads() {
		return this.loads;
	}

	/**
	 * Add a flow using Shortest Path
	 * 
	 * @param flow
	 * @throws Exception
	 */
	public void addFlow(Flow flow) throws Exception {
		LabelPath path = new LabelPath(topology.getNetGraph().getNodeByID(flow.getSource()),
				topology.getNetGraph().getNodeByID(flow.getDestination()));
		Segment s = new Segment(String.valueOf(flow.getDestination()), SegmentType.NODE);
		s.setSrcNodeId(flow.getSource());
		s.setDstNodeId(flow.getDestination());
		path.addSegment(s);
		addFlow(flow, path);
		currentConf.addLabelPath(path);
	}

	/**
	 * Add a flow using a defined SR Path
	 * 
	 * @param flow
	 * @throws Exception
	 */
	public void addFlow(Flow flow, LabelPath path) throws Exception {
		double[][] l = distribute(flow, path);
		this.flowPath.put(flow, path);
		addLoad(l);
	}

	/**
	 * Remove flow
	 * 
	 * @param flow
	 * @throws Exception
	 */
	public void removeFlow(Flow flow) throws Exception {
		LabelPath path = this.flowPath.get(flow);
		double[][] l = distribute(flow, path);
		this.flowPath.remove(flow);
		subtractLoad(l);
	}

	/**
	 * Distributes a flow's traffic according to an SR path
	 * 
	 * @param flow
	 * @param path
	 * @return a matrix of the flow links utilization. 
	 * @throws Exception
	 */
	private double[][] distribute(Flow flow, LabelPath path) throws Exception {

			
		// Path verification & correction
		
		correctPath(flow,path);
		topology.applyWeights(this.getWeights());
		double[][] l = new double[this.topology.getDimension()][this.topology.getDimension()];
		Iterator<Segment> it = path.getIterator();
		while (it.hasNext()) {
			Segment s = it.next();
			if (s.getType().equals(SegmentType.ADJ)) {
				l[s.getSrcNodeId()][s.getDstNodeId()] += flow.getDemand();

			} else if (s.getType().equals(SegmentType.NODE)) {
				int dest = s.getDstNodeId();
				Graph g=topology.getShortestPathGraph().getArcsShortestPath(dest);
				ECMPLoadBalancer lb = new ECMPLoadBalancer(g);
				ArrayList<Integer> nodes = new ArrayList<Integer>(
						topology.getShortestPathGraph().getNodesForDest(dest));
				Comparator<Integer> comparator = new Comparator<Integer>() {
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
				};
				nodes.sort(comparator);
				// finds the segment src node in the spanning tree
				while (nodes.get(0) != s.getSrcNodeId())
					nodes.remove(0);
				while (nodes.size() > 0) {
					int v = nodes.get(0);
					double sum = 0.0;
					if (v == s.getSrcNodeId())
						sum += flow.getDemand();
					else {
						for (int u = 0; u < topology.getDimension(); u++)
							if (topology.getNetGraph().existEdge(u, v)
									&& topology.getNetGraph().getEdge(u, v).isUP())
								sum += l[u][v];
					}
					// for each arc leaving from this node
					for (int w = 0; w < topology.getDimension(); w++) {
						if (topology.getNetGraph().existEdge(v, w) && topology.getNetGraph().getEdge(v, w).isUP()) {
							l[v][w] = lb.getSplitRatio(v, dest, v, w) * sum;
						}
					}
					nodes.remove(0);
				}
			}
		}
//		for(int i=0;i<l.length;i++){
//			for(int j=0;j<l.length;j++)
//				if(l[i][j]!=0)
//				System.out.print(" ["+i+"->"+j+"]="+l[i][j]);
//		}
//		System.out.println();
		return l;
	}


	
	
	
	private void addLoad(double[][] l) {
		for (int i = 0; i < topology.getDimension(); i++)
			for (int j = 0; j < topology.getDimension(); j++)
				this.loads[i][j] += l[i][j];
	}

	private void subtractLoad(double[][] l) {
		for (int i = 0; i < topology.getDimension(); i++)
			for (int j = 0; j < topology.getDimension(); j++)
				this.loads[i][j] -= l[i][j];
	}

	public NetworkTopology getTopology() {
		return this.topology;
	}

	public double getUsage(int from, int to) {
		return this.loads[from][to] / topology.getNetGraph().getEdge(from, to).getBandwidth();
	}

	public double getLoad(int from, int to) {
		return this.loads[from][to];
	}

	public double[][] getLoads(FlowType type) throws Exception{
		double[][] l = new double[topology.getDimension()][topology.getDimension()];
		Iterator<Flow> it=flowPath.keySet().iterator();
		while(it.hasNext()){
			Flow f=it.next();
			if(f.getFlowType().equals(type)){
				LabelPath p=flowPath.get(f);
				double[][] d= distribute(f, p);
				for(int i=0;i<topology.getDimension();i++)
					for(int j=0;j<topology.getDimension();j++)
						l[i][j]+=d[i][j];
			}
		}
		return l;
	}
	
	/**
	 * Clear loads and remove all configured flows
	 */
	public synchronized void clear() {
		loads = new double[topology.getDimension()][topology.getDimension()];
		flowPath = Collections.synchronizedMap(new HashMap<Flow, LabelPath>());
	}
	
	/**
	 * Remove all traffic from a specific flow type (SALP, TI-LFA, E2E, USER)
	 * @param Flow type
	 * @throws Exception
	 */
	public synchronized  void clear(FlowType type) throws Exception {
		Iterator<Flow> it=flowPath.keySet().iterator();
		ArrayList<Flow> removeList = new ArrayList<Flow>();
		while(it.hasNext()){
			Flow f=it.next();
			if(f.getFlowType().equals(type)){
				removeList.add(f);
			}
		}
		//remove flow 
		for(Flow f:removeList)
			removeFlow(f);
	}

	/**
	 * Performs alterations to load balancing
	 * 
	 * @param Node-p configuration
	 * @throws Exception
	 */
	public synchronized  void  apply(PValues p) throws Exception {
		Demands demands = this.getDemands(FlowType.SALP);
		apply(p, demands);
	}

	public void apply(PValues p, Demands demands) throws Exception {
		SRSimul simul = new SRSimul(this.topology.copy());
		simul.setConfigureSRPath(true);
		simul.computeLoads(weights, p, demands);
		SRConfiguration conf = simul.getSRconfiguration();
		clear(FlowType.SALP);
		apply(conf, demands);
	}

	
	/**
	 * Configures the network with an SR configuration and distributes traffic demands
	 * 
	 * @param SR configuration
	 * @param Traffic demand matrix
	 */
	public void apply(SRConfiguration conf, Demands demands) {

	//	try {
	//		topology.applyWeights(weights);
	//	} catch (DimensionErrorException e1) {
	//		e1.printStackTrace();
	//	}
		Collection<SRNodeConfiguration> n = conf.getNodesConfigurations();
		for (SRNodeConfiguration nc : n) {
			Collection<ArrayList<LabelPath>> u = nc.getConfiguration().values();
			for (ArrayList<LabelPath> a : u) {
				for (LabelPath b : a) {
					int s = b.getSource().getNodeId();
					int t = b.getDestination().getNodeId();
					Flow f = new Flow(s, t, Flow.FlowType.SALP);
					f.setFraction(b.getFraction());
					double d = b.getFraction() * demands.getDemands(s, t);
					f.setDemand(d);
					// adds the flow
					try {
						addFlow(f, b);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		////////////////////////////////////
		//print(this.getLoads());
		//try {
		//	SRSimul s = new SRSimul(this.topology.copy(),true);
		//	double[][] l2 =s.totalLoads(demands, weights, conf, true);
		//	print(l2);
		//} catch (DimensionErrorException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}
	}

	
	public LabelPath getPath(Flow f) {
		return flowPath.get(f);
	}

	public Flow getFlow(int index) {
		Flow[] flows = flowPath.keySet().toArray(new Flow[flowPath.size()]);
		return flows[index];
	}

	public int getNumberOfFlows() {
		return flowPath.size();
	}

	/**
	 * Verify if a SR path is valid - verify if all adj-segments are up - @todo
	 * other validations
	 * 
	 * @param path
	 * @return
	 */
	public boolean validatePath(LabelPath path) {
		List<Segment> list = path.getAdjacentSegment();
		for (Segment s : list) {
			NetEdge e = topology.getNetGraph().getEdge(s.getSrcNodeId(), s.getDstNodeId());
			if (!e.isUP())
				return false;
		}
		return true;
	}

	/**
	 * Corrects a path if not valid
	 * @param flow 
	 * 
	 * @param path
	 * @return
	 */
	private LabelPath correctPath(Flow flow, LabelPath path) throws Exception {
		LabelPath p = new LabelPath(path.getSource(), path.getDestination());
		p.setFraction(path.getFraction());

		List<Segment> list = path.getLabels();
		boolean stop=false;
		for (int i=0; i<list.size() && !stop; i++) {
			Segment s =list.get(i);
			if (s.getType().equals(SegmentType.NODE))
				p.addSegment(s);
			else {
				NetEdge e = topology.getNetGraph().getEdge(s.getSrcNodeId(), s.getDstNodeId());
				if (e.isUP()){
					p.addSegment(s);
				}
				else {
					if (!CORRECT_PATH)
						throw new Exception("Invalid path: Correction option disabled");
					if (this.pathCorrection == PathCorrection.TILFA) {
						Segment ss = new Segment(path.getDestination().toString(), SegmentType.NODE);
						ss.setSrcNodeId(s.getSrcNodeId());
						ss.setDstNodeId(path.getDestination().getNodeId());
						p.addSegment(ss);
						LOGGER.log(Level.FINE, "Label Path "+path.toString()+" altered using TI-LFA to "+p.toString());
						path.setLabels(p.getLabels());
						flow.setFlowType(FlowType.TILFA);
						stop =true;
							
					} else if (this.pathCorrection == PathCorrection.E2ESP) {
						p = new LabelPath(path.getSource(), path.getDestination());
						p.setFraction(path.getFraction());
						Segment ss = new Segment(path.getDestination().toString(), SegmentType.NODE);
						ss.setSrcNodeId(path.getSource().getNodeId());
						ss.setDstNodeId(path.getDestination().getNodeId());
						p.addSegment(ss);
						LOGGER.log(Level.FINE, "Label Path "+path.toString()+" altered using Edge to Edge SP to "+p.toString());
						path.setLabels(p.getLabels());
						flow.setFlowType(FlowType.E2E);
						stop =true;
					}
				}
			}
		}
		return path;
	}

	public Demands getDemands() {
		double[][] demands = new double[topology.getDimension()][topology.getDimension()];
		Iterator<Flow> it = this.flowPath.keySet().iterator();
		while (it.hasNext()) {
			Flow f = it.next();
			demands[f.getSource()][f.getDestination()] += f.getDemand();
		}
		return new Demands(demands);
	}

	
	public Demands getDemands(FlowType type) {
		double[][] demands = new double[topology.getDimension()][topology.getDimension()];
		Iterator<Flow> it = this.flowPath.keySet().iterator();
		while (it.hasNext()) {
			Flow f = it.next();
			if(f.getFlowType().equals(type))
				demands[f.getSource()][f.getDestination()] += f.getDemand();
		}
		return new Demands(demands);
	}
	
	
	
	public Demands getAggregatedDemands() {
		double[][] demands = new double[topology.getDimension()][topology.getDimension()];
		Iterator<Flow> it = this.flowPath.keySet().iterator();
		while (it.hasNext()) {
			Flow f = it.next();
			if (f.isAggregated())
				demands[f.getSource()][f.getDestination()] += f.getDemand();
		}
		return new Demands(demands);
	}

	public double getCongestionValue() {
		NetworkLoads nloads = new NetworkLoads(this.loads, this.topology);
		Demands demands = getDemands();
		SRSimul simul = new SRSimul(topology);
		double congestion = simul.congestionMeasure(nloads, demands);
		return congestion;
	}

	public void setWeights(OSPFWeights weigths) {
		try {
			this.topology.applyWeights(weigths);
		} catch (DimensionErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		this.weights = weigths;
		this.loads = new double[topology.getDimension()][topology.getDimension()];
		Iterator<Flow> flows = flowPath.keySet().iterator();
		while (flows.hasNext()) {
			Flow f = flows.next();
			LabelPath b = flowPath.get(f);
			try {
				addFlow(f, b);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Apply a topology change event (link up/down)
	 * 
	 * @param NetEdge
	 *            edge
	 * 
	 *            NOTE: supposes link status was already updated
	 * 
	 */
	public void failLink(NetEdge e) {

		// clear network loads
		loads = new double[topology.getDimension()][topology.getDimension()];
		// recompute shortest paths
		try {
			topology.applyWeights(weights);
		} catch (DimensionErrorException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//check the user selected recovery option
		selectLFRecovery();
		//SAPL path correction requires all paths and load balancing
		//ratios to be recomputed.
		if (this.pathCorrection == PathCorrection.SALP) {
			try {
				SRSimul sim = new SRSimul(topology.copy());
				sim.setConfigureSRPath(true);
				Demands d= this.getDemands().copy();
				sim.computeLoads(weights, this.getDemands());
				SRConfiguration conf=sim.getSRconfiguration();
				this.clear();
				this.apply(conf, d);
			} catch (DimensionErrorException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else {
			// adds individual flows and corrects affected paths
			Iterator<Flow> flows = flowPath.keySet().iterator();
			while (flows.hasNext()) {
				Flow f = flows.next();
				LabelPath b = flowPath.get(f);
				try {
					addFlow(f, b);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}

	public double getMLU() {
		double res = 0.0;
		for (int i = 0; i < topology.getDimension(); i++) {
			for (int j = 0; j < topology.getDimension(); j++) {
				double u;
				try {
					u = getUsage(i, j);
				} catch (Exception e) {
					u = 0;
				}
				if (u > res)
					res = u;
			}
		}
		return res;
	}

	public OSPFWeights getWeights() {
		return this.weights;
	}

	
	
	public PValues optimizeNodeP() throws Exception{
		JecoliPValueInteger eaOspf = new JecoliPValueInteger(getTopology().copy(),
				getDemands(FlowType.SALP), getWeights());
		
		double[][] l1=getLoads();
		double[][] l2=getLoads(FlowType.SALP);
		
		double[][] fixedLoads=new double[getTopology().getDimension()][getTopology().getDimension()];
		for(int i=0;i<getTopology().getDimension();i++)
			for(int j=0;j<getTopology().getDimension();j++)
				fixedLoads[i][j]=l1[i][j]-l2[i][j];
		
		Params params = new Params();
		params.setLoadBalancer(LoadBalancer.DEFT);
		params.setArchiveSize(100);
		params.setPopulationSize(100);
		int node_p_iterations = SystemConf.getPropertyInt("srsimulator.piterations", 100);
		params.setNumberGenerations(node_p_iterations);
		params.setInitialLoads(fixedLoads);
		eaOspf.setSecondDemands(getDemands());
		eaOspf.configureSRNLSGAIIAlgorithm(params);
		eaOspf.run();
		
		double[] pvalues = eaOspf.getBestSolutionReal();
		PValues p = new PValues(pvalues);
		this.apply(p);
		return p;
	}
	
	
	
	public void print(double[][] d){
		for(int i=0;i<d.length;i++){
			for(int j=0;j<d.length;j++)
				System.out.print(d[i][j]+";");
			System.out.println();
		}
		
		
	}
	
	
	
	
	public void constrainedMT(NetEdge e){
		Demands[] d = edgesLoad.getEdgeToEdgePartialDemand(this.getDemands(), e,EdgesLoad.EdgeNodeIn.LEFT_RIGHT);
		Params parameters = new Params();
		parameters.setArchiveSize(100);
		parameters.setPopulationSize(100);
		parameters.setLoadBalancer(LoadBalancer.DEFT);
		parameters.setSecondObjective(AlgorithmSecondObjective.MLU);
		parameters.setPreviousWeights(weights);
		parameters.setNumberGenerations(100);
		try {
			JecoliOSPF ea = new JecoliOSPF(topology, d, null);
			ea.configureConstrainedSRLMTNSGAII(parameters);
			ea.run();
			Population p = new NondominatedPopulation(ea.getSolutionSet());
			IntegerSolution sol=p.getLowestValuedSolutions(0, 1).get(0);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	
		
	}
	
	
	
	
}
