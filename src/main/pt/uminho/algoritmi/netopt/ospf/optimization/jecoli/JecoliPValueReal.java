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
import jecoli.algorithm.components.operator.reproduction.linear.GaussianPerturbationMutation;
import jecoli.algorithm.components.operator.reproduction.linear.LinearGenomeRandomMutation;
import jecoli.algorithm.components.operator.reproduction.linear.RealValueArithmeticalCrossover;
import jecoli.algorithm.components.operator.reproduction.linear.UniformCrossover;
import jecoli.algorithm.components.operator.selection.TournamentSelection;
import jecoli.algorithm.components.operator.selection.TournamentSelection2;
import jecoli.algorithm.components.randomnumbergenerator.DefaultRandomNumberGenerator;
import jecoli.algorithm.components.randomnumbergenerator.IRandomNumberGenerator;
import jecoli.algorithm.components.representation.linear.ILinearRepresentation;
import jecoli.algorithm.components.representation.real.RealValueRepresentationFactory;
import jecoli.algorithm.components.solution.ISolution;
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
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.algorithm.SRPValueEvolutionaryAlgorithm;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.algorithm.SRPValueNSGAII;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.evaluation.pdeft.DEFTPValueEvaluation;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.evaluation.pdeft.DEFTPValueEvaluationMO;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.evaluation.sr.SRPValueEvaluation;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.evaluation.sr.SRPValueEvaluationMO;
import pt.uminho.algoritmi.netopt.ospf.simulation.Demands;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.OSPFWeights;
import pt.uminho.algoritmi.netopt.ospf.simulation.Simul.LoadBalancer;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.ASolutionSet;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.AbstractSolutionSet;

public class JecoliPValueReal {
	
	
	private NetworkTopology topology;
	private Demands demands;
	private OSPFWeights weights;
	private IAlgorithmResult<ILinearRepresentation<Double>> results;
	private IAlgorithmStatistics<ILinearRepresentation<Double>> statistics;
	protected IRandomNumberGenerator randomNumberGenerator;
	protected ArchiveManager<Double, ILinearRepresentation<Double>> archive;
	private AlgorithmInterface<Double> algorithm;
	//private EvolutionaryAlgorithm<ILinearRepresentation<Double>, RealValueRepresentationFactory> algorithm;
		
	private double lowerLimit=0.01;
	private double upperLimit=10.0;

	
	
	
	public JecoliPValueReal(NetworkTopology topology, Demands demands, OSPFWeights weights){
		this.topology=topology;
		this.demands=demands;
		this.weights=weights;
		randomNumberGenerator = new DefaultRandomNumberGenerator();
		lowerLimit=SystemConf.getPropertyDouble("pvalue.min",0.01);
		upperLimit=SystemConf.getPropertyDouble("pvalue.max",10.0);
	}
	
	
	
	public RealValueRepresentationFactory configureSolutionFactory (int numberVariables)
	{	
		return new RealValueRepresentationFactory(numberVariables,this.lowerLimit,this.upperLimit,1);
	}
	
	
	
	
	public void configureSREvolutionaryAlgorithm(Params params) throws Exception,
	InvalidConfigurationException {
	
		SRPValueEvaluation f =new SRPValueEvaluation(this.topology,this.demands, this.weights.asIntArray());
		
		EvolutionaryConfiguration<ILinearRepresentation<Double>,RealValueRepresentationFactory> configuration = new EvolutionaryConfiguration<ILinearRepresentation<Double>,RealValueRepresentationFactory>();
		
		configuration.setEvaluationFunction(f);
		
		RealValueRepresentationFactory solutionFactory = configureSolutionFactory(topology.getDimension()); 
		configuration.setSolutionFactory( solutionFactory );
		
		configuration.setPopulationSize(params.getPopulationSize());
		
		configuration.setRandomNumberGenerator(new DefaultRandomNumberGenerator());
		configuration.setProblemBaseDirectory("nullDirectory");
		configuration.setAlgorithmStateFile("nullFile");
		configuration.setSaveAlgorithmStateDirectoryPath("nullDirectory");
		configuration.setAlgorithmResultWriterList(new ArrayList<IAlgorithmResultWriter<ILinearRepresentation<Double>>>());
		configuration.setStatisticsConfiguration(new StatisticsConfiguration());
		
		ITerminationCriteria terminationCriteria = new IterationTerminationCriteria(params.getNumberGenerations());
		configuration.setTerminationCriteria(terminationCriteria);
	
		RecombinationParameters recombinationParameters = new RecombinationParameters(params.getPopulationSize());
		configuration.setRecombinationParameters(recombinationParameters);
		
		configuration.setSelectionOperators(new TournamentSelection<ILinearRepresentation<Double>>(1,2));
		
		ReproductionOperatorContainer<ILinearRepresentation<Double>,RealValueRepresentationFactory> operatorContainer = 	createReproductionOperators(solutionFactory,true);
		configuration.setReproductionOperatorContainer(operatorContainer);
		
		algorithm = new SRPValueEvolutionaryAlgorithm(configuration);

	}
	

	
	
	public void configurePDEFTEvolutionaryAlgorithm(Params params) throws Exception,
	InvalidConfigurationException {
	
		boolean use_deft=params.getLoadBalancer().equals(LoadBalancer.DEFT)?true:false;
		DEFTPValueEvaluation f =new DEFTPValueEvaluation(this.topology,this.demands, this.weights.asIntArray(),use_deft);
		
		EvolutionaryConfiguration<ILinearRepresentation<Double>,RealValueRepresentationFactory> configuration = new EvolutionaryConfiguration<ILinearRepresentation<Double>,RealValueRepresentationFactory>();
		
		configuration.setEvaluationFunction(f);
		
		RealValueRepresentationFactory solutionFactory = configureSolutionFactory(topology.getDimension()); 
		configuration.setSolutionFactory( solutionFactory );
		
		configuration.setPopulationSize(params.getPopulationSize());
		
		configuration.setRandomNumberGenerator(new DefaultRandomNumberGenerator());
		configuration.setProblemBaseDirectory("nullDirectory");
		configuration.setAlgorithmStateFile("nullFile");
		configuration.setSaveAlgorithmStateDirectoryPath("nullDirectory");
		configuration.setAlgorithmResultWriterList(new ArrayList<IAlgorithmResultWriter<ILinearRepresentation<Double>>>());
		configuration.setStatisticsConfiguration(new StatisticsConfiguration());
		
		ITerminationCriteria terminationCriteria = new IterationTerminationCriteria(params.getNumberGenerations());
		configuration.setTerminationCriteria(terminationCriteria);
	
		RecombinationParameters recombinationParameters = new RecombinationParameters(params.getPopulationSize());
		configuration.setRecombinationParameters(recombinationParameters);
		
		configuration.setSelectionOperators(new TournamentSelection<ILinearRepresentation<Double>>(1,2));
		
		ReproductionOperatorContainer<ILinearRepresentation<Double>,RealValueRepresentationFactory> operatorContainer = 	createReproductionOperators(solutionFactory,true);
		configuration.setReproductionOperatorContainer(operatorContainer);
		
		algorithm = new SRPValueEvolutionaryAlgorithm(configuration);

	}

	
	
	
	public void configureSRNSGAIIAlgorithm(Params params) throws Exception,
	InvalidConfigurationException {
		
		SRPValueEvaluationMO function =new SRPValueEvaluationMO(this.topology,this.demands, this.weights.asIntArray());
		NSGAIIConfiguration<ILinearRepresentation<Double>, RealValueRepresentationFactory> configuration = new NSGAIIConfiguration<ILinearRepresentation<Double>, RealValueRepresentationFactory>();
		
		configuration.setEvaluationFunction(function);
		
		RealValueRepresentationFactory solutionFactory =new RealValueRepresentationFactory(topology.getDimension(),this.lowerLimit,this.upperLimit,2); 
		configuration.setSolutionFactory( solutionFactory );
		
		configuration.setPopulationSize(params.getPopulationSize());
		configuration.setNumberOfObjectives(2);
		configuration.setRandomNumberGenerator(new DefaultRandomNumberGenerator());
		configuration.setProblemBaseDirectory("nullDirectory");
		configuration.setAlgorithmStateFile("nullFile");
		configuration.setSaveAlgorithmStateDirectoryPath("nullDirectory");
		configuration.setAlgorithmResultWriterList(new ArrayList<IAlgorithmResultWriter<ILinearRepresentation<Double>>>());
		configuration.setStatisticsConfiguration(new StatisticsConfiguration());
		
		ITerminationCriteria terminationCriteria = new IterationTerminationCriteria(params.getNumberGenerations());
		configuration.setTerminationCriteria(terminationCriteria);
	
		RecombinationParameters recombinationParameters = new RecombinationParameters(params.getPopulationSize());
		configuration.setRecombinationParameters(recombinationParameters);
		
		configuration.setSelectionOperator(new TournamentSelection2<ILinearRepresentation<Double>>(
				1, 2, randomNumberGenerator));
		
		configuration.getStatisticConfiguration()
		.setNumberOfBestSolutionsToKeepPerRun(
				params.getPopulationSize());
		
		ReproductionOperatorContainer operatorContainer = 	createReproductionOperators(solutionFactory,true);
		configuration.setReproductionOperatorContainer(operatorContainer);
		
		algorithm = new SRPValueNSGAII(configuration);		
	}
	
	
	
	public void configurePDEFTNSGAIIAlgorithm(Params params) throws Exception,
	InvalidConfigurationException {
		
		boolean use_deft=params.getLoadBalancer().equals(LoadBalancer.DEFT)?true:false;
		DEFTPValueEvaluationMO f =new DEFTPValueEvaluationMO(this.topology,this.demands, this.weights.asIntArray(),use_deft);
		NSGAIIConfiguration<ILinearRepresentation<Double>, RealValueRepresentationFactory> configuration = new NSGAIIConfiguration<ILinearRepresentation<Double>, RealValueRepresentationFactory>();
		
		configuration.setEvaluationFunction(f);
		
		RealValueRepresentationFactory solutionFactory =new RealValueRepresentationFactory(topology.getDimension(),this.lowerLimit,this.upperLimit,2); 
		configuration.setSolutionFactory( solutionFactory );
		
		configuration.setPopulationSize(params.getPopulationSize());
		configuration.setNumberOfObjectives(2);
		configuration.setRandomNumberGenerator(new DefaultRandomNumberGenerator());
		configuration.setProblemBaseDirectory("nullDirectory");
		configuration.setAlgorithmStateFile("nullFile");
		configuration.setSaveAlgorithmStateDirectoryPath("nullDirectory");
		configuration.setAlgorithmResultWriterList(new ArrayList<IAlgorithmResultWriter<ILinearRepresentation<Double>>>());
		configuration.setStatisticsConfiguration(new StatisticsConfiguration());
		
		ITerminationCriteria terminationCriteria = new IterationTerminationCriteria(params.getNumberGenerations());
		configuration.setTerminationCriteria(terminationCriteria);
	
		RecombinationParameters recombinationParameters = new RecombinationParameters(params.getPopulationSize());
		configuration.setRecombinationParameters(recombinationParameters);
		
		configuration.
		setSelectionOperator(new TournamentSelection2<ILinearRepresentation<Double>>(
				1, 2, randomNumberGenerator));
		
		configuration.getStatisticConfiguration()
		.setNumberOfBestSolutionsToKeepPerRun(
				params.getPopulationSize());
		
		ReproductionOperatorContainer operatorContainer = 	createReproductionOperators(solutionFactory,true);
		configuration.setReproductionOperatorContainer(operatorContainer);
		
		algorithm = new SRPValueNSGAII(configuration);		
	}
	
	
	
	public ReproductionOperatorContainer createReproductionOperators(RealValueRepresentationFactory factory, boolean useCrossover) 
			throws Exception
			{
				double probEachOperator = 0.0;
				if (useCrossover) probEachOperator = 0.25;
				else probEachOperator = 0.5;
				
				ReproductionOperatorContainer operatorContainer = new ReproductionOperatorContainer();
				operatorContainer.addOperator(probEachOperator, new GaussianPerturbationMutation(0.05));
				operatorContainer.addOperator(probEachOperator,	new LinearGenomeRandomMutation<Double>(0.05));
				if (useCrossover)
				{
					operatorContainer.addOperator(probEachOperator, new UniformCrossover<Double>());
					operatorContainer.addOperator(probEachOperator, new RealValueArithmeticalCrossover());
				}
					
				
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

	
	public void setPlotter(IPlotter<ILinearRepresentation<Double>> plotter) {
		this.archive.setPlotter(plotter);
	}

	
	public void configureDefaultArchive(Params params) {
		archive = new ArchiveManager<Double, ILinearRepresentation<Double>>(
				this.getAlgorithm(),
				InsertionStrategy.ADD_ON_SOLUTIONSET_EVALUATION_FUNCTION_EVENT,
				InsertionStrategy.ADD_ALL,
				ProcessingStrategy.PROCESS_ARCHIVE_ON_ITERATION_INCREMENT);

	
		ITrimmingFunction<ILinearRepresentation<Double>> trimmer = new ZitzlerTruncation<ILinearRepresentation<Double>>(
				params.getArchiveSize(), getAlgorithm().getConfiguration()
						.getEvaluationFunction());
		archive.addTrimmingFunction(trimmer);
	}
	
	
	public AlgorithmInterface<Double> getAlgorithm() {
		return algorithm;
	}




	public IEvaluationFunction<ILinearRepresentation<Double>> getEvaluationFunction() {
		return this.getAlgorithm().getConfiguration().getEvaluationFunction();
	}

	
	public double[] getBestSolution() {
		
		
		//ISolutionContainer<ILinearRepresentation<Double>> c = results.getSolutionContainer();
		
		//ILinearRepresentation<Double> rep = 	c.getBestSolutionCellContainer(false).getSolution().getRepresentation();
		//ILinearRepresentation<Double> rep=results.getAlgorithmStatistics().getSolutionContainer().getBestSolutionCellContainer(false).getSolution().getRepresentation();
		
		ISolution<ILinearRepresentation<Double>> s =results.getSolutionContainer().getBestSolutionCellContainer(true).getSolution();
		ILinearRepresentation<Double> rep=s.getRepresentation();
		double[] sol = new double[rep.getNumberOfElements()];

		for (int i = 0; i < rep.getNumberOfElements(); i++)
			sol[i] = rep.getElementAt(i);
		return sol;
	}



	public ASolutionSet<Double> getArchive() {
		return SolutionParser.convertReal(archive.getArchive());
	}



	public AbstractSolutionSet<Double> getSolutionSet() {
		return algorithm.getSolutionSet();
	}


}
