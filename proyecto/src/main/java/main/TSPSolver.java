package main;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import com.graphhopper.jsprit.analysis.toolbox.GraphStreamViewer;
import com.graphhopper.jsprit.analysis.toolbox.Plotter;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.termination.PrematureAlgorithmTermination;
import com.graphhopper.jsprit.core.algorithm.termination.TimeTermination;
import com.graphhopper.jsprit.core.algorithm.termination.VariationCoefficientTermination;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter.Print;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;

/**
 * Modulo para obtener la distancia y tiempo de una ruta. Utiliza la librería Jsprit.
 * La función prinucipal es solve(), para ejecutarla debe haberse ejecutado previamente buildTrucks() y buildCostMatrix().
 * 
 * @author Toyos,Vallcorba
 *
 */
public class TSPSolver implements Serializable{
	private static final long serialVersionUID = -5979648570893638696L;
	/** Tamaño máximo del caché de soluciones. */
	private static final int MAX_CACHE = 100000;
	
	public int CAPACIDAD_MAXIMA = 100; //Cantidad maxima de residuos que se pueden levantar.
	public final int COSTO_POR_DISTANCIA = 1; //Costo por metro recorrido
	public final float COSTO_POR_TIEMPO = 0;//00.001f; // Costo por segundo de transporte (En milisegundos)
	public final int COSTO_FIJO = 0;//100; //Costo fijo por utilizar el camion
	public final double MAX_TIME = 1000*60*60*24; //Cota maxima preventiva de 24hs.
	
	/** Tras llegar al contenedor, se permanece allí 3 minutos para poder levantarlo */
	public final double TIEMPOXCONTENEDOR = 60*3*1000; //Tiempo que se permanece en cada contenedor (en ms).
	
	public transient final Location felipe_cardozo =  Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(-56.0982134,-34.8504341))
			.setId("startpoint").build();
	
	private transient VehicleImpl camion;
	private transient VehicleRoutingTransportCosts costMatrix;
	private transient Location startpoint = felipe_cardozo;
	
	private PrematureAlgorithmTermination timeTermination = new TimeTermination(3000); //3 segundos por defecto
	private PrematureAlgorithmTermination coefTermination = new VariationCoefficientTermination(50, 0.001); //Por defecto. En 50 iteraciones una diferencia de 0.001 
	
	private float [][] tiempo;
	private float [][] distancia;
	private float [][] positions;
	private float [] tiempoToStartpoint;
	private float [] tiempoFromStartpoint;
	private float [] distanciaToStartpoint;
	private float [] distanciaFromStartpoint;
	private int zhash;
	private class DtSol implements Serializable{

		private static final long serialVersionUID = -7325221375719119916L;
		public int [] index;
		public int sum;
		public float [] sol;
		public DtSol(int [] i, float [] s) {
			index = i;
			sol = s;
		}
	}
	/** Cache de soluciones. Permite almacenar soluciones ya calculadas para devolverlas de forma inmediata */
	private static Map<Integer,List<DtSol>> cache = new Hashtable<Integer,List<DtSol>>();

	TSPSolver(){ }
	

	/**
	 * Version alternativa de float con tiempo de ejecución e información adicional que se impormie en la salida estandar. 
	 * @param indiceContenedores
	 * @param plotResults
	 * @return retorna float[] donde la primer entrada es la distancia recorrida en metros del recorrido y la segunda entrada es el tiempo empleado para hacerlo
	 */
	public float [] solveVerbose(int [] indiceContenedores,boolean plotResults){
    	System.out.print("TSPSolver::solve("+Arrays.toString(indiceContenedores)+")");
      	long startTime = System.nanoTime();
      	float [] r = solve(indiceContenedores,plotResults);
		long endTime = System.nanoTime();
		System.out.println("DONE ["+(endTime - startTime)/1000000/1000.0+" s]");
		return r;
	}
	public float [] solve(int [] indiceContenedores){
		return solve(indiceContenedores,false);
	}
	
    /**
     * Calcula la ruta optima para levatar los contenedores 
     * <p>Todas las matrices del problema deben estar cargadas antes de poder ejecutar esta función. Es decir, deben ejecutarse
     * las funciones buildCostMatrix() y buildTrucks() previamente. </p>
     * @param plotResults true para imprimir los resultados
     * @param indiceContenedores contiene los contenedores que se quieren levantar segun su posicion en la matriz positions
     * @return retorna float[] donde la primer entrada es la distancia recorrida en metros del recorrido y la segunda entrada es el tiempo empleado para hacerlo
     */	
	public float [] solve(int [] indiceContenedores,boolean plotResults){
		int sumi = Arrays.stream(indiceContenedores).sum();
		int hash = Arrays.hashCode(indiceContenedores);
		//long startTime = System.nanoTime();
		if(cache.containsKey(hash)) {
			if(cache.get(hash).size()==1) {
				return cache.get(hash).get(0).sol;
			}
			for(DtSol s : cache.get(hash)) {
				if(sumi==s.sum && Arrays.equals(s.index,indiceContenedores)) {
					return s.sol;
				}
			}
		}
		//long endTime = System.nanoTime();
		//System.out.print("HASH ["+(endTime - startTime)/1000000/1000.0+" s]");
		
		if(zhash==hash && Arrays.stream(indiceContenedores).sum()==0) {
			float [] r = {0,0};
			cache(indiceContenedores,r,sumi);
			return r;
		}
		else if(Arrays.stream(indiceContenedores).sum()>CAPACIDAD_MAXIMA) {
			float [] r = {0,-1};
			cache(indiceContenedores,r,sumi);
			return r;
		}
		//startTime = System.nanoTime();
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance()
				.addVehicle(camion)
				.setFleetSize(FleetSize.FINITE)
				.setRoutingCost(costMatrix);

		for(int i=0;i<indiceContenedores.length;i++) {
			if(indiceContenedores[i]==1) {
				Location l =  Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(positions[i][0],
						positions[i][1]))
						.setId(String.valueOf(i)).build();
				vrpBuilder.addJob(Service.Builder.newInstance(String.valueOf(i)).setLocation(l).setServiceTime(TIEMPOXCONTENEDOR).addSizeDimension(0, 1).build());
			}
		}
		VehicleRoutingProblem problem = vrpBuilder.build();
		VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(problem);
		TimeTermination prematureTermination = new TimeTermination(3000); //No parecería ser un hard contstraint. Reduce el tiempo significativamente, pero no son 3 segundos ni cerca.
		
		algorithm.setPrematureAlgorithmTermination(prematureTermination);
		algorithm.addListener(prematureTermination);

	   // endTime = System.nanoTime();
	    //System.out.print("BUILT ["+(endTime - startTime)/1000000/1000.0+" s]");
	    
	    //startTime = System.nanoTime();
		Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
		VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);
	    //endTime = System.nanoTime();
	    //System.out.print("FOUND ["+(endTime - startTime)/1000000/1000.0+" s]");
		if(plotResults) {
			SolutionPrinter.print(problem, bestSolution, Print.VERBOSE);
			new GraphStreamViewer(problem, bestSolution).setRenderDelay(100).display();
			new Plotter(problem,bestSolution).plot("solution.png", "solution");
		}
		float [] result = new float[2];
		if(bestSolution.getUnassignedJobs().size()>0 || bestSolution.getRoutes().size()==0){
			result[0] = 0; result[1] = -1;
		} else {
			result[0] = (float) bestSolution.getCost(); result[1] = (float) bestSolution.getRoutes().iterator().next().getEnd().getArrTime();
		}
		cache(indiceContenedores,result,sumi);
		return result;
	}
	
	
	private void cache(int[] i ,float[] r, int sum) {
		if(cache.size()>MAX_CACHE) {
			Map<Integer,List<DtSol>> c2 = new Hashtable<Integer,List<DtSol>>();
			int n=0;
			for(Entry<Integer, List<DtSol>> x: cache.entrySet()) {
				c2.put(x.getKey(), x.getValue());
				if(++n > cache.size()/2)
					break;
			}
			cache = null;
			cache = c2;
		}
		int hash = Arrays.hashCode(i);
		if(cache.containsKey(hash))
			cache.get(hash).add(new DtSol(i,r));
		else {
			List<DtSol> l = new CopyOnWriteArrayList<>();
			l.add(new DtSol(i,r));
			cache.put(hash, l);
		}
	}
	
	/**
	 * Construye la matriz de tiempo y de costos. Las cuales debieron de setearse previamente.
	 * @return la instancia TSPSolver sobre la que se ejecutó la operación
	 */
	public TSPSolver buildcostMatrix() {
        VehicleRoutingTransportCostsMatrix.Builder costMatrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);
        for (int i=0; i<tiempo.length; i++)
        	for (int j=0; j<tiempo.length; j++) {
        		costMatrixBuilder.addTransportDistance(String.valueOf(i), String.valueOf(j), distancia[i][j]);
        		costMatrixBuilder.addTransportTime(String.valueOf(i), String.valueOf(j), tiempo[i][j]);
        	}
        for (int i=0; i<tiempo.length; i++) {
    		costMatrixBuilder.addTransportDistance(String.valueOf(i), "startpoint", distanciaToStartpoint[i]);
    		costMatrixBuilder.addTransportDistance("startpoint", String.valueOf(i), distanciaFromStartpoint[i]);
    		costMatrixBuilder.addTransportTime("startpoint", String.valueOf(i), tiempoFromStartpoint[i]);
    		costMatrixBuilder.addTransportTime(String.valueOf(i), "startpoint", tiempoToStartpoint[i]);
        }
        costMatrix = costMatrixBuilder.build();
        zhash = Arrays.hashCode(new int[tiempo.length]);
		return this;
	}
	
	/**
	 * Construye los camiones de basura con los parametros configurados.
	 * @return la instancia TSPSolver sobre la que se ejecutó la operación
	 */
	public TSPSolver buildTrucks() {
		VehicleTypeImpl.Builder templateCamionDeBasura = VehicleTypeImpl.Builder.newInstance("camionDeBasuraA01")
				.addCapacityDimension(0, CAPACIDAD_MAXIMA)
				.setCostPerDistance(COSTO_POR_DISTANCIA)
				.setCostPerTransportTime(COSTO_POR_TIEMPO)
				.setFixedCost(COSTO_FIJO);
		VehicleType camionDeBasura = templateCamionDeBasura.build();
		VehicleImpl.Builder fabricaCamiones = VehicleImpl.Builder.newInstance("camionMontevideo")
				.setStartLocation(startpoint)
				.setEndLocation(startpoint)
				.setReturnToDepot(true)
				.setEarliestStart(0)
				.setLatestArrival(MAX_TIME)
				.setType(camionDeBasura);	
		camion = fabricaCamiones.build();
		return this;
	}

	/**
	 * Setea la ubiciación de los contenedores.
	 * @param positions: posición de los contenedores en el mapa
	 * @return la instancia TSPSolver sobre la que se ejecutó la operación
	 */
	public TSPSolver setPositions(float [][] positions) {
		this.positions = positions;
		return this;
	}
	
	/**
	 * Setea el costo de tiempo de viaje entre contenedores.
	 * @param tiempo: costo de tiempo contenedor a contenedor
	 * @return la instancia TSPSolver sobre la que se ejecutó la operación
	 */
	public TSPSolver setTiempo(float [][] tiempo) {
		this.tiempo = tiempo;
		return this;
	}
	
	/**
	 * Setea el costo de distancia de viaje entre contenedores.
	 * @param distancia: costo de distancia contenedor a contenedor
	 * @return la instancia TSPSolver sobre la que se ejecutó la operación
	 */	
	public TSPSolver setDistancia(float [][] distancia) {
		this.distancia = distancia;
		return this;
	}
	
	/**
	 * Setea la cantidad de contenedores que puede levantar un camión
	 * @param  capMax: capacidad máxima de los camiones
	 * @return la instancia TSPSolver sobre la que se ejecutó la operación
	 */	
	public TSPSolver setCapacidadCamiones(int capMax) {
		this.CAPACIDAD_MAXIMA = capMax;
		return this;
	}

	/**
	 * Setea el costo de tiempo de viaje entre contenedores y el basurero
	 * @param tiempotoStartpoint: costo de tiempo de los contenedores al basurero
	 * @return la instancia TSPSolver sobre la que se ejecutó la operación
	 */	
	public TSPSolver setTiempotoStartpoint(float [] tiempotoStartpoint) {
		this.tiempoToStartpoint = tiempotoStartpoint;
		return this;
	}

	
	/**
	 * Setea el costo de tiempo de viaje entre el basurero y contenedores.
	 * @param tiempoFromStartpoint: costo de tiempotiempo del basurero al contenedor
	 * @return la instancia TSPSolver sobre la que se ejecutó la operación
	 */	
	public TSPSolver setTiempoFromStartpoint(float [] tiempoFromStartpoint) {
		this.tiempoFromStartpoint = tiempoFromStartpoint;
		return this;
	}
	
	/**
	 * Setea el costo de distancia de viaje entre contenedores y el basurero
	 * @param distanciatoStartpoint: costo de distancia de los contenedores al basurero
	 * @return la instancia TSPSolver sobre la que se ejecutó la operación
	 */	
	public TSPSolver setDistanciatoStartpoint(float [] distanciatoStartpoint) {
		this.distanciaToStartpoint = distanciatoStartpoint;
		return this;
	}
	
	/**
	 * Setea el costo de distancia de viaje entre el basurero y contenedores.
	 * @param distanciaFromStartpoint: costo de distancia del basurero al contenedor
	 * @return la instancia TSPSolver sobre la que se ejecutó la operación
	 */	
	public TSPSolver setDistanciaFromStartpoint(float [] distanciaFromStartpoint) {
		this.distanciaFromStartpoint = distanciaFromStartpoint;
		return this;
	}
	
	/**
	 * Setea la ubicación del basurero
	 * @param x: coordenada
	 * @param y: coordenada
	 * @return la instancia TSPSolver sobre la que se ejecutó la operación
	 */
	public TSPSolver setStartpoint(int x,int y) {
		this.startpoint =  Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(x,y))
				.setId("startpoint").build();
		return this;
	}
	
	/**
	 * Setea el tiempo maximo de ejecuciíón del algoritmo.
	 * @param time: tiempo máximo
	 * @return instancia TSPSolver sobre la que se ejecutó la operación
	 */
	public TSPSolver setTimeTermination(int time) {
		this.timeTermination = new TimeTermination(time);
		return this;
	}
	
	/**
	 * Setea la variación entre soluciones como criterio de parada del algoritmo
	 */
	public TSPSolver setCoefTermiantion(int iterations, float variance) {
		this.coefTermination = new VariationCoefficientTermination(iterations, variance); 
		return this;
	}


	public PrematureAlgorithmTermination getCoefTermination() {
		return coefTermination;
	}


	public PrematureAlgorithmTermination getTimeTermination() {
		return timeTermination;
	}
}



/*
 * 
	/* Funcion de ejemplo para resolver un problema TSP básico.
	private static void simpleExample() {
		//Define el tipo de vehiculo
		VehicleTypeImpl.Builder vehicleTypeBuilder = VehicleTypeImpl.Builder.newInstance("camionDeBasura");
		VehicleType camionDeBasura = vehicleTypeBuilder.build();
		
		//Construye un vehiculo concreto
		VehicleImpl.Builder vehicleBuilder = VehicleImpl.Builder.newInstance("vehicle");
		vehicleBuilder.setStartLocation(Location.newInstance(0,0));
		vehicleBuilder.setType(camionDeBasura);
		VehicleImpl camion1 = vehicleBuilder.build();
		
		//Ubicar los contenedores
		Service c1 = Service.Builder.newInstance("1").setLocation(Location.newInstance(0,0)).build();
		Service c2 = Service.Builder.newInstance("2").setLocation(Location.newInstance(5, 13)).build();
		Service c3 = Service.Builder.newInstance("3").setLocation(Location.newInstance(15, 7)).build();
		Service c4 = Service.Builder.newInstance("4").setLocation(Location.newInstance(15, 13)).build();
		
		//Definir el problema
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		vrpBuilder.addVehicle(camion1);
		vrpBuilder.addJob(c1).addJob(c2).addJob(c3).addJob(c4);
		vrpBuilder.setFleetSize(FleetSize.FINITE);
		VehicleRoutingProblem problem = vrpBuilder.build();
		
		//Resolver el problema
		VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(problem);
		Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
		VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);
		
		//Mostar resultados
		SolutionPrinter.print(problem, bestSolution, Print.CONCISE);
		new GraphStreamViewer(problem, bestSolution).setRenderDelay(100).display();
		new Plotter(problem,bestSolution).plot("solution.png", "solution");
	}
	
*/
