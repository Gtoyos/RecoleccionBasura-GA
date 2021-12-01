package main;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.uma.jmetal.problem.binaryproblem.impl.AbstractBinaryProblem;
import org.uma.jmetal.solution.binarysolution.BinarySolution;
import org.uma.jmetal.solution.binarysolution.impl.DefaultBinarySolution;
import org.uma.jmetal.util.errorchecking.JMetalException;

@SuppressWarnings("serial")
public class BasuraProblem extends AbstractBinaryProblem {

	private int cantidadContenedores,cantidadCamiones;
	private final int diasMaxSinLevantar = 2;
	private int [] basuraInicialContenedores;
	private final Random seed = new Random();
	private TSPSolver problemaTSP;
	
	/** Constructor */
	public BasuraProblem(String pathToInstanceFolder, int [] estadoInicialContenedores, int cantidadCamiones) {
	    setNumberOfVariables(1);
	    setNumberOfObjectives(1);
	    setName("BasuraProblem");

	    try {
	    	System.out.print("Loading matrix data... ");
          	long startTime = System.nanoTime();
          	problemaTSP = new TSPSolver()
          			.setPositions(MatrixLoader.readCSV(pathToInstanceFolder+"/ubicacionContenedores.csv"))
          			.setDistancia(MatrixLoader.readCSV(pathToInstanceFolder+"/distanciaContenedores.csv"))
          			.setTiempo(MatrixLoader.readCSV(pathToInstanceFolder+"/tiempoContenedores.csv"))
          			.setTiempoFromStartpoint(MatrixLoader.readCSV(pathToInstanceFolder+"/tiempoDesdeStartpoint.csv")[0])
          			.setTiempotoStartpoint(MatrixLoader.readCSV(pathToInstanceFolder+"/tiempoHaciaStartpoint.csv")[0])
          			.setDistanciaFromStartpoint(MatrixLoader.readCSV(pathToInstanceFolder+"/distanciaDesdeStartpoint.csv")[0])
          			.setDistanciatoStartpoint(MatrixLoader.readCSV(pathToInstanceFolder+"/distanciaHaciaStartpoint.csv")[0])
          			.setStartpoint(0, 0)
          			.setCapacidadCamiones(5)
          			.buildcostMatrix()
          			.buildTrucks();
		long endTime = System.nanoTime();
		System.out.println("DONE ["+(endTime - startTime)/1000000/1000.0+" s]");
	    } catch(IOException e) {
	    	e.printStackTrace();
	    }
	    this.basuraInicialContenedores = estadoInicialContenedores;
	    this.cantidadContenedores = estadoInicialContenedores.length;
	    this.cantidadCamiones = cantidadCamiones;
	}


	@Override
	public int getBitsFromVariable(int index) {
		if (index != 0)
			throw new JMetalException("BasuraProblem has only a variable. Index = " + index);
		return cantidadCamiones*cantidadContenedores*diasMaxSinLevantar*2;
	}

	@Override
	public List<Integer> getListOfBitsPerVariable() {
		return Arrays.asList(cantidadCamiones*cantidadContenedores*diasMaxSinLevantar*2);
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
	
	@Override
	public BinarySolution createSolution() {		
		BinarySolution x = new DefaultBinarySolution(getListOfBitsPerVariable(), getNumberOfObjectives());
		if(cc<100) {
			Itinerario b = generarOpt();
			x.variables().set(0, b);
			System.out.println("heh: "+evaluate(x).objectives()[0]);
			return x;
		}
		Itinerario b = new Itinerario(cantidadCamiones*cantidadContenedores*diasMaxSinLevantar*2,cantidadCamiones,cantidadContenedores,diasMaxSinLevantar);
		x.variables().set(0, b);
		for(int z=0; z<2; z++)
			for(int i=0; i<diasMaxSinLevantar; i++)
				for(int j=0; j<cantidadContenedores; j++)
					for(int k=0; k<cantidadCamiones; k++)
						((Itinerario) x.variables().get(0)).set(k,j,i,z,seed.nextInt(cantidadContenedores*cantidadCamiones)==0);
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
		System.out.println("Eval "+(cc++));
		int distancia=0;
		double fitness =0;
		int desbordados = calcularDesborde(solution);
		if(desbordados==0){
			for(int z=0; z<2; z++)
				for(int i=0; i<diasMaxSinLevantar; i++)
					for(int j=0; j<cantidadCamiones; j++){
						float [] res = problemaTSP.solve(((Itinerario) solution.variables().get(0)).getContenedores(j,i,z),false);
						if(res[1]==-1) {
							//Solución invalida, no cumple constraint de tiempo.
							solution.objectives()[0] = -1*(-Double.MAX_VALUE);
							return solution;
						}
						//System.out.println( Arrays.toString(((Itinerario) solution.variables().get(0)).getContenedores(j,i,z)) + "|" + Arrays.toString(res) + "|" + des);
						distancia+=res[0];
					}
			fitness = -distancia*distancia;
			//fitness = distancia;
		} else
			fitness = -(1e20+desbordados*desbordados);
		
		
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

	private Itinerario generarOpt() {
		Itinerario b = new Itinerario(cantidadCamiones*cantidadContenedores*diasMaxSinLevantar*2,cantidadCamiones,cantidadContenedores,diasMaxSinLevantar);
		b.set(0, 1, 0, 0, true);
		b.set(0, 2, 0, 0, true);
		b.set(0, 3, 0, 0, true);
		b.set(0, 4, 0, 0, true);
		b.set(0, 5, 0, 0, true);
		b.set(0, 6, 0, 0, true);
		b.set(0, 7, 0, 0, true);
		b.set(0, 8, 0, 0, true);
		b.set(0, 9, 0, 0, true);
		b.set(0, 0, 0, 0, true);
		return b;
	}
}
