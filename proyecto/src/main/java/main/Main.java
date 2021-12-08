package main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import org.uma.jmetal.util.JMetalLogger;




public class Main {
	
	//Parametros de ejecucion del algoritmo por defecto
	static int popsize = 150; //Resultado ajuste paramétrico.
	static int maxEval = 100000;
	static int cores = 8;

	public static void main(String[] args) {
		if(args.length>0)
			maxEval = Integer.valueOf(args[0]);
		if(args.length>1)
			cores = Integer.valueOf(args[1]);
		
		if(args.length>2) {
			if(args.length>3) {
				AnalisisExperimentalUnico(Integer.valueOf(args[3]));
				System.exit(0);
			}
			
			if(args[2].equals("exp"))
				AnalisisExperimental();
			else if(args[2].equals("greedy"))
				Greedylauncher();
			else
				Algorithmlauncher();
		}
		else
			Algorithmlauncher();
		System.exit(0);
	}

	public static void Greedylauncher() {
		String pathToInstanceFolder = "instance";
		float [] c0f = null;
		float [] param = null;
		try {
			c0f = MatrixLoader.readCSV(pathToInstanceFolder+"/estadoInicial.csv")[0];
			param = MatrixLoader.readCSV(pathToInstanceFolder+"/camiones.csv")[0];
		}catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		int [] c0 = new int[c0f.length];
		for(int i=0; i<c0.length; i++)
			c0[i]=(int) c0f[i];
		int cantidadDeCamiones = (int) param[0];
		int capacidadCamiones =  (int) param[1];

		BasuraAlgorithm alg = new BasuraAlgorithm(pathToInstanceFolder,c0)
				.setCantidadCamiones(cantidadDeCamiones)
				.setCapacidadCamiones(capacidadCamiones)
				.setPopulationSize(popsize)
				.setMaxEvaluations(maxEval)
				.setCores(cores);
		Itinerario sol = alg.runGreedy();
		
	    String line = "[GREEDY] " + resultOneLiner(sol) + "\n";
	    System.out.println(line);
	    Path f = Paths.get("instance/results.txt");
		try {
			Files.writeString(f, line, StandardOpenOption.CREATE,StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void AnalisisExperimentalUnico(int instance) {
		
		Path file1 = Paths.get("results/inst1.txt");
		Path file2 = Paths.get("results/inst2.txt");
		Path file3 = Paths.get("results/inst3.txt");
		Path file4 = Paths.get("results/inst4.txt");
		//Instancias de tamaño 50,30,20
		int cantidadDeCamiones;
		int cantidadDeContenedores;
		int capacidadCamiones;

		//Parametros candidatos
		int [] pops = {50,100,150};
		float [] cross = {0.6f,0.75f,0.95f};
		float [] mut = {-10,0.001f,0.01f,0.1f};

		for(int p: pops)
			for(float c: cross)
				for(float m: mut)
					for(int k=0; k<50; k++) {
						
						// ----------- INSTANCIA 1 ---------- //
						if(instance==1){
							cantidadDeCamiones = 5;
							capacidadCamiones = 5;
							cantidadDeContenedores = 50;
							int [] c0 = new int[cantidadDeContenedores];
							for(int i=0; i<cantidadDeContenedores; i+=i+4)
								c0[i] = 1;
							if(m<0)
								m=1f/((float) cantidadDeCamiones*cantidadDeContenedores*2f*2f);
							
							BasuraAlgorithm alg = new BasuraAlgorithm("i50",c0)
									.setCantidadCamiones(cantidadDeCamiones)
									.setCapacidadCamiones(capacidadCamiones)
									.setPopulationSize(p)
									.setMaxEvaluations(maxEval)
									.setCores(cores);
							alg.crossoverP = c;
							alg.mutationP = m;
							
							Itinerario sol = alg.run3();
							
							String line = p + " " + c +" " + m +" " + k + " " + resultOneLiner(sol)+ "\n";
							try {
								Files.writeString(file1, line, StandardOpenOption.APPEND);
							} catch (IOException e) {
								e.printStackTrace();
							}
							alg = null;
						}else if(instance==2) {
						// ----------- INSTANCIA 2 ---------- //
						//
							cantidadDeCamiones = 3;
							capacidadCamiones = 4;
							cantidadDeContenedores = 40;
							int [] c0 = new int[cantidadDeContenedores];
							for(int i=0; i<cantidadDeContenedores; i+=i+4)
								c0[i] = 1;
							if(m<0)
								m=1f/((float) cantidadDeCamiones*cantidadDeContenedores*2f*2f);
							
							BasuraAlgorithm alg = new BasuraAlgorithm("i40",c0)
									.setCantidadCamiones(cantidadDeCamiones)
									.setCapacidadCamiones(capacidadCamiones)
									.setPopulationSize(p)
									.setMaxEvaluations(maxEval)
									.setCores(cores);
							alg.crossoverP = c;
							alg.mutationP = m;
							
							Itinerario sol = alg.run3();
							
							String line = p + " " + c +" " + m +" " + k + " " + resultOneLiner(sol) + "\n";
							try {
								Files.writeString(file2, line, StandardOpenOption.APPEND);
							} catch (IOException e) {
								e.printStackTrace();
							}
							alg = null;
						}else if(instance==3) {
						// ----------- INSTANCIA 3 ---------- //
							cantidadDeCamiones = 2;
							capacidadCamiones = 5;
							cantidadDeContenedores = 30;
							int [] c0 = new int[cantidadDeContenedores];
							for(int i=0; i<cantidadDeContenedores; i+=i+4)
								c0[i] = 1;
							if(m<0)
								m=1f/((float) cantidadDeCamiones*cantidadDeContenedores*2f*2f);
							
							BasuraAlgorithm alg = new BasuraAlgorithm("i30",c0)
									.setCantidadCamiones(cantidadDeCamiones)
									.setCapacidadCamiones(capacidadCamiones)
									.setPopulationSize(p)
									.setMaxEvaluations(maxEval)
									.setCores(cores);
							alg.crossoverP = c;
							alg.mutationP = m;
							
							Itinerario sol = alg.run3();
							
							String line = p + " " + c +" " + m +" " + k + " " + resultOneLiner(sol)+ "\n";
							try {
								Files.writeString(file3, line, StandardOpenOption.APPEND);
							} catch (IOException e) {
								e.printStackTrace();
							}
							alg = null;
						// ----------- INSTANCIA 4 ---------- //
						} else if(instance==4){
								cantidadDeCamiones = 5;
								capacidadCamiones = 5;
								cantidadDeContenedores = 60;
								int [] c0 = new int[cantidadDeContenedores];
								for(int i=0; i<cantidadDeContenedores; i+=i+4)
									c0[i] = 1;
								if(m<0)
									m=1f/((float) cantidadDeCamiones*cantidadDeContenedores*2f*2f);
								
								BasuraAlgorithm alg = new BasuraAlgorithm("i60",c0)
										.setCantidadCamiones(cantidadDeCamiones)
										.setCapacidadCamiones(capacidadCamiones)
										.setPopulationSize(p)
										.setMaxEvaluations(maxEval)
										.setCores(cores);
								alg.crossoverP = c;
								alg.mutationP = m;
								
								Itinerario sol = alg.run3();
								
								String line = p + " " + c +" " + m +" " + k + " " + resultOneLiner(sol)+ "\n";
								try {
									Files.writeString(file4, line, StandardOpenOption.APPEND);
								} catch (IOException e) {
									e.printStackTrace();
								}
								alg = null;
						}
					}
		System.exit(0);
	}

	public static void AnalisisExperimental() {
		
		Path file1 = Paths.get("results/inst1.txt");
		Path file2 = Paths.get("results/inst2.txt");
		Path file3 = Paths.get("results/inst3.txt");
		
		//Instancias de tamaño 50,30,20
		int cantidadDeCamiones;
		int cantidadDeContenedores;
		int capacidadCamiones;

		//Parametros candidatos
		int [] pops = {50,100,150};
		float [] cross = {0.6f,0.75f,0.95f};
		float [] mut = {-10,0.001f,0.01f,0.1f};

		for(int p: pops)
			for(float c: cross)
				for(float m: mut)
					for(int k=0; k<50; k++) {
						
						// ----------- INSTANCIA 1 ---------- //
						{
							cantidadDeCamiones = 5;
							capacidadCamiones = 5;
							cantidadDeContenedores = 50;
							int [] c0 = new int[cantidadDeContenedores];
							for(int i=0; i<cantidadDeContenedores; i+=i+4)
								c0[i] = 1;
							if(m<0)
								m=1f/((float) cantidadDeCamiones*cantidadDeContenedores*2f*2f);
							
							BasuraAlgorithm alg = new BasuraAlgorithm("i50",c0)
									.setCantidadCamiones(cantidadDeCamiones)
									.setCapacidadCamiones(capacidadCamiones)
									.setPopulationSize(p)
									.setMaxEvaluations(maxEval)
									.setCores(cores);
							alg.crossoverP = c;
							alg.mutationP = m;
							
							Itinerario sol = alg.run3();
							
							String line = p + " " + c +" " + m +" " + k + " " + resultOneLiner(sol)+ "\n";
							try {
								Files.writeString(file1, line, StandardOpenOption.APPEND);
							} catch (IOException e) {
								e.printStackTrace();
							}
						} {
						// ----------- INSTANCIA 2 ---------- //
						//
						cantidadDeCamiones = 3;
						capacidadCamiones = 4;
						cantidadDeContenedores = 40;
						int [] c0 = new int[cantidadDeContenedores];
						for(int i=0; i<cantidadDeContenedores; i+=i+4)
							c0[i] = 1;
						if(m<0)
							m=1f/((float) cantidadDeCamiones*cantidadDeContenedores*2f*2f);
						
						BasuraAlgorithm alg = new BasuraAlgorithm("i40",c0)
								.setCantidadCamiones(cantidadDeCamiones)
								.setCapacidadCamiones(capacidadCamiones)
								.setPopulationSize(p)
								.setMaxEvaluations(maxEval)
								.setCores(cores);
						alg.crossoverP = c;
						alg.mutationP = m;
						
						Itinerario sol = alg.run3();
						
						String line = p + " " + c +" " + m +" " + k + " " + resultOneLiner(sol) + "\n";
						try {
							Files.writeString(file2, line, StandardOpenOption.APPEND);
						} catch (IOException e) {
							e.printStackTrace();
						}
						} {
						// ----------- INSTANCIA 3 ---------- //
							cantidadDeCamiones = 2;
							capacidadCamiones = 5;
							cantidadDeContenedores = 30;
							int [] c0 = new int[cantidadDeContenedores];
							for(int i=0; i<cantidadDeContenedores; i+=i+4)
								c0[i] = 1;
							if(m<0)
								m=1f/((float) cantidadDeCamiones*cantidadDeContenedores*2f*2f);
							
							BasuraAlgorithm alg = new BasuraAlgorithm("i30",c0)
									.setCantidadCamiones(cantidadDeCamiones)
									.setCapacidadCamiones(capacidadCamiones)
									.setPopulationSize(p)
									.setMaxEvaluations(maxEval)
									.setCores(cores);
							alg.crossoverP = c;
							alg.mutationP = m;
							
							Itinerario sol = alg.run3();
							
							String line = p + " " + c +" " + m +" " + k + " " + resultOneLiner(sol)+ "\n";
							try {
								Files.writeString(file3, line, StandardOpenOption.APPEND);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
	}

	private static String resultOneLiner(Itinerario r) {
		String stream="";
		String [] p=r.getResults().split("\n");
		for(String x : p) {
			stream += x.split(": ")[1];
			stream += " ";
		}
		return stream;
	}
	
	public static void Algorithmlauncher() {
		String pathToInstanceFolder = "instance";
		float [] c0f = null;
		float [] param = null;
		try {
			c0f = MatrixLoader.readCSV(pathToInstanceFolder+"/estadoInicial.csv")[0];
			param = MatrixLoader.readCSV(pathToInstanceFolder+"/camiones.csv")[0];
		}catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		int [] c0 = new int[c0f.length];
		for(int i=0; i<c0.length; i++)
			c0[i]=(int) c0f[i];
		int cantidadDeCamiones = (int) param[0];
		int capacidadCamiones =  (int) param[1];

		BasuraAlgorithm alg = new BasuraAlgorithm(pathToInstanceFolder,c0)
				.setCantidadCamiones(cantidadDeCamiones)
				.setCapacidadCamiones(capacidadCamiones)
				.setPopulationSize(popsize)
				.setMaxEvaluations(maxEval)
				.setCores(cores);
		BasuraAlgorithm.crossoverP = 0.95f; //Según ajuste paramétrico
		BasuraAlgorithm.mutationP = (1f/((float) cantidadDeCamiones*c0f.length * 4)); //Según ajuste paramétrico
		Itinerario sol = alg.run3();
		
		JMetalLogger.logger.info("~~~Resultados Algoritmo Evolutivo~~~");
	    JMetalLogger.logger.info("Total execution time: " + sol.getComp() + "ms");
	    JMetalLogger.logger.info("Fitness: " + sol.getFit()) ;
	    JMetalLogger.logger.info("Solution: " + sol.toString()) ;
	    
	    String line = resultOneLiner(sol) + "\n";
	    System.out.println(line);
	    Path f = Paths.get("instance/results.txt");
		try {
			Files.writeString(f, line, StandardOpenOption.CREATE,StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
