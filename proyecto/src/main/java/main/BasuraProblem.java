package main;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.uma.jmetal.problem.binaryproblem.impl.AbstractBinaryProblem;
import org.uma.jmetal.solution.binarysolution.BinarySolution;
import org.uma.jmetal.solution.binarysolution.impl.DefaultBinarySolution;
import org.uma.jmetal.util.JMetalException;

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

	    problemaTSP = new TSPSolver();
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
	@Override
	public BinarySolution createSolution() {		
		BinarySolution x = new DefaultBinarySolution(getListOfBitsPerVariable(), getNumberOfObjectives());
		Itinerario b = new Itinerario(cantidadCamiones*cantidadContenedores*diasMaxSinLevantar*2,cantidadCamiones,cantidadContenedores,diasMaxSinLevantar);
		x.setVariable(0, b);
		for(int z=0; z<2; z++)
			for(int i=0; i<diasMaxSinLevantar; i++)
				for(int j=0; j<cantidadContenedores; j++)
					for(int k=0; k<cantidadCamiones; k++)
						((Itinerario) x.getVariable(0)).set(k,j,i,z,seed.nextInt(cantidadContenedores*cantidadCamiones)==0);
		return x;
	}
	static int x=0;
	
	/**
	 * Calcula el fitness de la solución. El fitness se calcula en base a las restricciones de recolección y la distancia del itinerario
	 * 
	 * @param solution solución a evaluar.
	 */
	@Override
	public void evaluate(BinarySolution solution) {
		System.out.println("Eval "+(x++));
		int distancia=0;
		int fitness =0;
		int desbordados = calcularDesborde(solution);
		if(desbordados==0){
			for(int z=0; z<2; z++)
				for(int i=0; i<diasMaxSinLevantar; i++)
					for(int j=0; j<cantidadCamiones; j++){
						float [] res = problemaTSP.solve(((Itinerario) solution.getVariable(0)).getContenedores(j,i,z),false);
						distancia+=res[0];
					}
        fitness = distancia;
		} else
			fitness = -(desbordados*desbordados);
		
	    // maximization problem: multiply by -1 to minimize
	    solution.setObjective(0, -1*fitness);
	}

	private int calcularDesborde(BinarySolution solution) {
		int desbordados =0;
		int [] state = basuraInicialContenedores;
		for(int i=0; i<diasMaxSinLevantar; i++){
			for(int j=0; j<cantidadContenedores; j++)
				state[j] = state[j]+1;
			int [] lev = ((Itinerario) solution.getVariable(0)).getContenedoresLevantadosEnElDia(i);
			for(int j=0; j<cantidadContenedores; j++) {
				state[j] = ((lev[j]==1) ? 0: state[j]);
				if(state[j]>diasMaxSinLevantar)
					desbordados+=state[j]-diasMaxSinLevantar;	
			}
		}
		return desbordados;
	}

}
