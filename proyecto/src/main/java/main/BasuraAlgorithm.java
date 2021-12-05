package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.mochc.MOCHCBuilder;
import org.uma.jmetal.algorithm.singleobjective.geneticalgorithm.GeneticAlgorithmBuilder;
import org.uma.jmetal.example.AlgorithmRunner;
import org.uma.jmetal.experimental.componentbasedalgorithm.catalogue.replacement.Replacement;
import org.uma.jmetal.experimental.componentbasedalgorithm.catalogue.replacement.impl.MuPlusLambdaReplacement;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.HUXCrossover;
import org.uma.jmetal.operator.crossover.impl.SinglePointCrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.BitFlipMutation;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.operator.selection.impl.RandomSelection;
import org.uma.jmetal.operator.selection.impl.RankingAndCrowdingSelection;
import org.uma.jmetal.parallel.asynchronous.algorithm.impl.AsynchronousMultiThreadedGeneticAlgorithm;
import org.uma.jmetal.parallel.synchronous.SparkSolutionListEvaluator;
import org.uma.jmetal.problem.binaryproblem.BinaryProblem;
import org.uma.jmetal.solution.binarysolution.BinarySolution;
import org.uma.jmetal.solution.binarysolution.impl.DefaultBinarySolution;
import org.uma.jmetal.util.comparator.ObjectiveComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.MultiThreadedSolutionListEvaluator;
import org.uma.jmetal.util.observer.impl.PrintObjectivesObserver;
import org.uma.jmetal.util.termination.impl.TerminationByEvaluations;


public class BasuraAlgorithm {
	private String instanceFolder;
	private int [] estadoInicial;
	private int cantidadCamiones=10,
			cores=8,
			populationSize=100,
			maxEvaluations=10000,
			capacidadCamiones=200;
	public float mutationP=0.008f,crossoverP=0.75f;
	
	private BasuraProblem problem;
	
	public BasuraAlgorithm(String instanceFolder, int [] estadoInicial) {
		this.instanceFolder = instanceFolder;
		this.estadoInicial = estadoInicial;
	}

	
	public Itinerario run() {
	    problem = new BasuraProblem(instanceFolder, estadoInicial,cantidadCamiones,capacidadCamiones,cores);
	    
	    CrossoverOperator<BinarySolution> crossover = new SinglePointCrossover(crossoverP);
	    MutationOperator<BinarySolution> mutation = new BitFlipMutation(mutationP);
	    SelectionOperator<List<BinarySolution>, BinarySolution> selection = new BinaryTournamentSelection<BinarySolution>();
	    
	    //SparkConf sparkConf = new SparkConf()
	    //        .setMaster("local[6]") // 8 cores
	    //        .setAppName("NSGA-II with Spark");
	    //System.setProperty("hadoop.home.dir", "C:\\Program Files\\winutils");
	    //JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);
	    //SolutionListEvaluator<BinarySolution> evaluator = new SparkSolutionListEvaluator<>(sparkContext);
	    MultiThreadedSolutionListEvaluator<BinarySolution> evaluator =  new MultiThreadedSolutionListEvaluator<BinarySolution>(cores);
	    
	    GeneticAlgorithmBuilder<BinarySolution> builder =
	            new GeneticAlgorithmBuilder<BinarySolution>(problem, crossover, mutation)
	                .setPopulationSize(populationSize)
	                .setMaxEvaluations(maxEvaluations)
	                .setSelectionOperator(selection)
	                //.setSolutionListEvaluator(new MultiThreadedSolutionListEvaluator<BinarySolution>(16));
	                //.setSolutionListEvaluator(new SparkSolutionListEvaluator<>(sparkContext));
	                .setSolutionListEvaluator(evaluator);

	    Algorithm<BinarySolution> algorithm = builder.build();	    
	    algorithm.run();
	    evaluator.shutdown();
	    
	    //AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();
	    return ((Itinerario) algorithm.getResult().variables().get(0));

	}

	public Itinerario run2() {
		problem = new BasuraProblem(instanceFolder, estadoInicial,cantidadCamiones,capacidadCamiones,cores);
	    CrossoverOperator<BinarySolution> crossoverOperator;
	    MutationOperator<BinarySolution> mutationOperator;
	    SelectionOperator<List<BinarySolution>, BinarySolution> parentsSelection;
	    SelectionOperator<List<BinarySolution>, List<BinarySolution>> newGenerationSelection;
	    Algorithm<List<BinarySolution>> algorithm ;
	    crossoverOperator = new HUXCrossover(1.0) ;
	    parentsSelection = new RandomSelection<BinarySolution>() ;
	    newGenerationSelection = new RankingAndCrowdingSelection<BinarySolution>(100) ;
	    mutationOperator = new BitFlipMutation(0.35) ;
	    algorithm = new MOCHCBuilder(problem)
	            .setInitialConvergenceCount(0.25)
	            .setConvergenceValue(3)
	            .setPreservedPopulation(0.05)
	            .setPopulationSize(populationSize)
	            .setMaxEvaluations(maxEvaluations)
	            .setCrossover(crossoverOperator)
	            .setNewGenerationSelection(newGenerationSelection)
	            .setCataclysmicMutation(mutationOperator)
	            .setParentSelection(parentsSelection)
	            .setEvaluator(new MultiThreadedSolutionListEvaluator<BinarySolution>(cores))
	            .build() ;

	    AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm)
	            .execute() ;
	    
	    List<BinarySolution> population = algorithm.getResult() ;
	    long computingTime = algorithmRunner.getComputingTime() ;
	    System.out.println(Arrays.toString(population.toArray()));
	    Itinerario best = new Itinerario(population.get(0).variables().get(0), cantidadCamiones, estadoInicial.length, 2)
	    		.setComp(computingTime);
	    return best;
	}
	
	public Itinerario run3() {
		problem = new BasuraProblem(instanceFolder, estadoInicial,cantidadCamiones,capacidadCamiones,cores);

	    CrossoverOperator<BinarySolution> crossover = new HUXCrossover(crossoverP);//new SinglePointCrossover(0.8);
	    MutationOperator<BinarySolution> mutation = new BitFlipMutation(mutationP);
	    SelectionOperator<List<BinarySolution>, BinarySolution> selection = new BinaryTournamentSelection<BinarySolution>();
	    Replacement<BinarySolution> replacement = new MuPlusLambdaReplacement<>(new ObjectiveComparator<>(0)) ;
	    
	    long initTime = System.currentTimeMillis();
	    AsynchronousMultiThreadedGeneticAlgorithm<BinarySolution> geneticAlgorithm =
	        new AsynchronousMultiThreadedGeneticAlgorithm<>(
	            cores, problem, populationSize, crossover, mutation, selection, replacement, new TerminationByEvaluations(maxEvaluations));

	    //PrintObjectivesObserver printObjectivesObserver = new PrintObjectivesObserver(100) ;
	    //geneticAlgorithm.getObservable().register(printObjectivesObserver);
	    
	    geneticAlgorithm.run();
	    geneticAlgorithm.updateProgress();
	    
	    long endTime = System.currentTimeMillis();
	    List<BinarySolution> resultList = geneticAlgorithm.getResult();
	    return ((Itinerario) resultList.get(0).variables().get(0)).setComp(endTime-initTime);
	}
	
	public Itinerario runGreedy() {
	    System.out.println("Greedy: ");
	    Greedy g = new Greedy();
	    try {
					g.setDistancia(MatrixLoader.readCSV(instanceFolder+"/distanciaContenedores.csv"))
					.setTiempo(MatrixLoader.readCSV(instanceFolder+"/tiempoContenedores.csv"))
					.setTiempoFromStartpoint(MatrixLoader.readCSV(instanceFolder+"/tiempoDesdeStartpoint.csv")[0])
					.setTiempotoStartpoint(MatrixLoader.readCSV(instanceFolder+"/tiempoHaciaStartpoint.csv")[0])
					.setDistanciaFromStartpoint(MatrixLoader.readCSV(instanceFolder+"/distanciaDesdeStartpoint.csv")[0])
					.setDistanciatoStartpoint(MatrixLoader.readCSV(instanceFolder+"/distanciaHaciaStartpoint.csv")[0])
					.setCantidadCamiones(cantidadCamiones)
					.setBasuraInicialContenedores(estadoInicial)
					.setCAPACIDAD_MAXIMA(capacidadCamiones);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    long initTime = System.currentTimeMillis();
	    Itinerario it = g.solve(-1);
	    BinarySolution b = new DefaultBinarySolution(new ArrayList<>(Arrays.asList(it.getBinarySetLength())),1);
	    b.variables().set(0, it);
	    problem.evaluate(b);
	    long endTime = System.currentTimeMillis();
	    it.setComp(endTime-initTime);
	    return it;
	}
	
	public int getCantidadCamiones() {
		return cantidadCamiones;
	}
	public BasuraAlgorithm setCantidadCamiones(int cantidadCamiones) {
		this.cantidadCamiones = cantidadCamiones;
		return this;
	}
	public BasuraAlgorithm setCores(int cores) {
		this.cores = cores;
		return this;
	}
	
	public int getPopulationSize() {
		return populationSize;
	}

	public BasuraAlgorithm setPopulationSize(int populationSize) {
		this.populationSize = populationSize;
		return this;
	}

	public int getMaxEvaluations() {
		return maxEvaluations;
	}

	public BasuraAlgorithm setMaxEvaluations(int maxEvaluations) {
		this.maxEvaluations = maxEvaluations;
		return this;
	}

	public BasuraAlgorithm setCapacidadCamiones(int capacidadCamiones) {
		this.capacidadCamiones= capacidadCamiones;
		return this;
	}
	
}
