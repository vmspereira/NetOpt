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
import java.util.Iterator;
import java.util.List;

import jecoli.algorithm.components.algorithm.IAlgorithmResult;
import jecoli.algorithm.components.algorithm.IAlgorithmStatistics;
import jecoli.algorithm.components.algorithm.writer.IAlgorithmResultWriter;
import jecoli.algorithm.components.configuration.InvalidConfigurationException;
import jecoli.algorithm.components.evaluationfunction.IEvaluationFunction;
import jecoli.algorithm.components.operator.IReproductionOperator;
import jecoli.algorithm.components.operator.container.IOperatorContainer;
import jecoli.algorithm.components.operator.container.ReproductionOperatorContainer;
import jecoli.algorithm.components.operator.reproduction.linear.IntegerAddMutation;
import jecoli.algorithm.components.operator.reproduction.linear.LinearGenomeRandomMutation;
import jecoli.algorithm.components.operator.reproduction.linear.TwoPointCrossOver;
import jecoli.algorithm.components.operator.reproduction.linear.UniformCrossover;
import jecoli.algorithm.components.operator.selection.EnvironmentalSelection;
import jecoli.algorithm.components.operator.selection.TournamentSelection;
import jecoli.algorithm.components.operator.selection.TournamentSelection2;
import jecoli.algorithm.components.randomnumbergenerator.DefaultRandomNumberGenerator;
import jecoli.algorithm.components.randomnumbergenerator.IRandomNumberGenerator;
import jecoli.algorithm.components.representation.integer.IntegerRepresentationFactory;
import jecoli.algorithm.components.representation.linear.ILinearRepresentation;
import jecoli.algorithm.components.representation.linear.ILinearRepresentationFactory;
import jecoli.algorithm.components.representation.linear.LinearRepresentation;
import jecoli.algorithm.components.solution.ISolution;
import jecoli.algorithm.components.solution.ISolutionContainer;
import jecoli.algorithm.components.solution.ISolutionFactory;
import jecoli.algorithm.components.solution.ISolutionSet;
import jecoli.algorithm.components.statistics.StatisticsConfiguration;
import jecoli.algorithm.components.terminationcriteria.FitnessTargetTerminationCriteria;
import jecoli.algorithm.components.terminationcriteria.ITerminationCriteria;
import jecoli.algorithm.components.terminationcriteria.IterationTerminationCriteria;
import jecoli.algorithm.multiobjective.archive.components.ArchiveManager;
import jecoli.algorithm.multiobjective.archive.components.InsertionStrategy;
import jecoli.algorithm.multiobjective.archive.components.ProcessingStrategy;
import jecoli.algorithm.multiobjective.archive.plotting.IPlotter;
import jecoli.algorithm.multiobjective.archive.trimming.ITrimmingFunction;
import jecoli.algorithm.multiobjective.archive.trimming.ZitzlerTruncation;
import jecoli.algorithm.multiobjective.nsgaII.NSGAIIConfiguration;
import jecoli.algorithm.multiobjective.spea2.SPEA2Configuration;
import jecoli.algorithm.singleobjective.evolutionary.EvolutionaryConfiguration;
import jecoli.algorithm.singleobjective.evolutionary.RecombinationParameters;
import pt.uminho.algoritmi.netopt.SystemConf;
import pt.uminho.algoritmi.netopt.ospf.graph.CapWGraph;
import pt.uminho.algoritmi.netopt.ospf.graph.Graph;
import pt.uminho.algoritmi.netopt.ospf.optimization.Params;
import pt.uminho.algoritmi.netopt.ospf.optimization.Params.AlgorithmSecondObjective;
import pt.uminho.algoritmi.netopt.ospf.optimization.Params.EdgeSelectionOption;
import pt.uminho.algoritmi.netopt.ospf.optimization.Params.TerminationCriteria;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.algorithm.AlgorithmInterface;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.algorithm.OSPFEvolutionaryAlgorithm;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.algorithm.OSPFNSGAII;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.algorithm.OSPFSPEA2;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.algorithm.representation.permutation.HybridPermutationCrossover;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.algorithm.representation.permutation.PermutationInversionMutation;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.algorithm.representation.permutation.PermutationNonAdjacentSwapMutation;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.algorithm.representation.tuple.TupleRepresentationFactory;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.evaluation.EvaluationType;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.evaluation.ospf.HybridEvaluationMO;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.evaluation.ospf.OSPFAllLinkFailureEvaluation;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.evaluation.ospf.OSPFAllLinkFailureEvaluationMO;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.evaluation.ospf.OSPFIntegerEvaluation;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.evaluation.ospf.OSPFIntegerEvaluationMO;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.evaluation.ospf.OSPFLinkFailureIntegerEvaluation;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.evaluation.ospf.OSPFLinkFailureIntegerEvaluationMO;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.evaluation.ospf.OSPFMultiLayerIntegerEvaluation;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.evaluation.sr.ConstrainedSRMultiLayerEvaluation;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.evaluation.sr.SRIntegerEvaluation;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.evaluation.sr.SRIntegerEvaluationMO;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.evaluation.sr.SRIntegerEvaluationMOLP;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.evaluation.sr.SRLinkFailureEvaluation;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.evaluation.sr.SRMultiLayerEvaluation;
import pt.uminho.algoritmi.netopt.ospf.simulation.DelayRequests;
import pt.uminho.algoritmi.netopt.ospf.simulation.Demands;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.OSPFWeights;
import pt.uminho.algoritmi.netopt.ospf.simulation.Population;
import pt.uminho.algoritmi.netopt.ospf.simulation.Simul.LoadBalancer;
import pt.uminho.algoritmi.netopt.ospf.simulation.exception.DimensionErrorException;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.AbstractSolutionSet;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.IntegerSolution;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.ASolution;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.ASolutionSet;

public class JecoliOSPF {

	private NetworkTopology topology;
	private Demands[] demands;
	private DelayRequests delays;

	private AlgorithmInterface<Integer> algorithm;
	private IAlgorithmResult<ILinearRepresentation<Integer>> results;
	private IAlgorithmStatistics<ILinearRepresentation<Integer>> statistics;

	private String info;
	protected IRandomNumberGenerator randomNumberGenerator;
	protected ArchiveManager<Integer, ILinearRepresentation<Integer>> archive;
	private int MINWeight = 1;
	private int MAXWeight = 20;
	private int NUMObjectives = 2;

	public JecoliOSPF(NetworkTopology topology, Demands[] demands, DelayRequests delays) {
		this.topology = topology.copy();
		this.demands = demands;
		this.delays = delays;
		this.algorithm = null;
		this.results = null;
		this.statistics = null;
		randomNumberGenerator = new DefaultRandomNumberGenerator();
		MAXWeight = SystemConf.getPropertyInt("ospf.maxweight", 20);
		MINWeight = SystemConf.getPropertyInt("ospf.minweight", 1);
	}
	
	

	public void setDemands(Demands[] d) {
		this.demands=d;
	}

	/**
	 * Runs the optimization
	 * 
	 * @throws Exception
	 */
	public void run() throws Exception {
		results = algorithm.run();
		statistics = results.getAlgorithmStatistics();
		statistics.getSolutionContainer().getNumberOfSolutions();
	}

	/**
	 * Cancels the optimization
	 */
	public void cancel() {
		try {
			results = algorithm.cancel();
			statistics = results.getAlgorithmStatistics();
			statistics.getSolutionContainer().getNumberOfSolutions();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @return
	 * @throws Exception
	 * 
	 *             builds the reproduction operators container for EA and MOEA
	 */
	public ReproductionOperatorContainer<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>> getContainer()
			throws Exception {

		double tpco=SystemConf.getPropertyDouble("ea.twoPointCrossover", 0.25);
		double uco=SystemConf.getPropertyDouble("ea.uniformCrossover", 0.25);
		double rm=SystemConf.getPropertyDouble("ea.randomMutation", 0.25);
		double im=SystemConf.getPropertyDouble("ea.incrementalMutation", 0.25);
		if(tpco+uco+rm+im!=1 || tpco<0 || uco<0 || rm<0 || im<0){
			tpco=0.25;uco=0.25;rm=0.25;im=0.25;
		}
			
		
		ReproductionOperatorContainer<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>> operatorContainer = new ReproductionOperatorContainer<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>>();
		operatorContainer.addOperator(tpco, new TwoPointCrossOver<Integer>());
		operatorContainer.addOperator(uco, new UniformCrossover<Integer>());
		operatorContainer.addOperator(rm, new LinearGenomeRandomMutation<Integer>(3));
		operatorContainer.addOperator(im, new IntegerAddMutation(3));
		return operatorContainer;
	}

	/**
	 * 
	 * Builds the initial population to be used by SOEA/MOEA. The number of
	 * objectives is defined in the <code>ISolutionFactory</code>
	 * 
	 * @param params
	 * @param solutionFactory
	 * @return ISolutionSet
	 * @throws DimensionErrorException
	 * 
	 * 
	 */
	private ISolutionSet<ILinearRepresentation<Integer>> buildInitialPopulation(Params params,
			ISolutionFactory<ILinearRepresentation<Integer>> solutionFactory) throws DimensionErrorException {

		// solution set
		ISolutionSet<ILinearRepresentation<Integer>> newSolutionSet;
		int numberOfObjective = solutionFactory.getNumberOfObjectives();

		int populationSize = params.getPopulationSize();
		int fromOldPopulationSize = 0;

		// number of solution obtained from a given previous solution
		if (params.hasInitialPopulation()) {

			double percentage = params.getInitialPopulationPercentage() / 100;
			if (percentage < 1.0) {

				fromOldPopulationSize = Math.min((int) (percentage * params.getPopulationSize()),
						params.getInitialPopulation().getNumberOfSolutions());
			} else {
				fromOldPopulationSize = params.getInitialPopulation().getNumberOfSolutions();
			}
		}

		// build solution set
		int q;
		int p;
		if (fromOldPopulationSize > populationSize) {
			q = 0;
			p = populationSize;
		} else {
			q = populationSize - fromOldPopulationSize;
			p = fromOldPopulationSize;
		}

		newSolutionSet = solutionFactory.generateSolutionSet(q, new DefaultRandomNumberGenerator());

		// Selection could be random or consider some kind of sorting
		// for now just select the p first individuals

		// TODO: select distinct individuals
		if (p > 0) {
			Population clonedPop = params.getInitialPopulation().copy(numberOfObjective);
			List<IntegerSolution> l = clonedPop.getLowestValuedSolutions(p);
			Iterator<IntegerSolution> it = l.iterator();
			while (it.hasNext())
				newSolutionSet.add(SolutionParser.convert(it.next(), 2));
		}

		// Add solutions from standard weight configurations schemes
		// invcap
		if (params.getUseInvCap()) {
			OSPFWeights w = new OSPFWeights(this.topology.getDimension());
			w.setInvCapWeights(MINWeight, MAXWeight, this.topology);
			ISolution<ILinearRepresentation<Integer>> s = SolutionParser
					.convert(w.toSolution(topology, numberOfObjective), numberOfObjective);
			newSolutionSet.add(s);
		}
		// l2
		if (params.getUseL2()) {
			OSPFWeights w = new OSPFWeights(this.topology.getDimension());
			w.setL2Weights(MINWeight, MAXWeight, topology);
			ISolution<ILinearRepresentation<Integer>> s = SolutionParser
					.convert(w.toSolution(topology, numberOfObjective), numberOfObjective);
			newSolutionSet.add(s);
		}
		// unit
		if (params.getUseUnit()) {
			OSPFWeights w = new OSPFWeights(this.topology.getDimension());
			w.setUnitWeights(topology);
			ISolution<ILinearRepresentation<Integer>> s = SolutionParser
					.convert(w.toSolution(topology, numberOfObjective), numberOfObjective);
			newSolutionSet.add(s);
		}

		return newSolutionSet;
	}

	/**
	 * 
	 * Pre configuration for EA
	 * 
	 * @param params
	 * @return
	 * @throws Exception
	 */

	private EvolutionaryConfiguration<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>> buildPreConfigurationEA(
			Params params) throws Exception {

		ILinearRepresentationFactory<Integer> solutionFactory = new IntegerRepresentationFactory(
				topology.getNumberEdges(), MAXWeight, MINWeight);

		ITerminationCriteria terminationCriteria;
		if (params.getCriteria().equals(TerminationCriteria.ITERATION))
			terminationCriteria = new IterationTerminationCriteria(params.getNumberGenerations());
		else
			terminationCriteria = new FitnessTargetTerminationCriteria(params.getCriteriaValue());

		ReproductionOperatorContainer<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>> reproductionOperatorContainer = getContainer();

		EvolutionaryConfiguration<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>> configuration = new EvolutionaryConfiguration<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>>();

		configuration.setSolutionFactory(solutionFactory);
		configuration.setTerminationCriteria(terminationCriteria);

		ISolutionSet<ILinearRepresentation<Integer>> newSolutions = buildInitialPopulation(params, solutionFactory);
		configuration.setInitialPopulation(newSolutions);
		configuration.setPopulationInitialization(false);

		RecombinationParameters recombinationParameters = new RecombinationParameters(
				newSolutions.getNumberOfSolutions());

		configuration.setRecombinationParameters(recombinationParameters);
		configuration.setSelectionOperator(new TournamentSelection<ILinearRepresentation<Integer>>(1, 2));
		configuration.setSurvivorSelectionOperator(new TournamentSelection<ILinearRepresentation<Integer>>(1, 2));
		configuration.setPopulationSize(newSolutions.getNumberOfSolutions());
		configuration.setReproductionOperatorContainer(reproductionOperatorContainer);
		configuration.setRandomNumberGenerator(randomNumberGenerator);
		configuration.setProblemBaseDirectory("nullDirectory");
		configuration.setAlgorithmStateFile("nullFile");
		configuration.setSaveAlgorithmStateDirectoryPath("nullDirectory");
		configuration
				.setAlgorithmResultWriterList(new ArrayList<IAlgorithmResultWriter<ILinearRepresentation<Integer>>>());
		configuration.setStatisticsConfiguration(new StatisticsConfiguration());

		return configuration;
	}

	/**
	 * Demands & delay optimization
	 * 
	 * @param params
	 * @throws Exception
	 * @throws InvalidConfigurationException
	 * 
	 * 
	 */

	public void configureEvolutionaryAlgorithm(Params params) throws Exception, InvalidConfigurationException {

		EvaluationType type = EvaluationType.DEMANDS_DELAY;
		if (params.getSecondObjective() == AlgorithmSecondObjective.DEMANDS) {
			type = EvaluationType.TWO_DEMANDS;
		} else if (params.getSecondObjective() == AlgorithmSecondObjective.MLU) {
			type = EvaluationType.DEMANDS_MLU;
		}else if (params.getSecondObjective() == AlgorithmSecondObjective.ALU) {
			type = EvaluationType.DEMANDS_ALU;
		}

		OSPFIntegerEvaluation f = new OSPFIntegerEvaluation(topology, demands);
		f.setAlpha(params.getAlfa());
		f.setDelays(delays);
		f.setType(type);
		f.setLoadBalancer(params.getLoadBalancer());

		EvolutionaryConfiguration<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>> configuration = buildPreConfigurationEA(
				params);
		configuration.setEvaluationFunction(f);

		this.info = params.toString();
		this.algorithm = new OSPFEvolutionaryAlgorithm(configuration);
	}

	/**
	 * 
	 * Link failure optimization
	 * 
	 * @param params
	 * @throws Exception
	 * @throws InvalidConfigurationException
	 */

	public void configureLinkFailureAlgorithm(Params params) throws Exception, InvalidConfigurationException {
		IEvaluationFunction<ILinearRepresentation<Integer>> evaluationFunction;
		if(params.getEdgeSelectionOption().equals(EdgeSelectionOption.ALLEDGES))
			evaluationFunction= new OSPFAllLinkFailureEvaluation(topology,demands[0],params.getLoadBalancer(),params.getAlfa());
		else
			evaluationFunction = new OSPFLinkFailureIntegerEvaluation(params.getAlfa(), params.getBeta(), topology, demands,
				delays, params.getEdgeSelectionOption(), params.getEdgeFailureId(), false);

		EvolutionaryConfiguration<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>> configuration = buildPreConfigurationEA(
				params);
		configuration.setEvaluationFunction(evaluationFunction);

		this.info = params.toString();
		this.algorithm = new OSPFEvolutionaryAlgorithm(configuration);
	}

	/**
	 * Pre-configuration for NSGAII
	 * 
	 * @param params
	 * @return
	 * @throws Exception
	 * @throws InvalidConfigurationException
	 */
	public NSGAIIConfiguration<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>> preConfigureNSGAII(
			Params params) throws Exception, InvalidConfigurationException {

		NSGAIIConfiguration<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>> configuration = new NSGAIIConfiguration<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>>();
		configuration.setStatisticsConfiguration(new StatisticsConfiguration());
		configuration.setRandomNumberGenerator(randomNumberGenerator);
		IntegerRepresentationFactory solutionFactory = new IntegerRepresentationFactory(topology.getNumberEdges(),
				MAXWeight, MINWeight, NUMObjectives);
		configuration.setSolutionFactory(solutionFactory);
		configuration.setNumberOfObjectives(NUMObjectives);

		configuration.setPopulationSize(params.getPopulationSize());

		configuration.getStatisticConfiguration().setNumberOfBestSolutionsToKeepPerRun(params.getPopulationSize());

		ISolutionSet<ILinearRepresentation<Integer>> newSolutions = buildInitialPopulation(params, solutionFactory);
		configuration.setInitialPopulation(newSolutions);
		configuration.setPopulationInitialization(false);

		ITerminationCriteria terminationCriteria = new IterationTerminationCriteria(params.getNumberGenerations());
		configuration.setTerminationCriteria(terminationCriteria);

		RecombinationParameters recombinationParameters = new RecombinationParameters(0, params.getPopulationSize(), 0,
				true);
		configuration.setRecombinationParameters(recombinationParameters);

		configuration.setSelectionOperator(
				new TournamentSelection2<ILinearRepresentation<Integer>>(1, 2, randomNumberGenerator));
		configuration.setReproductionOperatorContainer(this.getContainer());
		this.info = params.toString();
		return configuration;

	}

	/**
	 * Preconfiguration for SPEA2
	 * 
	 * @param params
	 * @return
	 * @throws Exception
	 * @throws InvalidConfigurationException
	 */
	@SuppressWarnings("unchecked")
	public SPEA2Configuration<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>> preConfigurationSPEA2(
			Params params) throws Exception, InvalidConfigurationException {

		SPEA2Configuration<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>> configuration = new SPEA2Configuration<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>>();

		configuration.setStatisticsConfiguration(new StatisticsConfiguration());
		configuration.setRandomNumberGenerator(randomNumberGenerator);
		IntegerRepresentationFactory solutionFactory = new IntegerRepresentationFactory(topology.getNumberEdges(),
				MAXWeight, MINWeight, NUMObjectives);
		configuration.setSolutionFactory(solutionFactory);
		configuration.setNumberOfObjectives(NUMObjectives);

		configuration.setPopulationSize(params.getPopulationSize());
		configuration.setMaximumArchiveSize(params.getArchiveSize());

		ISolutionSet<ILinearRepresentation<Integer>> newSolutions = buildInitialPopulation(params, solutionFactory);

		configuration.setInitialPopulation(newSolutions);
		configuration.setPopulationInitialization(false);

		ITerminationCriteria terminationCriteria = new IterationTerminationCriteria(params.getNumberGenerations());
		configuration.setTerminationCriteria(terminationCriteria);

		RecombinationParameters recombinationParameters = new RecombinationParameters(0, params.getPopulationSize(), 0,
				true);

		configuration.setRecombinationParameters(recombinationParameters);
		configuration.setEnvironmentalSelectionOperator(new EnvironmentalSelection<ILinearRepresentation<Integer>>());
		configuration.setSelectionOperator(new TournamentSelection<ILinearRepresentation<Integer>>(1, 2));
		IOperatorContainer<IReproductionOperator<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>>> reproductionOperatorContainer = this
				.getContainer();
		configuration.setReproductionOperatorContainer(
				(ReproductionOperatorContainer<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>>) reproductionOperatorContainer);

		this.info = params.toString();
		return configuration;
	}

	/**
	 * NSGAII
	 * 
	 * @param params
	 * @throws Exception
	 * @throws InvalidConfigurationException
	 */

	public void configureNSGAII(Params params) throws Exception, InvalidConfigurationException {

		NSGAIIConfiguration<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>> configuration = this
				.preConfigureNSGAII(params);
		EvaluationType type = EvaluationType.DEMANDS_DELAY;
		if (params.getSecondObjective() == AlgorithmSecondObjective.DEMANDS) {
			type = EvaluationType.TWO_DEMANDS;
		} else if (params.getSecondObjective() == AlgorithmSecondObjective.MLU) {
			type = EvaluationType.DEMANDS_MLU;
		}else if (params.getSecondObjective() == AlgorithmSecondObjective.ALU) {
			type = EvaluationType.DEMANDS_ALU;
		}

		OSPFIntegerEvaluationMO ospfEvaluation = new OSPFIntegerEvaluationMO(topology, demands, delays, type);

		ospfEvaluation.setLoadBalancer(params.getLoadBalancer());
		configuration.setEvaluationFunction(ospfEvaluation);

		algorithm = new OSPFNSGAII(configuration);
	}

	/**
	 * SPEA2
	 * 
	 * @param params
	 * @throws Exception
	 * @throws InvalidConfigurationException
	 */
	public void configureSPEA2(Params params) throws Exception, InvalidConfigurationException {

		SPEA2Configuration<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>> configuration = this
				.preConfigurationSPEA2(params);

		EvaluationType type = EvaluationType.DEMANDS_DELAY;
		if (params.getSecondObjective() == AlgorithmSecondObjective.DEMANDS) {
			type = EvaluationType.TWO_DEMANDS;
		} else if (params.getSecondObjective() == AlgorithmSecondObjective.MLU) {
			type = EvaluationType.DEMANDS_MLU;
		}else if (params.getSecondObjective() == AlgorithmSecondObjective.ALU) {
			type = EvaluationType.DEMANDS_ALU;
		}

		OSPFIntegerEvaluationMO ospfEvaluation = new OSPFIntegerEvaluationMO(topology, demands, delays, type);
		ospfEvaluation.setLoadBalancer(params.getLoadBalancer());
		configuration.setEvaluationFunction(ospfEvaluation);

		this.algorithm = new OSPFSPEA2(configuration);
	}

	/**
	 * NSGAII
	 * 
	 * @param params
	 * @throws Exception
	 * @throws InvalidConfigurationException
	 */

	public void configureNSGAIILinkFailure(Params params) throws Exception, InvalidConfigurationException {

		NSGAIIConfiguration<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>> configuration = this
				.preConfigureNSGAII(params);

		IEvaluationFunction<ILinearRepresentation<Integer>> evaluationFunction;
		if(params.getEdgeSelectionOption().equals(EdgeSelectionOption.ALLEDGES))
			 evaluationFunction= new OSPFAllLinkFailureEvaluationMO(topology,demands[0],params.getLoadBalancer());
		else
			evaluationFunction = new OSPFLinkFailureIntegerEvaluationMO(params.getAlfa(), params.getBeta(), topology,
				demands, delays, params.getEdgeSelectionOption(), params.getEdgeFailureId(), false);

		configuration.setEvaluationFunction(evaluationFunction);

		algorithm = new OSPFNSGAII(configuration);
	}

	/**
	 * SPEA2
	 * 
	 * @param params
	 * @throws Exception
	 * @throws InvalidConfigurationException
	 */
	public void configureSPEA2LinkFailure(Params params) throws Exception, InvalidConfigurationException {

		SPEA2Configuration<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>> configuration = this
				.preConfigurationSPEA2(params);

		IEvaluationFunction<ILinearRepresentation<Integer>> evaluationFunction;
		if(params.getEdgeSelectionOption().equals(EdgeSelectionOption.ALLEDGES))
			 evaluationFunction= new OSPFAllLinkFailureEvaluationMO(topology,demands[0],params.getLoadBalancer());
		else
			 evaluationFunction = new OSPFLinkFailureIntegerEvaluationMO(params.getAlfa(), params.getBeta(), topology,
				demands, delays, params.getEdgeSelectionOption(), params.getEdgeFailureId(), false);

		configuration.setEvaluationFunction(evaluationFunction);

		this.algorithm = new OSPFSPEA2(configuration);
	}

	/**
	 * MultiTopology optimization
	 * 
	 * @param params
	 * @throws Exception
	 * @throws InvalidConfigurationException
	 */

	public void configureMultiLayerAlgorithm(Params params) throws Exception, InvalidConfigurationException {
		configureMultiLayerAlgorithm(params, 2);
	}

	/**
	 * MultiTopology optimization
	 * 
	 * @param params
	 * @throws Exception
	 * @throws InvalidConfigurationException
	 */

	public void configureMultiLayerAlgorithm(Params params, int n) throws Exception, InvalidConfigurationException {

		int nLayers = n;

		EvolutionaryConfiguration<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>> configuration = new EvolutionaryConfiguration<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>>();

		IEvaluationFunction<ILinearRepresentation<Integer>> evaluationFunction;
		evaluationFunction = new OSPFMultiLayerIntegerEvaluation(topology, demands, nLayers);

		configuration.setEvaluationFunction(evaluationFunction);

		// number of weights/maximum weight
		ILinearRepresentationFactory<Integer> solutionFactory = new IntegerRepresentationFactory(
				topology.getNumberEdges() * nLayers, MAXWeight, MINWeight);
		configuration.setSolutionFactory(solutionFactory);

		IterationTerminationCriteria terminationCriteria = new IterationTerminationCriteria(
				params.getNumberGenerations());

		configuration.setTerminationCriteria(terminationCriteria);
		RecombinationParameters recombinationParameters = new RecombinationParameters(
				params.getPopulationSize() * nLayers);
		configuration.setRecombinationParameters(recombinationParameters);

		configuration.setRandomNumberGenerator(randomNumberGenerator);
		configuration.setProblemBaseDirectory("nullDirectory");
		configuration.setAlgorithmStateFile("nullFile");
		configuration.setSaveAlgorithmStateDirectoryPath("nullDirectory");

		configuration.setSelectionOperator(new TournamentSelection<ILinearRepresentation<Integer>>(1, 2));

		configuration.setSurvivorSelectionOperator(new TournamentSelection<ILinearRepresentation<Integer>>(1, 2));

		IOperatorContainer<IReproductionOperator<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>>> reproductionOperatorContainer = getContainer();

		configuration.setPopulationSize(params.getPopulationSize() * nLayers);

		configuration.setPopulationInitialization(true);
		configuration.setReproductionOperatorContainer(reproductionOperatorContainer);

		this.info = params.toString();
		this.algorithm = new OSPFEvolutionaryAlgorithm(configuration);
	}

	/**
	 * 
	 * @return the bestSolution
	 */
	public int[] getBestSolutionWeights() {
		ISolutionContainer<ILinearRepresentation<Integer>> c = results.getSolutionContainer();

		LinearRepresentation<Integer> rep = (LinearRepresentation<Integer>) c.getBestSolutionCellContainer(true)
				.getSolution().getRepresentation();
		int[] sol = new int[rep.getNumberOfElements()];

		for (int i = 0; i < rep.getNumberOfElements(); i++)
			sol[i] = rep.getElementAt(i);
		return sol;
	}

	/**
	 * 
	 * @return
	 */
	public int[][] getBestSolutionMatrix() {
		CapWGraph graph = topology.getGraph();

		int[] weights = getBestSolutionWeights();
		int[][] res = new int[topology.getDimension()][topology.getDimension()];

		int w = 0;
		for (int i = 0; i < graph.getDimension(); i++) {

			for (int j = 0; j < graph.getDimension(); j++) {
				if (!graph.getConnection(i, j).equals(Graph.Status.NOCONNECTION)) {
					res[i][j] = weights[w]; // range of the GA is 0 to max-1
					w++;
				} else {
					res[i][j] = 0;
				}
			}
		}
		return res;
	}

	public ASolution<Integer> getBestSolution() {
		ISolution<ILinearRepresentation<Integer>> s = results.getSolutionContainer().getBestSolutionCellContainer(true)
				.getSolution();
		return SolutionParser.convert(s);
	}

	public AbstractSolutionSet<Integer> getSolutionSet() {
		return algorithm.getSolutionSet();
	}

	public AbstractSolutionSet<Integer> getAchiveSolutionSet() {
		return algorithm.getAchiveSolutionSet();
	}

	public String getInfo() {
		return this.info;
	}

	public AlgorithmInterface<Integer> getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(AlgorithmInterface<Integer> algorithm) {
		this.algorithm = algorithm;
	}

	public void configureDefaultArchive(Params params) {
		archive = new ArchiveManager<Integer, ILinearRepresentation<Integer>>(this.getAlgorithm(),
				InsertionStrategy.ADD_ON_SOLUTIONSET_EVALUATION_FUNCTION_EVENT, InsertionStrategy.ADD_ALL,
				ProcessingStrategy.PROCESS_ARCHIVE_ON_ITERATION_INCREMENT);

		ITrimmingFunction<ILinearRepresentation<Integer>> trimmer = new ZitzlerTruncation<ILinearRepresentation<Integer>>(
				params.getArchiveSize(), getAlgorithm().getConfiguration().getEvaluationFunction());
		archive.addTrimmingFunction(trimmer);
	}

	public ASolutionSet<Integer> getArchive() {
		return SolutionParser.convert(archive.getArchive());
	}

	public IEvaluationFunction<ILinearRepresentation<Integer>> getEvaluationFunction() {
		return this.getAlgorithm().getConfiguration().getEvaluationFunction();
	}

	public void setPlotter(IPlotter<ILinearRepresentation<Integer>> plotter) {
		this.archive.setPlotter(plotter);
	}

	/**
	 * SR optimization
	 */

	/**
	 * SOEA
	 * 
	 * @param params
	 * @throws Exception
	 * @throws InvalidConfigurationException
	 */

	public void configureSREvolutionaryAlgorithm(Params params) throws Exception, InvalidConfigurationException {

		IEvaluationFunction<ILinearRepresentation<Integer>> evaluationFunction;

		EvaluationType type = EvaluationType.DEMANDS_DELAY;
		if (params.getSecondObjective() == AlgorithmSecondObjective.DEMANDS) {
			type = EvaluationType.TWO_DEMANDS;
		} else if (params.getSecondObjective() == AlgorithmSecondObjective.MLU) {
			type = EvaluationType.DEMANDS_MLU;
		}else if (params.getSecondObjective() == AlgorithmSecondObjective.ALU) {
			type = EvaluationType.DEMANDS_ALU;
		}

		SRIntegerEvaluation f = new SRIntegerEvaluation(topology, demands);
		f.setAlpha(params.getAlfa());
		f.setDelays(delays);
		f.setType(type);
		if (params.getLoadBalancer() == LoadBalancer.DEFT)
			f.setLoadBalancer(LoadBalancer.DEFT);

		evaluationFunction = f;

		EvolutionaryConfiguration<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>> configuration = buildPreConfigurationEA(
				params);
		configuration.setEvaluationFunction(evaluationFunction);

		this.info = params.toString();
		this.algorithm = new OSPFEvolutionaryAlgorithm(configuration);
	}

	/**
	 * NSGAII
	 * 
	 * @param params
	 * @throws Exception
	 * @throws InvalidConfigurationException
	 */

	public void configureSRNSGAII(Params params) throws Exception, InvalidConfigurationException {

		NSGAIIConfiguration<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>> configuration = this
				.preConfigureNSGAII(params);

		EvaluationType type = EvaluationType.DEMANDS_DELAY;
		if (params.getSecondObjective() == AlgorithmSecondObjective.DEMANDS) {
			type = EvaluationType.TWO_DEMANDS;
		} else if (params.getSecondObjective() == AlgorithmSecondObjective.MLU) {
			type = EvaluationType.DEMANDS_MLU;
		}else if (params.getSecondObjective() == AlgorithmSecondObjective.ALU) {
			type = EvaluationType.DEMANDS_ALU;
		}

		SRIntegerEvaluationMO ospfEvaluation = new SRIntegerEvaluationMO(topology, demands, delays, type);
		// sets load balancing strategy (DEFT or PEFT)
		ospfEvaluation.setLoadBalancer(params.getLoadBalancer());
		configuration.setEvaluationFunction(ospfEvaluation);

		algorithm = new OSPFNSGAII(configuration);
	}
	
	
	
	public void configureHybridSRNSGAIILP(Params params) throws Exception, InvalidConfigurationException {

		NSGAIIConfiguration<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>> configuration = this
				.preConfigureNSGAII(params);
		SRIntegerEvaluationMOLP ospfEvaluation = new SRIntegerEvaluationMOLP(topology, demands);
		configuration.setEvaluationFunction(ospfEvaluation);

		algorithm = new OSPFNSGAII(configuration);
	}

	/**
	 * SPEA2
	 * 
	 * @param params
	 * @throws Exception
	 * @throws InvalidConfigurationException
	 */
	public void configureSRSPEA2(Params params) throws Exception, InvalidConfigurationException {

		SPEA2Configuration<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>> configuration = this
				.preConfigurationSPEA2(params);

		EvaluationType type = EvaluationType.DEMANDS_DELAY;
		if (params.getSecondObjective() == AlgorithmSecondObjective.DEMANDS) {
			type = EvaluationType.TWO_DEMANDS;
		} else if (params.getSecondObjective() == AlgorithmSecondObjective.MLU) {
			type = EvaluationType.DEMANDS_MLU;
		}else if (params.getSecondObjective() == AlgorithmSecondObjective.ALU) {
			type = EvaluationType.DEMANDS_ALU;
		}

		SRIntegerEvaluationMO ospfEvaluation = new SRIntegerEvaluationMO(topology, demands, delays, type);
		ospfEvaluation.setLoadBalancer(params.getLoadBalancer());
		configuration.setEvaluationFunction(ospfEvaluation);

		this.algorithm = new OSPFSPEA2(configuration);
	}

	
	
	
	
	/**
	 * SR Single Link failure Optimization
	 * 
	 * @param params
	 * @throws Exception
	 * @throws InvalidConfigurationException
	 */
	public void configureSRLinkFailureNSGAII(Params params) throws Exception, InvalidConfigurationException {

		NSGAIIConfiguration<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>> configuration = this
				.preConfigureNSGAII(params);
		SRLinkFailureEvaluation ospfEvaluation = new SRLinkFailureEvaluation(topology, demands[0],params.getLoadBalancer(), SRLinkFailureEvaluation.LFObjectives.CONGESTION_CONGESTION);
		configuration.setEvaluationFunction(ospfEvaluation);
		
		
		algorithm = new OSPFNSGAII(configuration);
	}
	
	
	
	public void configureConstrainedSRLMTNSGAII(Params params) throws Exception, InvalidConfigurationException {
		
		
		Demands[] d = new Demands[2];
		
		d[0]=demands[1];
		d[1]=demands[2];
		
		ConstrainedSRMultiLayerEvaluation ospfEvaluation = new ConstrainedSRMultiLayerEvaluation(topology, params.getPreviousWeights(),demands[0] ,d);
		
		if(params.getPValues()!=null)
			ospfEvaluation.setPValues(params.getPValues().getPValues());
		
		
		NSGAIIConfiguration<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>> configuration = new NSGAIIConfiguration<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>>();
		configuration.setStatisticsConfiguration(new StatisticsConfiguration());
		configuration.setRandomNumberGenerator(randomNumberGenerator);		
		IntegerRepresentationFactory solutionFactory = new IntegerRepresentationFactory(topology.getNumberEdges()*2,
				MAXWeight, MINWeight, 2);
		configuration.setSolutionFactory(solutionFactory);
		configuration.setNumberOfObjectives(2);

		configuration.setPopulationSize(params.getPopulationSize());
		configuration.getStatisticConfiguration().setNumberOfBestSolutionsToKeepPerRun(params.getPopulationSize());
		
		if(params.getInitialPopulationPercentage()>0 && params.getInitialPopulation()!=null){
			ISolutionSet<ILinearRepresentation<Integer>> newSolutions = buildInitialPopulation(params, solutionFactory);
			configuration.setInitialPopulation(newSolutions);
			configuration.setPopulationInitialization(false);	
		}
		else
		  configuration.setPopulationInitialization(true);

		ITerminationCriteria terminationCriteria = new IterationTerminationCriteria(params.getNumberGenerations());
		configuration.setTerminationCriteria(terminationCriteria);

		RecombinationParameters recombinationParameters = new RecombinationParameters(0, params.getPopulationSize(), 0,
				true);
		configuration.setRecombinationParameters(recombinationParameters);

		configuration.setSelectionOperator(
				new TournamentSelection2<ILinearRepresentation<Integer>>(1, 2, randomNumberGenerator));
		configuration.setReproductionOperatorContainer(this.getContainer());
		configuration.setEvaluationFunction(ospfEvaluation);
		this.info = params.toString();
		algorithm = new OSPFNSGAII(configuration);

		 
	}
	
	
	public void configureSRLMTNSGAII(Params params) throws Exception, InvalidConfigurationException {

		int nlayers;
		SRMultiLayerEvaluation ospfEvaluation;
		if(params.getPreviousWeights()==null){
			ospfEvaluation= new SRMultiLayerEvaluation(topology, demands,params.getLoadBalancer());
			nlayers=demands.length;
		}
		else{
			Demands[] d = new Demands[demands.length-1];
			for(int i=1;i<demands.length;i++)
				d[i-1]=demands[i];
			ospfEvaluation = new SRMultiLayerEvaluation(topology, params.getPreviousWeights(),demands[0] ,d,params.getLoadBalancer());
			nlayers=demands.length-1;
		}
		if(params.getPValues()!=null)
			ospfEvaluation.setPValues(params.getPValues().getPValues());
		
		
		NSGAIIConfiguration<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>> configuration = new NSGAIIConfiguration<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>>();
		configuration.setStatisticsConfiguration(new StatisticsConfiguration());
		configuration.setRandomNumberGenerator(randomNumberGenerator);		
		IntegerRepresentationFactory solutionFactory = new IntegerRepresentationFactory(topology.getNumberEdges()*nlayers,
				MAXWeight, MINWeight, 2);
		configuration.setSolutionFactory(solutionFactory);
		configuration.setNumberOfObjectives(2);

		configuration.setPopulationSize(params.getPopulationSize());
		configuration.getStatisticConfiguration().setNumberOfBestSolutionsToKeepPerRun(params.getPopulationSize());
		
		if(params.getInitialPopulationPercentage()>0 && params.getInitialPopulation()!=null){
			ISolutionSet<ILinearRepresentation<Integer>> newSolutions = buildInitialPopulation(params, solutionFactory);
			configuration.setInitialPopulation(newSolutions);
			configuration.setPopulationInitialization(false);	
		}
		else
		  configuration.setPopulationInitialization(true);

		ITerminationCriteria terminationCriteria = new IterationTerminationCriteria(params.getNumberGenerations());
		configuration.setTerminationCriteria(terminationCriteria);

		RecombinationParameters recombinationParameters = new RecombinationParameters(0, params.getPopulationSize(), 0,
				true);
		configuration.setRecombinationParameters(recombinationParameters);

		configuration.setSelectionOperator(
				new TournamentSelection2<ILinearRepresentation<Integer>>(1, 2, randomNumberGenerator));
		configuration.setReproductionOperatorContainer(this.getContainer());
		configuration.setEvaluationFunction(ospfEvaluation);
		this.info = params.toString();
		algorithm = new OSPFNSGAII(configuration);
	}
	
	
	
	
	
	
	/* 
	 *  Hybrid IP/SDN  
	 */

	
	public void configureHybridNSGAII(Params params) throws Exception{
		
		NSGAIIConfiguration<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>> configuration = new NSGAIIConfiguration<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>>();
		configuration.setStatisticsConfiguration(new StatisticsConfiguration());
		configuration.setRandomNumberGenerator(randomNumberGenerator);
		List<Integer> perm = new ArrayList<Integer>(topology.getDimension());
		
		int nedges=topology.getNumberEdges();
		int nnodes=topology.getDimension();
		int solutionSize = nedges+nnodes;
		int k = Math.min(params.getNumberSDNNodes(),topology.getDimension());
		
		for(int i=0;i<topology.getDimension();i++)
			perm.add(i, i<k? 1 : 0);
		
		TupleRepresentationFactory solutionFactory = new TupleRepresentationFactory(nedges,MAXWeight, MINWeight, NUMObjectives,perm);

		configuration.setSolutionFactory(solutionFactory);
		configuration.setNumberOfObjectives(NUMObjectives);
		configuration.setPopulationSize(params.getPopulationSize());
		configuration.getStatisticConfiguration().setNumberOfBestSolutionsToKeepPerRun(params.getPopulationSize());
		
		//generate initial population
		if(params.getInitialPopulation()!=null && params.getInitialPopulationPercentage()>0){
			ISolutionSet<ILinearRepresentation<Integer>> newSolutionSet;
			int populationSize = params.getPopulationSize();
			int fromOldPopulationSize = 0;
			double percentage = params.getInitialPopulationPercentage() / 100;
			if (percentage < 1.0) {
				fromOldPopulationSize = Math.min((int) (percentage * params.getPopulationSize()),
						params.getInitialPopulation().getNumberOfSolutions());
			} else {
				fromOldPopulationSize = params.getInitialPopulation().getNumberOfSolutions();
			}
			int q,p;
			if (fromOldPopulationSize > populationSize) {
				q = 0;
				p = populationSize;
			} else {
				q = populationSize - fromOldPopulationSize;
				p = fromOldPopulationSize;
			}
			
			
			newSolutionSet = solutionFactory.generateSolutionSet(q, new DefaultRandomNumberGenerator());
			for(int i=0;i<p;i++){
					ISolution<ILinearRepresentation<Integer>> solution=solutionFactory.generateSolution(params.getInitialPopulation().getWeights(i));
					newSolutionSet.add(solution);
			}
			configuration.setInitialPopulation(newSolutionSet);
			configuration.setPopulationInitialization(false);
		}
		else
			configuration.setPopulationInitialization(true);

		ITerminationCriteria terminationCriteria = new IterationTerminationCriteria(params.getNumberGenerations());
		configuration.setTerminationCriteria(terminationCriteria);

		RecombinationParameters recombinationParameters = new RecombinationParameters(0, params.getPopulationSize(), 0,
				true);
		configuration.setRecombinationParameters(recombinationParameters);

		configuration.setSelectionOperator(
				new TournamentSelection2<ILinearRepresentation<Integer>>(1, 2, randomNumberGenerator));
		
		
		
		// normal density operators probability
		
		//NormalDistribution dn= new NormalDistribution(nnodes/2,nnodes/4);
		//double p=dn.density(nnodes/2-Math.abs(nnodes/2-n));
		
		// linear 
		
		double p = (-0.5 * Math.abs(k - (nnodes/2))) / (nnodes/2) + 0.5; 
		
		ReproductionOperatorContainer<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>> reproductionOperatorContainer = new ReproductionOperatorContainer<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>>();
		reproductionOperatorContainer.addOperator(0.5-p/4, new TwoPointCrossOver<Integer>(0,nedges));
		reproductionOperatorContainer.addOperator(0.5-p/4, new UniformCrossover<Integer>(0,nedges));
		reproductionOperatorContainer.addOperator(p/2, new HybridPermutationCrossover<Integer>(nedges));		
		
		configuration.setReproductionOperatorContainer(reproductionOperatorContainer);
		
		ReproductionOperatorContainer<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>> mutationOperatorContainer = new ReproductionOperatorContainer<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>>();
		mutationOperatorContainer.addOperator(0.5-p/4, new LinearGenomeRandomMutation<Integer>(3,0,nedges));
		mutationOperatorContainer.addOperator(0.5-p/4, new IntegerAddMutation(3,0,nedges));
		mutationOperatorContainer.addOperator(3*p/8, new PermutationNonAdjacentSwapMutation(nedges,solutionSize));
		mutationOperatorContainer.addOperator(p/8, new PermutationInversionMutation(nedges,solutionSize));
		configuration.setMutationOperatorsContainer(mutationOperatorContainer);
	

		this.info = params.toString();

		EvaluationType type = EvaluationType.DEMANDS_MLU;
		
		HybridEvaluationMO ospfEvaluation = new HybridEvaluationMO(topology, demands,null, type);

		ospfEvaluation.setLoadBalancer(params.getLoadBalancer());
		configuration.setEvaluationFunction(ospfEvaluation);

		algorithm = new OSPFNSGAII(configuration);
	}

	
	
	
	
	
	
}
