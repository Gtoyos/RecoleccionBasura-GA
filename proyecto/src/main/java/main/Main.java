package main;

import java.io.IOException;

import org.uma.jmetal.util.JMetalLogger;




public class Main {

	public static void main(String[] args) {
		int [] c0 = new int[10];
		c0[3]=1;
		c0[4]=1;
		c0[5]=1;
		c0[6]=1;
		Itinerario sol = (new BasuraAlgorithm("i10",c0)).run();

	    System.out.println("~Resultados Algoritmo Evolutivo~");
	    JMetalLogger.logger.info("Total execution time: " + sol.getComputingTime() + "ms");
	    JMetalLogger.logger.info("Objectives values have been written to file FUN.tsv");
	    JMetalLogger.logger.info("Variables values have been written to file VAR.tsv");

	    JMetalLogger.logger.info("Fitness: " + sol.getFitness()) ;
	    JMetalLogger.logger.info("Solution: " + sol.toString()) ;
	    
	}
	
	public static void test(String[] args) {
		TSPSolver s = new TSPSolver();
		try {
			System.out.print("Loading matrix data... ");
			long startTime = System.nanoTime();
			s.setPositions(MatrixLoader.readCSV("test/coordenadasContenedores.csv"));
			s.setDistancia(MatrixLoader.readCSV("test/distancia.csv"));
			s.setTiempo(MatrixLoader.readCSV("test/tiempo.csv"));
			s.setTiempoFromStartpoint(MatrixLoader.readCSV("test/tiempoDesdeCardozo.csv")[0]);
			s.setTiempotoStartpoint(MatrixLoader.readCSV("test/tiempoHaciaCardozo.csv")[0]);
			s.setDistanciaFromStartpoint(MatrixLoader.readCSV("test/distanciaDesdeCardozo.csv")[0]);
			s.setDistanciatoStartpoint(MatrixLoader.readCSV("test/distanciaHaciaCardozo.csv")[0]);
			long endTime = System.nanoTime();
			long duration = (endTime - startTime)/1000000;
			System.out.println("DONE ["+duration/1000.0+" s]");
		} catch(IOException e) {
			e.printStackTrace();
		}
		int [] index = new int[10];
		for(int i=0;i<10;i++)
			index[i++] = i;

		System.out.print("Calculating route... ");
		long startTime = System.nanoTime();
		s.solve(index,true);
		long endTime = System.nanoTime();
		long duration = (endTime - startTime)/1000000;
		System.out.println("DONE ["+duration/1000.0+" s]");
	}

}
