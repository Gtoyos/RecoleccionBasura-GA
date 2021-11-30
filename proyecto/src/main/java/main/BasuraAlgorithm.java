package main;

import java.util.List;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.singleobjective.geneticalgorithm.GeneticAlgorithmBuilder;
import org.uma.jmetal.example.AlgorithmRunner;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.SinglePointCrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.BitFlipMutation;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.problem.binaryproblem.BinaryProblem;
import org.uma.jmetal.solution.binarysolution.BinarySolution;


public class BasuraAlgorithm {
	private String instanceFolder;
	private int [] estadoInicial;
	private int cantidadCamiones=5,
			populationSize=100,
			maxEvaluations=10000;
	
	public BasuraAlgorithm(String instanceFolder, int [] estadoInicial) {
		this.instanceFolder = instanceFolder;
		this.estadoInicial = estadoInicial;
	}
	
	public Itinerario run() {
	    BinaryProblem problem = new BasuraProblem(instanceFolder, estadoInicial,cantidadCamiones);
	    
	    CrossoverOperator<BinarySolution> crossover = new SinglePointCrossover(0.6);
	    MutationOperator<BinarySolution> mutation = new BitFlipMutation(0.0008);
	    SelectionOperator<List<BinarySolution>, BinarySolution> selection = new BinaryTournamentSelection<BinarySolution>();
	    
	    Algorithm<BinarySolution> algorithm = new GeneticAlgorithmBuilder<>(problem, crossover, mutation)
	            .setPopulationSize(populationSize)
	            .setMaxEvaluations(maxEvaluations)
	            .setSelectionOperator(selection)
	            .build();

	    AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();
	    return ((Itinerario) algorithm.getResult().getVariable(0))
	    		.setFitness(algorithm.getResult().getObjective(0))
	    		.setComputingTime(algorithmRunner.getComputingTime());
	}

	
	public int getCantidadCamiones() {
		return cantidadCamiones;
	}
	public BasuraAlgorithm setCantidadCamiones(int cantidadCamiones) {
		this.cantidadCamiones = cantidadCamiones;
		return this;
	}

	public int getPopulationSize() {
		return populationSize;
	}

	public void setPopulationSize(int populationSize) {
		this.populationSize = populationSize;
	}

	public int getMaxEvaluations() {
		return maxEvaluations;
	}

	public void setMaxEvaluations(int maxEvaluations) {
		this.maxEvaluations = maxEvaluations;
	}
	
}
