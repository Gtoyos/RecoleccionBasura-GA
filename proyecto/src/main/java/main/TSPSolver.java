package main;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.IntStream;

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
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter.Print;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;


public class TSPSolver implements Serializable{

	private static final long serialVersionUID = -5979648570893638696L;
	public int CAPACIDAD_MAXIMA = 100; //Cantidad maxima de residuos que se pueden levantar.
	public final int COSTO_POR_DISTANCIA = 1; //Costo por metro recorrido
	public final float COSTO_POR_TIEMPO = 0;//00.001f; // Costo por segundo de transporte (En milisegundos)
	public final int COSTO_FIJO = 0;//100; //Costo fijo por utilizar el camion
	public final double MAX_TIME = 1000*60*60*24; //Cota maxima preventiva de 24hs.
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
	
	private static Map<Integer,List<DtSol>> cache = new Hashtable<Integer,List<DtSol>>();

	TSPSolver(){ }
	
    /**
     * Calcula la ruta optima para levatar los contenedores 
     * <p>Todas las matrices del problema deben estar cargadas antes de poder ejecutar esta función
     * 
     * @param plotResults true para imprimir los resultados
     * @param indiceContenedores contiene los contenedores que se quieren levantar segun su posicion en la matriz positions
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
		int hash = Arrays.hashCode(i);
		if(cache.containsKey(hash))
			cache.get(hash).add(new DtSol(i,r));
		else {
			List<DtSol> l = new ArrayList<>();
			l.add(new DtSol(i,r));
			cache.put(hash, l);
		}
	}
	
	

	/* Funcion de ejemplo para resolver un problema TSP básico. */
	public static void simpleExample() {
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
	
	public static void main(String[] args) {
		simpleExample();
	}

	public TSPSolver setPositions(float [][] positions) {
		this.positions = positions;
		return this;
	}
	public TSPSolver setTiempo(float [][] tiempo) {
		this.tiempo = tiempo;
		return this;
	}
	public TSPSolver setDistancia(float [][] distancia) {
		this.distancia = distancia;
		return this;
	}

	public TSPSolver setCapacidadCamiones(int capMax) {
		this.CAPACIDAD_MAXIMA = capMax;
		return this;
	}
	public TSPSolver setTiempotoStartpoint(float [] tiempotoStartpoint) {
		this.tiempoToStartpoint = tiempotoStartpoint;
		return this;
	}

	public TSPSolver setTiempoFromStartpoint(float [] tiempoFromStartpoint) {
		this.tiempoFromStartpoint = tiempoFromStartpoint;
		return this;
	}

	public TSPSolver setDistanciatoStartpoint(float [] distanciatoStartpoint) {
		this.distanciaToStartpoint = distanciatoStartpoint;
		return this;
	}

	public TSPSolver setDistanciaFromStartpoint(float [] distanciaFromStartpoint) {
		this.distanciaFromStartpoint = distanciaFromStartpoint;
		return this;
	}
	
	public TSPSolver setStartpoint(int x,int y) {
		this.startpoint =  Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(x,y))
				.setId("startpoint").build();
		return this;
	}
	
	public TSPSolver setTimeTermination(int time) {
		this.timeTermination = new TimeTermination(time);
		return this;
	}
	public TSPSolver setCoefTermiantion(int iterations, float variance) {
		this.coefTermination = new VariationCoefficientTermination(iterations, variance); 
		return this;
	}
}

