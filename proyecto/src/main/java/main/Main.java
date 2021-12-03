package main;

import java.io.IOException;
import java.util.Arrays;

import org.uma.jmetal.util.JMetalLogger;




public class Main {

	public static void main(String[] args) {
		Algorithmlauncher();
	}
	
	public static void Algorithmlauncher() {
		
		//Define la instancia particular del problema.
		int cantidadDeCamiones = 5;
		int capacidadCamiones = 6;
		int [] c0 = new int[50];
		for(int i=0; i<50; i++)
			c0[i] = 1;
		String pathToInstanceFolder = "i50";
		
		//Parametros de ejecucion del algoritmo
		int popsize = 100;
		int maxEval = 10000;
		int cores = 8;
		
		Itinerario sol = (new BasuraAlgorithm(pathToInstanceFolder,c0))
				.setCantidadCamiones(cantidadDeCamiones)
				.setCapacidadCamiones(capacidadCamiones)
				.setPopulationSize(popsize)
				.setMaxEvaluations(maxEval)
				.setCores(cores)
				.run(); 

		
		JMetalLogger.logger.info("~~~Resultados Algoritmo Evolutivo~~~");
	    JMetalLogger.logger.info("Total execution time: " + sol.getComputingTime() + "ms");
	    JMetalLogger.logger.info("Objectives values have been written to file FUN.tsv");
	    JMetalLogger.logger.info("Variables values have been written to file VAR.tsv");

	    JMetalLogger.logger.info("Fitness: " + sol.getFitness()) ;
	    JMetalLogger.logger.info("Solution: " + sol.toString()) ;
	    
	    System.out.println("Greedy: ");
	    Greedy g = new Greedy();
	    try {
					g.setDistancia(MatrixLoader.readCSV(pathToInstanceFolder+"/distanciaContenedores.csv"))
					.setTiempo(MatrixLoader.readCSV(pathToInstanceFolder+"/tiempoContenedores.csv"))
					.setTiempoFromStartpoint(MatrixLoader.readCSV(pathToInstanceFolder+"/tiempoDesdeStartpoint.csv")[0])
					.setTiempotoStartpoint(MatrixLoader.readCSV(pathToInstanceFolder+"/tiempoHaciaStartpoint.csv")[0])
					.setDistanciaFromStartpoint(MatrixLoader.readCSV(pathToInstanceFolder+"/distanciaDesdeStartpoint.csv")[0])
					.setDistanciatoStartpoint(MatrixLoader.readCSV(pathToInstanceFolder+"/distanciaHaciaStartpoint.csv")[0])
					.setCantidadCamiones(cantidadDeCamiones)
					.setBasuraInicialContenedores(c0)
					.setCAPACIDAD_MAXIMA(capacidadCamiones);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    System.out.println(g.solve(-1).toString());
	}
	
	public static void test() {
		TSPSolver problemaTSP = new TSPSolver();
		String pathToInstanceFolder = "i10";
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

	public static void generadorDeInstancias() {
	    float [][] tiempo= null;
		float [][] distancia= null;
		float [][] positions= null;
		float [] tiempoToStartpoint= null;
		float [] tiempoFromStartpoint= null;
		float [] distanciaToStartpoint = null;
		float [] distanciaFromStartpoint = null;
		try {
		    tiempo = MatrixLoader.readCSV("data\\ContenedoresGraphHopper\\tiempo.csv");
			distancia = MatrixLoader.readCSV("data\\ContenedoresGraphHopper\\distancia.csv");
			positions = MatrixLoader.readCSV("data\\coordenadasContenedores.csv");
			tiempoToStartpoint = MatrixLoader.readCSV("data\\ContenedoresGraphHopper\\tiempoHaciaStartpoint.csv")[0];
			tiempoFromStartpoint = MatrixLoader.readCSV("data\\ContenedoresGraphHopper\\tiempoDesdeStartpoint.csv")[0];
			distanciaToStartpoint = MatrixLoader.readCSV("data\\ContenedoresGraphHopper\\distanciaHaciaStartpoint.csv")[0];
			distanciaFromStartpoint = MatrixLoader.readCSV("data\\ContenedoresGraphHopper\\distanciaDesdeStartpoint.csv")[0];	
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		tiempo = MatrixLoader.slice(50, 50, tiempo);
		distancia = MatrixLoader.slice(50, 50, distancia);
		positions = MatrixLoader.slice(50,2, positions);
		tiempoToStartpoint = MatrixLoader.slice(50, tiempoToStartpoint);
		tiempoFromStartpoint = MatrixLoader.slice(50, tiempoFromStartpoint);
		distanciaToStartpoint = MatrixLoader.slice(50, distanciaToStartpoint);
		distanciaFromStartpoint = MatrixLoader.slice(50, distanciaFromStartpoint);
		
		System.out.println(Arrays.toString(tiempoToStartpoint));
		
		MatrixLoader.writeCSV(distancia, "i50/distanciaContenedores.csv");
		MatrixLoader.writeCSV(tiempo, "i50/tiempoContenedores.csv");
		MatrixLoader.writeCSV(positions, "i50/ubicacionContenedores.csv");
		MatrixLoader.writeCSV(tiempoToStartpoint, "i50/tiempoHaciaStartpoint.csv");
		MatrixLoader.writeCSV(tiempoFromStartpoint, "i50/tiempoDesdeStartpoint.csv");
		MatrixLoader.writeCSV(distanciaToStartpoint, "i50/distanciaHaciaStartpoint.csv");
		MatrixLoader.writeCSV(distanciaFromStartpoint, "i50/distanciaDesdeStartpoint.csv");
	}
}
