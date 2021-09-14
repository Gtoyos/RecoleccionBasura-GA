package logica;

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


public class AntenaBatch {
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
	    String path = "instancias/36x45/1/instancia.in";
	    	    
	    problem = new AntenaProblem(filas,columnas,path) ;

	    crossover = new SinglePointCrossover(0.7);

	    double mutationProbability = 0.01;
	    mutation = new BitFlipMutation(mutationProbability) ;
	    
	    selection = new BinaryTournamentSelection<BinarySolution>();
	    //selection = new RandomSelection<BinarySolution>();
	    algorithm = new GeneticAlgorithmBuilder<>(problem, crossover, mutation)
	            .setPopulationSize(100)
	            .setMaxEvaluations(100000)
	            .setSelectionOperator(selection)
	            .build();
	    
	    double min=Double.MAX_VALUE,opt=0,avg=0,mdev=0;
	    List<Double> results = new ArrayList<>();
	    for(int k=0;k<50;k++) {  
		    AlgoRunner algorithmRunner = new AlgoRunner.Executor(algorithm).execute() ;
		    BinarySolution solution = algorithm.getResult() ;
		    results.add(-1*solution.getObjective(0));
	    }
	    for(double x: results) {
	    	avg = avg+x;
	    	if(x>opt)
	    		opt=x;
	    	if(x<min)
	    		min=x;
	    }
	    avg = avg/50.0;
	    double s=0;
	    for(double x:results)
	    	s = s + (x-avg)*(x-avg);
	    mdev = Math.sqrt(avg/50.0);
	    
	    System.out.println("RESULTADOS DE LA EJECCUCION (min/avg/max/mdev)");
	    System.out.println(min+"/"+avg+"/"+opt+"/"+mdev);
	  }

}