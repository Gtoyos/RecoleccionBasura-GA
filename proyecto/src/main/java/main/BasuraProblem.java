package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;

import org.uma.jmetal.problem.binaryproblem.impl.AbstractBinaryProblem;
import org.uma.jmetal.solution.binarysolution.BinarySolution;
import org.uma.jmetal.solution.binarysolution.impl.DefaultBinarySolution;
import org.uma.jmetal.util.errorchecking.JMetalException;

@SuppressWarnings("serial")
public class BasuraProblem extends AbstractBinaryProblem {

	private int cantidadContenedores,cantidadCamiones,capacidadCamiones;
	private final int diasMaxSinLevantar = 2;
	private double MAX_DIST;
	private int [] basuraInicialContenedores;
	private TSPSolver problemaTSP;
	private Greedy greed;
	
	//Parametros de la funcion de fitness.
	private int factorTurnoDiurno=2; //Es 2 veces mas costoso recorrer la ciudad de dia que denoche.
	private double tiempoMaximo=60*60*8*1000; //Tiempo maximo de cada recorrido (en ms).
	private int factorTiempo= 1000*60*60; //Exederse 1h tiene el mismo costo que dejar un contenedor desbordado 1 dia.
	
	/** Constructor */
	public BasuraProblem(String pathToInstanceFolder, int [] estadoInicialContenedores, int cantidadCamiones, int capacidadCamiones) {
	    setNumberOfVariables(1);
	    setNumberOfObjectives(1);
	    setName("BasuraProblem");
	    float [][] tiempo= null;
		float [][] distancia= null;
		float [][] positions= null;
		float [] tiempoToStartpoint= null;
		float [] tiempoFromStartpoint= null;
		float [] distanciaToStartpoint = null;
		float [] distanciaFromStartpoint = null;
	    try {
	    	System.out.print("Loading matrix data... ");
          	long startTime = System.nanoTime();
    	    tiempo = MatrixLoader.readCSV(pathToInstanceFolder+"/tiempoContenedores.csv");
    		distancia = MatrixLoader.readCSV(pathToInstanceFolder+"/distanciaContenedores.csv");
    		positions = MatrixLoader.readCSV(pathToInstanceFolder+"/ubicacionContenedores.csv");
    		tiempoToStartpoint = MatrixLoader.readCSV(pathToInstanceFolder+"/tiempoHaciaStartpoint.csv")[0];
    		tiempoFromStartpoint = MatrixLoader.readCSV(pathToInstanceFolder+"/tiempoDesdeStartpoint.csv")[0];
    		distanciaToStartpoint = MatrixLoader.readCSV(pathToInstanceFolder+"/distanciaHaciaStartpoint.csv")[0];
    		distanciaFromStartpoint = MatrixLoader.readCSV(pathToInstanceFolder+"/distanciaDesdeStartpoint.csv")[0];
    		long endTime = System.nanoTime();
    		System.out.println("DONE ["+(endTime - startTime)/1000000/1000.0+" s]");
	    }catch(IOException e) {
	    	e.printStackTrace();
	    }
        problemaTSP = new TSPSolver()
			.setPositions(positions)
			.setDistancia(distancia)
			.setTiempo(tiempo)
			.setTiempoFromStartpoint(tiempoFromStartpoint)
			.setTiempotoStartpoint(tiempoToStartpoint)
			.setDistanciaFromStartpoint(distanciaFromStartpoint)
			.setDistanciatoStartpoint(distanciaToStartpoint)
			.setStartpoint(0, 0)
			.setCapacidadCamiones(capacidadCamiones)
			.buildcostMatrix()
			.buildTrucks();
        greed = new Greedy()
			.setDistancia(distancia)
			.setTiempo(tiempo)
			.setTiempoFromStartpoint(tiempoFromStartpoint)
			.setTiempotoStartpoint(tiempoToStartpoint)
			.setDistanciaFromStartpoint(distanciaFromStartpoint)
			.setDistanciatoStartpoint(distanciaToStartpoint)
			.setCantidadCamiones(cantidadCamiones)
			.setBasuraInicialContenedores(estadoInicialContenedores)
			.setCAPACIDAD_MAXIMA(capacidadCamiones);
    					
	    this.basuraInicialContenedores = estadoInicialContenedores;
	    this.cantidadContenedores = estadoInicialContenedores.length;
	    this.cantidadCamiones = cantidadCamiones;
	    this.capacidadCamiones= capacidadCamiones;
	    this.MAX_DIST = getDistanciaMaxima(distancia);
	}

	@Override
	public int getBitsFromVariable(int index) {
		if (index != 0)
			throw new JMetalException("BasuraProblem has only a variable. Index = " + index);
		return cantidadCamiones*cantidadContenedores*diasMaxSinLevantar*2;
	}

	@Override
	public List<Integer> getListOfBitsPerVariable() {
		return new ArrayList<Integer>(Arrays.asList(cantidadCamiones*cantidadContenedores*diasMaxSinLevantar*2));
	}

	/**
	 * Crea una solución posible. Una solución consiste de un itinerario con los contenedores que levantará cada camión
	 * en cada turno de cada día. La solución está representada como un conjunto de matrices para cada turno de cada dia. Donde las
	 * filas son cada camión y las columnas los contenedores. Una entrada 1 en la fila i columna j indica que el camión i levantará
	 * el contenedor j para el turno y día que representa dicha matriz.
	 * 
	 * @return Retorna la matriz codificada como una BinarySolution.
	 */
	
	static int cc=0;
	boolean primera_vez = true;
	@Override
	public BinarySolution createSolution() {
		BinarySolution x = new DefaultBinarySolution(getListOfBitsPerVariable(), getNumberOfObjectives());
		if(primera_vez) {
			//Greedy.
			Itinerario b = greed.solve(-1);
			x.variables().set(0, b);
			primera_vez=false;
			return x;
		}
		int strategy = ThreadLocalRandom.current().nextInt(0, 6);
		Itinerario b = null;
		if(strategy==0) {
			//Greedy 
			int start = ThreadLocalRandom.current().nextInt(0, cantidadContenedores);
			b = greed.solve(start);
		}
		else if(strategy>1) {
			b = new Itinerario(cantidadCamiones*cantidadContenedores*diasMaxSinLevantar*2,cantidadCamiones,cantidadContenedores,diasMaxSinLevantar);
	        for(int z=0; z<2; z++)
	            for(int i=0; i<diasMaxSinLevantar; i++)
	                for(int j=0; j<cantidadContenedores; j++)
	                    for(int k=0; k<cantidadCamiones; k++)
	                        b.set(k,j,i,z,(ThreadLocalRandom.current().nextInt(0, (int) Math.ceil( ((float)cantidadContenedores) / ((float)cantidadCamiones) )))==0);

		}
		else if(strategy==1) {
			//Greedy con mezcla de dias/turno
			int start = ThreadLocalRandom.current().nextInt(0, cantidadContenedores);
			b = greed.solve(start);
			b.shuffleDiasTurnos();
		}
		x.variables().set(0, b);
		return x;
	}
	
	
	/**
	 * Calcula el fitness de la solución. El fitness se calcula en base a las restricciones de recolección y la distancia del itinerario
	 * 
	 * @param solution solución a evaluar.
	 * @return solución evaluada.
	 */
	@Override
	public BinarySolution evaluate(BinarySolution solution) {
		double distancia=0;
		double distanciaReal=0;
		double tiempo=0;
		double fitness =0;
		int desbordados = calcularDesborde(solution);
		if(desbordados==0){
			for(int z=0; z<2; z++)
				for(int i=0; i<diasMaxSinLevantar; i++)
					for(int j=0; j<cantidadCamiones; j++){
						float [] res = problemaTSP.solve(((Itinerario) solution.variables().get(0)).getContenedores(j,i,z),false);
						if(res[1]==-1) {
							reparar((Itinerario) solution.variables().get(0));
							return evaluate(solution);
						}
						distancia+=res[0]*(z==0 ? factorTurnoDiurno:1);
						distanciaReal+=res[0];
						tiempo+=res[1];
					}
			if(tiempo > tiempoMaximo*cantidadCamiones*diasMaxSinLevantar*2) {
				fitness = -1*(1/factorTiempo)*tiempo;
				System.out.println("timeput");
			}
			else
				fitness = factorTurnoDiurno*MAX_DIST*MAX_DIST - distancia*distancia;
		} else {
			fitness = -1*desbordados*desbordados;
			((Itinerario) solution.variables().get(0)).setHayDesborde(true);
		}
		((Itinerario) solution.variables().get(0)).setFitness(fitness);
		((Itinerario) solution.variables().get(0)).setDistancia((float) distanciaReal);
		((Itinerario) solution.variables().get(0)).setTiempo((float) tiempo);
		
		System.out.println("Eval "+(cc++)+" "+fitness+"distancia: "+distancia+"desborde: "+desbordados+" tiempo: "+tiempo);
	    
		// maximization problem: multiply by -1 to minimize
		solution.objectives()[0] = -1*fitness;
		return solution;
	}

	private int calcularDesborde(BinarySolution solution) {
		int desbordados =0;
		int [] state = basuraInicialContenedores.clone();
		for(int i=0; i<diasMaxSinLevantar; i++){
			for(int q=0; q<state.length; q++)
				state[q] +=1;
			int [] lev = ((Itinerario) solution.variables().get(0)).getContenedoresLevantadosEnElDia(i);
			for(int j=0; j<cantidadContenedores; j++) {
				state[j] = ((lev[j]==1) ? 0: state[j]);
				if(state[j]>=diasMaxSinLevantar)
					desbordados+=state[j]-diasMaxSinLevantar+1;	
			}

		}
		return desbordados;
	}

	private Itinerario reparar(Itinerario r) {
		class DtHorario{
			int camion,dia,turno;
		}
		List<Integer> toAllocate = new ArrayList<>();
		Map<DtHorario,Integer> camionesOciosos = new HashMap<>();
		int [] bins;
		for(int z=0; z<2; z++)
			for(int i=0; i<diasMaxSinLevantar; i++)
				for(int j=0; j<cantidadCamiones; j++){
					bins = r.getContenedores(j,i,z);
					int s = Arrays.stream(bins).sum();
					while(s>capacidadCamiones) {
						int p = ThreadLocalRandom.current().nextInt(0, cantidadContenedores);
						if(bins[p]==1) {
							bins[p]=0;
							r.set(j, p, i, z, false);
							toAllocate.add(p);
							s--;
						}
					}
					if(s<capacidadCamiones) {
						DtHorario d = new DtHorario();
						d.camion=j;
						d.dia=i;
						d.turno=z;
						camionesOciosos.put(d, capacidadCamiones-s);
					}
				}
		int i=0;
		for(Entry<DtHorario, Integer> x : camionesOciosos.entrySet()) {
			for(int j=0; j<x.getValue() && i< toAllocate.size(); j++)
				r.set(x.getKey().camion, toAllocate.get(i++), x.getKey().dia, x.getKey().turno, true);
		}
		if(i<toAllocate.size())
			r.setHayDesborde(true);
		return r;
	}


	public int getFactorTurnoDiurno() {
		return factorTurnoDiurno;
	}


	public void setFactorTurnoDiurno(int factorTurnoDiurno) {
		this.factorTurnoDiurno = factorTurnoDiurno;
	}


	public double getTiempoMaximo() {
		return tiempoMaximo;
	}


	public void setTiempoMaximo(double tiempoMaximo) {
		this.tiempoMaximo = tiempoMaximo;
	}


	public int getFactorTiempo() {
		return factorTiempo;
	}


	public void setFactorTiempo(int factorTiempo) {
		this.factorTiempo = factorTiempo;
	}
	

	private double getDistanciaMaxima(float[][] distancia) {
		double max=-1;
		for(int i=0; i< distancia.length; i++)
			for(int j=0; j<distancia[i].length; j++)
				if(distancia[i][j]>max)
					max = distancia[i][j];
		return max*this.capacidadCamiones*this.cantidadCamiones*this.diasMaxSinLevantar*2;
		
	}
}
