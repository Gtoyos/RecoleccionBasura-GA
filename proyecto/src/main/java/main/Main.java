package main;

import java.io.IOException;
import java.util.Arrays;

import org.uma.jmetal.util.JMetalLogger;




public class Main {

	public static void main(String[] args) {
		launcher();
	}
	
	public static void launcher() {
		int [] c0 = new int[10];

		Itinerario sol = (new BasuraAlgorithm("simple",c0)).run2();

	    System.out.println("~Resultados Algoritmo Evolutivo~");
	    JMetalLogger.logger.info("Total execution time: " + sol.getComputingTime() + "ms");
	    JMetalLogger.logger.info("Objectives values have been written to file FUN.tsv");
	    JMetalLogger.logger.info("Variables values have been written to file VAR.tsv");

	    JMetalLogger.logger.info("Fitness: " + sol.getFitness()) ;
	    JMetalLogger.logger.info("Solution: " + sol.toString()) ;
	    
	    System.out.println("Greedy: ");
	    String pathToInstanceFolder = "simple";
	    Greedy g = new Greedy();
	    try {
					g.setDistancia(MatrixLoader.readCSV(pathToInstanceFolder+"/distanciaContenedores.csv"))
					.setTiempo(MatrixLoader.readCSV(pathToInstanceFolder+"/tiempoContenedores.csv"))
					.setTiempoFromStartpoint(MatrixLoader.readCSV(pathToInstanceFolder+"/tiempoDesdeStartpoint.csv")[0])
					.setTiempotoStartpoint(MatrixLoader.readCSV(pathToInstanceFolder+"/tiempoHaciaStartpoint.csv")[0])
					.setDistanciaFromStartpoint(MatrixLoader.readCSV(pathToInstanceFolder+"/distanciaDesdeStartpoint.csv")[0])
					.setDistanciatoStartpoint(MatrixLoader.readCSV(pathToInstanceFolder+"/distanciaHaciaStartpoint.csv")[0])
					.setCantidadCamiones(10)
					.setCantidadContenedores(10)
					.setBasuraInicialContenedores(c0)
					.setCAPACIDAD_MAXIMA(5);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    System.out.println(g.solve(-1).toString());
	}
	
	public static void test() {
		TSPSolver problemaTSP = new TSPSolver();
		String pathToInstanceFolder = "simple";
	    try {
	    	System.out.print("Loading matrix data... ");
	      	long startTime = System.nanoTime();
			problemaTSP.setPositions(MatrixLoader.readCSV(pathToInstanceFolder+"/ubicacionContenedores.csv"));
			problemaTSP.setDistancia(MatrixLoader.readCSV(pathToInstanceFolder+"/distanciaContenedores.csv"));
			problemaTSP.setTiempo(MatrixLoader.readCSV(pathToInstanceFolder+"/tiempoContenedores.csv"));
			problemaTSP.setTiempoFromStartpoint(MatrixLoader.readCSV(pathToInstanceFolder+"/tiempoDesdeStartpoint.csv")[0]);
			problemaTSP.setTiempotoStartpoint(MatrixLoader.readCSV(pathToInstanceFolder+"/tiempoHaciaStartpoint.csv")[0]);
			problemaTSP.setDistanciaFromStartpoint(MatrixLoader.readCSV(pathToInstanceFolder+"/distanciaDesdeStartpoint.csv")[0]);
			problemaTSP.setDistanciatoStartpoint(MatrixLoader.readCSV(pathToInstanceFolder+"/distanciaHaciaStartpoint.csv")[0]);
			problemaTSP.setStartpoint(10,0);
			problemaTSP.buildcostMatrix();
		long endTime = System.nanoTime();
		System.out.println("DONE ["+(endTime - startTime)/1000000/1000.0+" s]");
	    } catch(IOException e) {
	    	e.printStackTrace();
	    }
		int [] index = new int[10];
		for(int i=0;i<-1;i++)
			index[i] = 1;

		System.out.print("Calculating route... ");
		long startTime = System.nanoTime();
		float [] resu = problemaTSP.solve(index,true);
		System.out.println(Arrays.toString(resu));
		long endTime = System.nanoTime();
		long duration = (endTime - startTime)/1000000;
		System.out.println("DONE ["+duration/1000.0+" s]");
	}

}
