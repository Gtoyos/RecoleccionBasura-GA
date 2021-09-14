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

	    //CARGAR MATRIZ DEL PROBLEMA
	    int filas = 36;
	    int columnas = 45;
	    String path = "instancias/36x45/3/instancia.in";
	    	    
	    problem = new AntenaProblem(filas,columnas,path) ;

	    crossover = new SinglePointCrossover(0.6);

	    double mutationProbability = 1.0/(filas*columnas)-0.00005;
	    //double mutationProbability = 0.0008;
	    mutation = new BitFlipMutation(mutationProbability) ;
	    
	    selection = new BinaryTournamentSelection<BinarySolution>();
	    //selection = new RandomSelection<BinarySolution>();
	    algorithm = new GeneticAlgorithmBuilder<>(problem, crossover, mutation)
	            .setPopulationSize(100)
	            .setMaxEvaluations(100000)
	            .setSelectionOperator(selection)
	            .build();
	    
	    AlgoRunner algorithmRunner = new AlgoRunner.Executor(algorithm)
	            .execute() ;
	    BinarySolution solution = algorithm.getResult() ;
	    
	    
	    File salida = new File("solucion.out");
	    salida.createNewFile();
	    FileWriter writer = new FileWriter(salida);
	    writer.write(getFenotipo(filas,columnas,solution));
	    writer.close();
	    //System.out.println("Coordenadas de la solución: \n"+q);
	    long computingTime = algorithmRunner.getComputingTime() ;

	    
	    List<BinarySolution> population = new ArrayList<>(1) ;
	    population.add(solution) ;
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
	    
	    System.out.println("Antenas emplazadas: "+(solution.getObjective(0)+filas*columnas));
	    //ForzaBruta rt = new ForzaBruta(6,6);
	    //rt.optimo();
	  }
	  
	  private static String getFenotipo(int filas, int columnas,BinarySolution solution) {
		  String res="";
		  for (int i = 0; i < filas*columnas; i++) {
			  if(solution.getVariable(0).get(i)) {
				  res = res + i%columnas +" "+ i/columnas + "\n";
			  }
		  }
		  return res;
	  }
}
