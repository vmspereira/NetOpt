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
package pt.uminho.algoritmi.netopt.ospf.optimization.jecoli;

import java.util.ArrayList;
import jecoli.algorithm.components.algorithm.IAlgorithmResult;
import jecoli.algorithm.components.algorithm.IAlgorithmStatistics;
import jecoli.algorithm.components.algorithm.writer.IAlgorithmResultWriter;
import jecoli.algorithm.components.configuration.InvalidConfigurationException;
import jecoli.algorithm.components.evaluationfunction.IEvaluationFunction;
import jecoli.algorithm.components.operator.container.ReproductionOperatorContainer;
import jecoli.algorithm.components.operator.reproduction.linear.IntegerAddMutation;
import jecoli.algorithm.components.operator.reproduction.linear.LinearGenomeRandomMutation;
import jecoli.algorithm.components.operator.reproduction.linear.TwoPointCrossOver;
import jecoli.algorithm.components.operator.reproduction.linear.UniformCrossover;
import jecoli.algorithm.components.operator.selection.TournamentSelection;
import jecoli.algorithm.components.operator.selection.TournamentSelection2;
import jecoli.algorithm.components.randomnumbergenerator.DefaultRandomNumberGenerator;
import jecoli.algorithm.components.randomnumbergenerator.IRandomNumberGenerator;
import jecoli.algorithm.components.representation.integer.IntegerRepresentationFactory;
import jecoli.algorithm.components.representation.linear.ILinearRepresentation;
import jecoli.algorithm.components.representation.linear.ILinearRepresentationFactory;
import jecoli.algorithm.components.representation.linear.LinearRepresentation;
import jecoli.algorithm.components.solution.ISolution;
import jecoli.algorithm.components.solution.ISolutionFactory;
import jecoli.algorithm.components.solution.ISolutionSet;
import jecoli.algorithm.components.solution.Solution;
import jecoli.algorithm.components.statistics.StatisticsConfiguration;
import jecoli.algorithm.components.terminationcriteria.ITerminationCriteria;
import jecoli.algorithm.components.terminationcriteria.IterationTerminationCriteria;
import jecoli.algorithm.multiobjective.archive.components.ArchiveManager;
import jecoli.algorithm.multiobjective.archive.components.InsertionStrategy;
import jecoli.algorithm.multiobjective.archive.components.ProcessingStrategy;
import jecoli.algorithm.multiobjective.archive.plotting.IPlotter;
import jecoli.algorithm.multiobjective.archive.trimming.ITrimmingFunction;
import jecoli.algorithm.multiobjective.archive.trimming.ZitzlerTruncation;
import jecoli.algorithm.multiobjective.nsgaII.NSGAIIConfiguration;
import jecoli.algorithm.singleobjective.evolutionary.EvolutionaryConfiguration;
import jecoli.algorithm.singleobjective.evolutionary.RecombinationParameters;
import pt.uminho.algoritmi.netopt.SystemConf;
import pt.uminho.algoritmi.netopt.ospf.optimization.Params;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.algorithm.AlgorithmInterface;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.algorithm.OSPFEvolutionaryAlgorithm;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.algorithm.OSPFNSGAII;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.evaluation.pdeft.DEFTPValueIntegerEvaluation;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.evaluation.pdeft.DEFTPValueIntegerEvaluationMO;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.evaluation.pdeft.DEFTWeightsPValueIntegerEvaluation;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.evaluation.pdeft.DEFTWeightsPValueIntegerEvaluationMO;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.evaluation.sr.SRPValueIntegerEvaluationMO;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.evaluation.sr.SRPValueLoadIntegerEvaluationMO;
import pt.uminho.algoritmi.netopt.ospf.simulation.Demands;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.NondominatedPopulation;
import pt.uminho.algoritmi.netopt.ospf.simulation.OSPFWeights;
import pt.uminho.algoritmi.netopt.ospf.simulation.Population;
import pt.uminho.algoritmi.netopt.ospf.simulation.Simul.LoadBalancer;
import pt.uminho.algoritmi.netopt.ospf.simulation.exception.DimensionErrorException;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.ASolutionSet;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.AbstractSolutionSet;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.IntegerSolution;

public class JecoliPValueInteger {
	
	
	private NetworkTopology topology;
	private Demands demands;
	private Demands demands2;
	private OSPFWeights weights;
	private IAlgorithmResult<ILinearRepresentation<Integer>> results;
	private IAlgorithmStatistics<ILinearRepresentation<Integer>> statistics;
	protected IRandomNumberGenerator randomNumberGenerator;
	protected ArchiveManager<Integer, ILinearRepresentation<Integer>> archive;
	private AlgorithmInterface<Integer> algorithm;
	private int MINWeight = 1;
	private int MAXWeight = 20;	
	private int lowerLimit=1;
	private int upperLimit=1000;
	private int divider=100;

	
	public JecoliPValueInteger(NetworkTopology topology, Demands demands, OSPFWeights weights){
		this.topology=topology;
		this.demands=demands;
		this.weights=weights;
		randomNumberGenerator = new DefaultRandomNumberGenerator();
		divider=SystemConf.getPropertyInt("pvalue.divider",100);
		lowerLimit=(int) (SystemConf.getPropertyDouble("pvalue.min",0.01)*divider);
		upperLimit=(int) (SystemConf.getPropertyDouble("pvalue.max",10.0)*divider);
		MAXWeight=SystemConf.getPropertyInt("ospf.maxweight",20);
    	MINWeight=SystemConf.getPropertyInt("ospf.minweight",1);
	}
	
	
	
	public JecoliPValueInteger(NetworkTopology topology, Demands demand1, Demands demand2) {
		this.topology=topology;
		this.demands=demand1;
		this.demands2=demand2;
		this.weights=null;
		randomNumberGenerator = new DefaultRandomNumberGenerator();
		divider=SystemConf.getPropertyInt("pvalue.divider",100);
		lowerLimit=(int) (SystemConf.getPropertyDouble("pvalue.min",0.01)*divider);
		upperLimit=(int) (SystemConf.getPropertyDouble("pvalue.max",10.0)*divider);
		MAXWeight=SystemConf.getPropertyInt("ospf.maxweight",20);
    	MINWeight=SystemConf.getPropertyInt("ospf.minweight",1);
	}



	public JecoliPValueInteger(NetworkTopology topology, Demands demands) {
		this.topology=topology;
		this.demands=demands;
		this.demands2=null;
		this.weights=null;
		randomNumberGenerator = new DefaultRandomNumberGenerator();
		divider=SystemConf.getPropertyInt("pvalue.divider",100);
		lowerLimit=(int) (SystemConf.getPropertyDouble("pvalue.min",0.01)*divider);
		upperLimit=(int) (SystemConf.getPropertyDouble("pvalue.max",10.0)*divider);
		MAXWeight=SystemConf.getPropertyInt("ospf.maxweight",20);
    	MINWeight=SystemConf.getPropertyInt("ospf.minweight",1);
	}


	/*
	 * Includes default p value into the initial population 
	 */
	private ISolutionSet<ILinearRepresentation<Integer>> buildPValueInitialPopulation(
			Params params,
			ISolutionFactory<ILinearRepresentation<Integer>> solutionFactory)
			throws DimensionErrorException {

		// solution set
		ISolutionSet<ILinearRepresentation<Integer>> newSolutionSet;
		int numberOfObjective = solutionFactory.getNumberOfObjectives();
		int populationSize = params.getPopulationSize();
		ArrayList<Integer> genome = new ArrayList<Integer>();
		for (int i = 0; i < topology.getDimension(); i++)
					genome.add(divider);
		LinearRepresentation<Integer> r = new LinearRepresentation<Integer>(genome);
		ISolution<ILinearRepresentation<Integer>> s = new Solution<ILinearRepresentation<Integer>>(r,numberOfObjective);
		
		
		
		newSolutionSet = solutionFactory.generateSolutionSet(populationSize-1,
				new DefaultRandomNumberGenerator());
		newSolutionSet.add(s);
	    return newSolutionSet;
	}

	
	

	public ILinearRepresentationFactory<Integer> configureSolutionFactory (int numberVariables)
	{	
		return new IntegerRepresentationFactory(numberVariables,this.lowerLimit,this.upperLimit,1);
	}
	
	public ILinearRepresentationFactory<Integer> configureSolutionFactoryWP (int numberP, int numberW, int numberOfObjectives)
	{	int numberVariables =numberP+numberW;
	    ArrayList<Integer> l=new ArrayList<Integer>();
	    ArrayList<Integer> u=new ArrayList<Integer>();
	    for(int i=0;i<numberP;i++){
	    	l.add(i,lowerLimit);u.add(i,upperLimit);
	    }
	    for(int i=numberP;i<numberVariables;i++){
	    	l.add(i,MINWeight);u.add(i,MAXWeight);
	    }
		return new IntegerRepresentationFactory(numberVariables,l,u,numberOfObjectives);
	}
	
	
	
	public void configureEvolutionaryAlgorithm(Params params) throws Exception,
	InvalidConfigurationException {
		boolean use_deft=params.getLoadBalancer().equals(LoadBalancer.DEFT)?true:false;
		DEFTPValueIntegerEvaluation f =new DEFTPValueIntegerEvaluation(this.topology,this.demands, this.weights.asIntArray(),use_deft);
		
		EvolutionaryConfiguration<ILinearRepresentation<Integer>,ILinearRepresentationFactory<Integer>> configuration = new EvolutionaryConfiguration<ILinearRepresentation<Integer>,ILinearRepresentationFactory<Integer>>();
		
		configuration.setEvaluationFunction(f);
		
		ILinearRepresentationFactory<Integer> solutionFactory = configureSolutionFactory(topology.getDimension()); 
		configuration.setSolutionFactory( solutionFactory );
		
		configuration.setPopulationSize(params.getPopulationSize());
		
				
		configuration.setRandomNumberGenerator(new DefaultRandomNumberGenerator());
		configuration.setProblemBaseDirectory("nullDirectory");
		configuration.setAlgorithmStateFile("nullFile");
		configuration.setSaveAlgorithmStateDirectoryPath("nullDirectory");
		configuration.setAlgorithmResultWriterList(new ArrayList<IAlgorithmResultWriter<ILinearRepresentation<Integer>>>());
		configuration.setStatisticsConfiguration(new StatisticsConfiguration());
		
		ITerminationCriteria terminationCriteria = new IterationTerminationCriteria(params.getNumberGenerations());
		configuration.setTerminationCriteria(terminationCriteria);
	
		RecombinationParameters recombinationParameters = new RecombinationParameters(params.getPopulationSize());
		configuration.setRecombinationParameters(recombinationParameters);
		
		configuration.setSelectionOperators(new TournamentSelection<ILinearRepresentation<Integer>>(1,2));
		configuration
		.setSurvivorSelectionOperator(new TournamentSelection<ILinearRepresentation<Integer>>(
				1, 2));

		
		ReproductionOperatorContainer<ILinearRepresentation<Integer>,ILinearRepresentationFactory<Integer>> operatorContainer = createReproductionOperators();
		configuration.setReproductionOperatorContainer(operatorContainer);
		
		algorithm = new OSPFEvolutionaryAlgorithm(configuration);

	}
	
	
	
	public void configureWeightAndPValuesEA(Params params) throws Exception,
	InvalidConfigurationException {
		
		boolean use_deft=params.getLoadBalancer().equals(LoadBalancer.DEFT)?true:false;
		DEFTWeightsPValueIntegerEvaluation f =new DEFTWeightsPValueIntegerEvaluation(this.topology,this.demands,use_deft);
		
		EvolutionaryConfiguration<ILinearRepresentation<Integer>,ILinearRepresentationFactory<Integer>> configuration = new EvolutionaryConfiguration<ILinearRepresentation<Integer>,ILinearRepresentationFactory<Integer>>();
		
		configuration.setEvaluationFunction(f);
		
		ILinearRepresentationFactory<Integer> solutionFactory = configureSolutionFactoryWP(topology.getDimension(),topology.getNumberEdges(),1);
		configuration.setSolutionFactory( solutionFactory );
		
		configuration.setPopulationSize(params.getPopulationSize());
		
		configuration.setRandomNumberGenerator(new DefaultRandomNumberGenerator());
		configuration.setProblemBaseDirectory("nullDirectory");
		configuration.setAlgorithmStateFile("nullFile");
		configuration.setSaveAlgorithmStateDirectoryPath("nullDirectory");
		configuration.setAlgorithmResultWriterList(new ArrayList<IAlgorithmResultWriter<ILinearRepresentation<Integer>>>());
		configuration.setStatisticsConfiguration(new StatisticsConfiguration());
		
		ITerminationCriteria terminationCriteria = new IterationTerminationCriteria(params.getNumberGenerations());
		configuration.setTerminationCriteria(terminationCriteria);
	
		RecombinationParameters recombinationParameters = new RecombinationParameters(params.getPopulationSize());
		configuration.setRecombinationParameters(recombinationParameters);
		
		configuration.setSelectionOperators(new TournamentSelection<ILinearRepresentation<Integer>>(1,2));
		
		ReproductionOperatorContainer<ILinearRepresentation<Integer>,ILinearRepresentationFactory<Integer>> operatorContainer = 	createReproductionOperators();
		configuration.setReproductionOperatorContainer(operatorContainer);
		
		algorithm = new OSPFEvolutionaryAlgorithm(configuration);

	}
	
	
	
	
	public void configureNSGAIIAlgorithm(Params params) throws Exception,
	InvalidConfigurationException {
		boolean use_deft=params.getLoadBalancer().equals(LoadBalancer.DEFT)?true:false;
		DEFTPValueIntegerEvaluationMO f =new DEFTPValueIntegerEvaluationMO(this.topology,this.demands, this.weights.asIntArray(),use_deft,divider);
		NSGAIIConfiguration<ILinearRepresentation<Integer>,ILinearRepresentationFactory<Integer>> configuration = new NSGAIIConfiguration<ILinearRepresentation<Integer>,ILinearRepresentationFactory<Integer>>();
		
		configuration.setEvaluationFunction(f);
		
		IntegerRepresentationFactory solutionFactory =new IntegerRepresentationFactory(topology.getDimension(),this.lowerLimit,this.upperLimit,2); 
		configuration.setSolutionFactory( solutionFactory );
//-------		
		ISolutionSet<ILinearRepresentation<Integer>> newSolutions = buildPValueInitialPopulation(
				params, solutionFactory);
		configuration.setInitialPopulation(newSolutions);
		configuration.setPopulationInitialization(false);
//----------
		
		configuration.setPopulationSize(params.getPopulationSize());
		configuration.setNumberOfObjectives(2);
		configuration.setRandomNumberGenerator(new DefaultRandomNumberGenerator());
		configuration.setProblemBaseDirectory("nullDirectory");
		configuration.setAlgorithmStateFile("nullFile");
		configuration.setSaveAlgorithmStateDirectoryPath("nullDirectory");
		configuration.setAlgorithmResultWriterList(new ArrayList<IAlgorithmResultWriter<ILinearRepresentation<Integer>>>());
		configuration.setStatisticsConfiguration(new StatisticsConfiguration());
		
		ITerminationCriteria terminationCriteria = new IterationTerminationCriteria(params.getNumberGenerations());
		configuration.setTerminationCriteria(terminationCriteria);
	
		RecombinationParameters recombinationParameters = new RecombinationParameters(params.getPopulationSize());
		configuration.setRecombinationParameters(recombinationParameters);
		
		configuration.
		setSelectionOperator(new TournamentSelection2<ILinearRepresentation<Integer>>(
				1, 2, randomNumberGenerator));
		
		configuration.getStatisticConfiguration()
		.setNumberOfBestSolutionsToKeepPerRun(
				params.getPopulationSize());


		
		ReproductionOperatorContainer<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>> operatorContainer = 	createReproductionOperators();
		configuration.setReproductionOperatorContainer(operatorContainer);
		
		algorithm = new OSPFNSGAII(configuration);		
	}
	
	
	
	public void configureSRNSGAIIAlgorithm(Params params) throws Exception,
	InvalidConfigurationException {
		SRPValueIntegerEvaluationMO f =new SRPValueIntegerEvaluationMO(this.topology,this.demands, this.weights.asIntArray(),params.getLoadBalancer());
		NSGAIIConfiguration<ILinearRepresentation<Integer>,ILinearRepresentationFactory<Integer>> configuration = new NSGAIIConfiguration<ILinearRepresentation<Integer>,ILinearRepresentationFactory<Integer>>();
		
		configuration.setEvaluationFunction(f);
		
		IntegerRepresentationFactory solutionFactory =new IntegerRepresentationFactory(topology.getDimension(),this.lowerLimit,this.upperLimit,2); 
		configuration.setSolutionFactory( solutionFactory );

		//-------		
		ISolutionSet<ILinearRepresentation<Integer>> newSolutions = buildPValueInitialPopulation(
				params, solutionFactory);
		configuration.setInitialPopulation(newSolutions);
		configuration.setPopulationInitialization(false);
        //----------
		
		configuration.setPopulationSize(params.getPopulationSize());
		configuration.setNumberOfObjectives(2);
		configuration.setRandomNumberGenerator(new DefaultRandomNumberGenerator());
		configuration.setProblemBaseDirectory("nullDirectory");
		configuration.setAlgorithmStateFile("nullFile");
		configuration.setSaveAlgorithmStateDirectoryPath("nullDirectory");
		configuration.setAlgorithmResultWriterList(new ArrayList<IAlgorithmResultWriter<ILinearRepresentation<Integer>>>());
		configuration.setStatisticsConfiguration(new StatisticsConfiguration());
		
		ITerminationCriteria terminationCriteria = new IterationTerminationCriteria(params.getNumberGenerations());
		configuration.setTerminationCriteria(terminationCriteria);
	
		RecombinationParameters recombinationParameters = new RecombinationParameters(params.getPopulationSize());
		configuration.setRecombinationParameters(recombinationParameters);
		
		configuration.
		setSelectionOperator(new TournamentSelection2<ILinearRepresentation<Integer>>(
				1, 2, randomNumberGenerator));
		
		configuration.getStatisticConfiguration()
		.setNumberOfBestSolutionsToKeepPerRun(
				params.getPopulationSize());


		
		ReproductionOperatorContainer<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>> operatorContainer = 	createReproductionOperators();
		configuration.setReproductionOperatorContainer(operatorContainer);
		
		algorithm = new OSPFNSGAII(configuration);		
	}


	public void configureSRNLSGAIIAlgorithm(Params params) throws Exception,
	InvalidConfigurationException {
		SRPValueLoadIntegerEvaluationMO f =new SRPValueLoadIntegerEvaluationMO(this.topology,this.demands, this.weights.asIntArray(),params.getInitialLoads(),this.demands2);
		NSGAIIConfiguration<ILinearRepresentation<Integer>,ILinearRepresentationFactory<Integer>> configuration = new NSGAIIConfiguration<ILinearRepresentation<Integer>,ILinearRepresentationFactory<Integer>>();
		
		configuration.setEvaluationFunction(f);
		
		IntegerRepresentationFactory solutionFactory =new IntegerRepresentationFactory(topology.getDimension(),this.lowerLimit,this.upperLimit,2); 
		configuration.setSolutionFactory( solutionFactory );

		//-------		
		ISolutionSet<ILinearRepresentation<Integer>> newSolutions = buildPValueInitialPopulation(
				params, solutionFactory);
		configuration.setInitialPopulation(newSolutions);
		configuration.setPopulationInitialization(false);
        //----------
		
		configuration.setPopulationSize(params.getPopulationSize());
		configuration.setNumberOfObjectives(2);
		configuration.setRandomNumberGenerator(new DefaultRandomNumberGenerator());
		configuration.setProblemBaseDirectory("nullDirectory");
		configuration.setAlgorithmStateFile("nullFile");
		configuration.setSaveAlgorithmStateDirectoryPath("nullDirectory");
		configuration.setAlgorithmResultWriterList(new ArrayList<IAlgorithmResultWriter<ILinearRepresentation<Integer>>>());
		configuration.setStatisticsConfiguration(new StatisticsConfiguration());
		
		ITerminationCriteria terminationCriteria = new IterationTerminationCriteria(params.getNumberGenerations());
		configuration.setTerminationCriteria(terminationCriteria);
	
		RecombinationParameters recombinationParameters = new RecombinationParameters(params.getPopulationSize());
		configuration.setRecombinationParameters(recombinationParameters);
		
		configuration.
		setSelectionOperator(new TournamentSelection2<ILinearRepresentation<Integer>>(
				1, 2, randomNumberGenerator));
		
		configuration.getStatisticConfiguration()
		.setNumberOfBestSolutionsToKeepPerRun(
				params.getPopulationSize());


		
		ReproductionOperatorContainer<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>> operatorContainer = 	createReproductionOperators();
		configuration.setReproductionOperatorContainer(operatorContainer);
		
		algorithm = new OSPFNSGAII(configuration);		
	}

	
	
	/**
	 * Configures the optimization for two demand matrices
	 * @param params
	 * @throws Exception
	 * @throws InvalidConfigurationException
	 */
	public void configureTwoDemandsAlgorithm(Params params) throws Exception,
	InvalidConfigurationException {
		boolean use_deft=params.getLoadBalancer().equals(LoadBalancer.DEFT)?true:false;
		Demands[] d=new Demands[2];
		d[0]=this.demands;d[1]=this.demands2;
		DEFTWeightsPValueIntegerEvaluationMO f =new DEFTWeightsPValueIntegerEvaluationMO(this.topology,d,use_deft);
		NSGAIIConfiguration<ILinearRepresentation<Integer>,ILinearRepresentationFactory<Integer>> configuration = new NSGAIIConfiguration<ILinearRepresentation<Integer>,ILinearRepresentationFactory<Integer>>();
		
		configuration.setEvaluationFunction(f);
		
		ILinearRepresentationFactory<Integer> solutionFactory = configureSolutionFactoryWP(topology.getDimension(),topology.getNumberEdges(),2); 
		configuration.setSolutionFactory( solutionFactory );
		
		configuration.setPopulationSize(params.getPopulationSize());
		configuration.setNumberOfObjectives(2);
		configuration.setRandomNumberGenerator(new DefaultRandomNumberGenerator());
		configuration.setProblemBaseDirectory("nullDirectory");
		configuration.setAlgorithmStateFile("nullFile");
		configuration.setSaveAlgorithmStateDirectoryPath("nullDirectory");
		configuration.setAlgorithmResultWriterList(new ArrayList<IAlgorithmResultWriter<ILinearRepresentation<Integer>>>());
		configuration.setStatisticsConfiguration(new StatisticsConfiguration());
		
		ITerminationCriteria terminationCriteria = new IterationTerminationCriteria(params.getNumberGenerations());
		configuration.setTerminationCriteria(terminationCriteria);
	
		RecombinationParameters recombinationParameters = new RecombinationParameters(params.getPopulationSize());
		configuration.setRecombinationParameters(recombinationParameters);
		
		configuration.
		setSelectionOperator(new TournamentSelection2<ILinearRepresentation<Integer>>(
				1, 2, randomNumberGenerator));
		
		configuration.getStatisticConfiguration()
		.setNumberOfBestSolutionsToKeepPerRun(
				params.getPopulationSize());


		
		ReproductionOperatorContainer<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>> operatorContainer = 	createReproductionOperators();
		configuration.setReproductionOperatorContainer(operatorContainer);
		
		algorithm = new OSPFNSGAII(configuration);
		
	}
	
	
	/**
	 * 
	 * @return the reproduction container
	 * @throws Exception
	 */
	private ReproductionOperatorContainer<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>> createReproductionOperators() 
			throws Exception
	{
		ReproductionOperatorContainer<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>> operatorContainer = new ReproductionOperatorContainer<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>>();
		operatorContainer.addOperator(0.25, new TwoPointCrossOver<Integer>());
		operatorContainer.addOperator(0.25, new UniformCrossover<Integer>());
		operatorContainer.addOperator(0.25,
				new LinearGenomeRandomMutation<Integer>(3));
		operatorContainer.addOperator(0.25, new IntegerAddMutation(3));
		return operatorContainer;					
	}

	/**
	 * Runs the optimization
	 * @throws Exception
	 */
	public void run() throws Exception {
		results = algorithm.run();
		statistics = results.getAlgorithmStatistics();
		statistics.getSolutionContainer().getNumberOfSolutions();
	}
	
	
	/**
	 *  Cancels the optimization
	 */
	public void cancel(){
		try {
			results = algorithm.cancel();
			statistics = results.getAlgorithmStatistics();
			statistics.getSolutionContainer().getNumberOfSolutions();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Defines a solution evaluation plotter
	 * 
	 * @param plotter
	 */
	public void setPlotter(IPlotter<ILinearRepresentation<Integer>> plotter) {
		this.archive.setPlotter(plotter);
	}

	/**
	 * Configures the SPEA2 archive
	 * @param params
	 */
	public void configureDefaultArchive(Params params) {
		archive = new ArchiveManager<Integer, ILinearRepresentation<Integer>>(
				this.getAlgorithm(),
				InsertionStrategy.ADD_ON_SOLUTIONSET_EVALUATION_FUNCTION_EVENT,
				InsertionStrategy.ADD_ALL,
				ProcessingStrategy.PROCESS_ARCHIVE_ON_ITERATION_INCREMENT);

	
		ITrimmingFunction<ILinearRepresentation<Integer>> trimmer = new ZitzlerTruncation<ILinearRepresentation<Integer>>(
				params.getArchiveSize(), getAlgorithm().getConfiguration()
						.getEvaluationFunction());
		archive.addTrimmingFunction(trimmer);
	}
	
	
	public AlgorithmInterface<Integer> getAlgorithm() {
		return algorithm;
	}




	public IEvaluationFunction<ILinearRepresentation<Integer>> getEvaluationFunction() {
		return this.getAlgorithm().getConfiguration().getEvaluationFunction();
	}


	/**
	 * 
	 * @return the best solution
	 */
	public double[] getBestSolutionReal() {
		
		IntegerSolution s= getPolulation().getLowestValuedSolutions(0,1).get(0);
		double[] sol = new double[s.getNumberOfVariables()];
		for (int i = 0; i < s.getNumberOfVariables(); i++){
			sol[i] = ((double)s.getVariableValue(i))/this.divider;
		}
		return sol;
	}

	
	
	public Population getPolulation(){
		return new NondominatedPopulation(this.getSolutionSet());
	}

	/**
	 * 
	 * @return the solution archive
	 */
	public ASolutionSet<Integer> getArchive() {
		return SolutionParser.convert(archive.getArchive());
	}


	/**
	 * 
	 * @return the best p Values solution
	 */
	public double[] getBestSolutionPvalues() {
		ISolution<ILinearRepresentation<Integer>> s= results.getSolutionContainer().getBestSolutionCellContainer(true).getSolution();
		ILinearRepresentation<Integer> rep=s.getRepresentation();
		double[] sol = new double[topology.getDimension()];
		for (int i = 0; i<topology.getDimension(); i++){
			sol[i] = ((double)rep.getElementAt(i))/this.divider;
		}
		return sol;
	}


	/**
	 * 
	 * @return the weights configuration with best evaluation 
	 */
	public int[] getBestSolutionWeights() {
		ISolution<ILinearRepresentation<Integer>> s= results.getSolutionContainer().getBestSolutionCellContainer(true).getSolution();
		ILinearRepresentation<Integer> rep=s.getRepresentation();
		int n=topology.getDimension();
		int m =topology.getNumberEdges();
		int t= m+n;
		int[] res = new int[m];
		for(int i=n; i < t; i++)
		{
			res[i-n] = rep.getElementAt(i);
		}
		return res;
	}



	public AbstractSolutionSet<Integer> getSolutionSet() {
		return algorithm.getSolutionSet();
	}
	
	public void setMinMaxPvalues(int min, int max,int divider){
		this.lowerLimit=min;
		this.upperLimit=max;
		this.divider= divider;
	}


	public void setSecondDemands(Demands d){
		this.demands2=d;
	}
}
