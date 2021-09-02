package logica;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.singleobjective.geneticalgorithm.GeneticAlgorithmBuilder;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.crossover.impl.SinglePointCrossover;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.mutation.impl.BitFlipMutation;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.BinaryTournamentSelection;
import org.uma.jmetal.problem.binaryproblem.BinaryProblem;
import org.uma.jmetal.solution.binarysolution.BinarySolution;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;

public class AntenaProblemSolverRunner {
	  /**
	   * Usage: java org.uma.jmetal.runner.singleobjective.GenerationalGeneticAlgorithmBinaryEncodingRunner
	   */
	  public static void main(String[] args) throws Exception {
	    BinaryProblem problem;
	    Algorithm<BinarySolution> algorithm;
	    CrossoverOperator<BinarySolution> crossover;
	    MutationOperator<BinarySolution> mutation;
	    SelectionOperator<List<BinarySolution>, BinarySolution> selection;

	    problem = new AntenaProblem() ;

	    //crossover = new AntenaCrossover(0.9);

	    //double mutationProbability = 0;
	    //mutation = new AntenaMutator(mutationProbability) ;

	    crossover = new SinglePointCrossover(0.9);

	    double mutationProbability = 0.03;
	    mutation = new BitFlipMutation(mutationProbability) ;
	    
	    selection = new BinaryTournamentSelection<BinarySolution>();
	    //selection = new RandomSelection<BinarySolution>();
	    algorithm = new GeneticAlgorithmBuilder<>(problem, crossover, mutation)
	            .setPopulationSize(100)
	            .setMaxEvaluations(90000)
	            .setSelectionOperator(selection)
	            .build();
	    
	    AlgoRunner algorithmRunner = new AlgoRunner.Executor(algorithm)
	            .execute() ;

	    
	    BinarySolution solution = algorithm.getResult() ;
	    List<BinarySolution> population = new ArrayList<>(1) ;
	    population.add(solution) ;
	    String q = "";
	    for (int i = 0; i < 36; i++) {
	    	if(solution.getVariable(0).get(i)) {
	    		q = q + i%6 +" "+ i/6 + "\n";
	    	}
	    }
	    File salida = new File("solucion.out");
	    salida.createNewFile();
	    FileWriter writer = new FileWriter(salida);
	    writer.write(q);
	    writer.close();
	    //System.out.println("Coordenadas de la solución: \n"+q);
	    long computingTime = algorithmRunner.getComputingTime() ;

	    new SolutionListOutput(population)
	            .setVarFileOutputContext(new DefaultFileOutputContext("VAR.tsv"))
	            .setFunFileOutputContext(new DefaultFileOutputContext("FUN.tsv"))
	            .print();
	    System.out.println("~Resultados Algoritmo Evolutivo~");
	    JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");
	    JMetalLogger.logger.info("Objectives values have been written to file FUN.tsv");
	    JMetalLogger.logger.info("Variables values have been written to file VAR.tsv");

	    JMetalLogger.logger.info("Fitness: " + solution.getObjective(0)) ;
	    JMetalLogger.logger.info("Solution: " + solution.getVariable(0)) ;
	    System.out.println("~Resultados Algoritmo Fuerza Bruta~");
	    ForzaBruta rt = new ForzaBruta(6,6);
	    rt.optimo();
	  }
}
