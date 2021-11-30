package main;

import java.util.Collection;
import java.util.Iterator;

import com.graphhopper.jsprit.analysis.toolbox.GraphStreamViewer;
import com.graphhopper.jsprit.analysis.toolbox.Plotter;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
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


public class TSPSolver {
	public final int CAPACIDAD_MAXIMA = 200; //Cantidad maxima de residuos que se pueden levantar.
	public final int COSTO_POR_DISTANCIA = 2; //Costo por metro recorrido
	public final float COSTO_POR_TIEMPO = 0.001f; // Costo por segundo de transporte (En milisegundos)
	public final int COSTO_FIJO = 100; //Costo fijo por utilizar el camion
	public final double MAX_TIME = 2880000000.0*1000; //8hs (En ms)
	public final double TIEMPOXCONTENEDOR = 60*3*1000; //Tiempo que se permanece en cada contenedor (en ms).
	public final Location felipe_cardozo =  Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(-56.0982134,-34.8504341))
			.setId("startpoint").build();
	private VehicleImpl camion;
	private Location startpoint = felipe_cardozo;
	private float [][] tiempo;
	private float [][] distancia;
	private float [][] positions;
	private float [] tiempoToStartpoint;
	private float [] tiempoFromStartpoint;
	private float [] distanciaToStartpoint;
	private float [] distanciaFromStartpoint;
	TSPSolver(){
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
	}
	
    /**
     * Calcula la ruta optima para levatar los contenedores 
     * <p>Todas las matrices del problema deben estar cargadas antes de poder ejecutar esta función
     *
     * @param plotResults true para imprimir los resultados
     * @param indiceContenedores contiene los contenedores que se quieren levantar segun su posicion en la matriz positions
     * @return retorna float[] donde la primer entrada es la distancia recorrida en metros del recorrido y la segunda entrada es el tiempo empleado para hacerlo
     */
	public float [] solve(int [] indiceContenedores,boolean plotResults){

        VehicleRoutingTransportCostsMatrix.Builder costMatrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);
        for (int i=0; i<indiceContenedores.length; i++)
        	for (int j=0; j<indiceContenedores.length; j++) {
        		costMatrixBuilder.addTransportDistance(String.valueOf(i), String.valueOf(j), distancia[indiceContenedores[i]][indiceContenedores[j]]);
        		costMatrixBuilder.addTransportTime(String.valueOf(i), String.valueOf(j), tiempo[indiceContenedores[i]][indiceContenedores[j]]);
        	}
        for (int i=0; i<indiceContenedores.length; i++) {
    		costMatrixBuilder.addTransportDistance(String.valueOf(i), "startpoint", distanciaToStartpoint[indiceContenedores[i]]);
    		costMatrixBuilder.addTransportDistance("startpoint", String.valueOf(i), distanciaFromStartpoint[indiceContenedores[i]]);
    		costMatrixBuilder.addTransportTime("startpoint", String.valueOf(i), tiempoFromStartpoint[indiceContenedores[i]]);
    		costMatrixBuilder.addTransportTime(String.valueOf(i), "startpoint", tiempoToStartpoint[indiceContenedores[i]]);
        }

        VehicleRoutingTransportCosts costMatrix = costMatrixBuilder.build();
		
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance()
				.addVehicle(camion)
				.setFleetSize(FleetSize.FINITE)
				.setRoutingCost(costMatrix);
		for(int i=0;i<indiceContenedores.length;i++) {
			Location l =  Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(positions[indiceContenedores[i]][0],
					positions[indiceContenedores[i]][1]))
					.setId(String.valueOf(i)).build();
			vrpBuilder.addJob(Service.Builder.newInstance(String.valueOf(i)).setLocation(l).setServiceTime(TIEMPOXCONTENEDOR).build());
		}
		VehicleRoutingProblem problem = vrpBuilder.build();
		

		VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(problem);
		Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
		VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);
		
		if(plotResults) {
			SolutionPrinter.print(problem, bestSolution, Print.VERBOSE);
			new GraphStreamViewer(problem, bestSolution).setRenderDelay(100).display();
			new Plotter(problem,bestSolution).plot("solution.png", "solution");
		}

		float tDistance=0,tTime=0;
		for(VehicleRoute route : bestSolution.getRoutes()) {
			Iterator<TourActivity> it = route.getActivities().iterator();
			TourActivity a = it.next();
			tDistance+=costMatrix.getDistance(route.getStart().getLocation(),a.getLocation(),route.getDepartureTime(),camion);
			while(it.hasNext()) {
				TourActivity b = it.next();
				tDistance+=costMatrix.getDistance(a.getLocation(),b.getLocation(),a.getEndTime(),camion);
				a = b;
				if(!it.hasNext()) {
					tDistance+=costMatrix.getDistance(a.getLocation(),route.getEnd().getLocation(),a.getEndTime(),camion);
				}
			}
			tTime = (float) route.getEnd().getArrTime();
		}
		
		float [] result = {tDistance,tTime};
		return result;
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
		Service c1 = Service.Builder.newInstance("1").setLocation(Location.newInstance(-5.608671211000000056e+01,-3.479861317000000298e+01)).build();
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
	
	
	
	
	
	public static void main(String[] args) {
		simpleExample();
	}

	public void setPositions(float [][] positions) {
		this.positions = positions;
	}
	public void setTiempo(float [][] tiempo) {
		this.tiempo = tiempo;
	}
	public void setDistancia(float [][] distancia) {
		this.distancia = distancia;
	}

	public void setTiempotoStartpoint(float [] tiempotoStartpoint) {
		this.tiempoToStartpoint = tiempotoStartpoint;
	}

	public void setTiempoFromStartpoint(float [] tiempoFromStartpoint) {
		this.tiempoFromStartpoint = tiempoFromStartpoint;
	}

	public void setDistanciatoStartpoint(float [] distanciatoStartpoint) {
		this.distanciaToStartpoint = distanciatoStartpoint;
	}

	public void setDistanciaFromStartpoint(float [] distanciaFromStartpoint) {
		this.distanciaFromStartpoint = distanciaFromStartpoint;
	}

	public void setStartpoint(Location startpoint) {
		this.startpoint = startpoint;
	}
}
