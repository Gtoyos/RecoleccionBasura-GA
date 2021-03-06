package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Modulo de ejecución del algoritmo greedy presentado en el articulo.
 * Antes de ejecutar el greedy deben cargarse la ubicación de los contenedores
 * y las matrices de distancia/tiempo utilizando sus respectivos getters.
 * @author Toyos, Vallcorba
 *
 */
public class Greedy {
	
	private final static int diasMaxSinLevantar = 2;

	
	public final int COSTO_POR_DISTANCIA = 1; //Costo por metro recorrido
	public final float COSTO_POR_TIEMPO = 0;//00.001f; // Costo por segundo de transporte (En milisegundos)
	public final int COSTO_FIJO = 0;//100; //Costo fijo por utilizar el camion
	public final double MAX_TIME = 2880000000.0*1000; //8hs (En ms)
	public final double TIEMPOXCONTENEDOR = 60*3*1000; //Tiempo que se permanece en cada contenedor (en ms).
	
	
	private int CAPACIDAD_MAXIMA = 200; //Cantidad maxima de residuos que se pueden levantar.
	private int cantidadContenedores;
	private int cantidadCamiones;
	private int [] basuraInicialContenedores;
	
	private float [][] distancia;
	private float [] distanciaToStartpoint;
	private float [] distanciaFromStartpoint;
	
	private float [][] tiempo;
	private float [] tiempoToStartpoint;
	private float [] tiempoFromStartpoint;
	
	
    /**
     * Calcula un itinerario utilizando el algoritmo greedy presentado en el articulo
     * <p>Todas las matrices del problema deben estar cargadas antes de poder ejecutar esta función. Es decir, deben ejecutarse
     * los getters respectivos para cargar las matrices. </p>
     * @param contIni: primer contenedor al cual el camion viajará desde el basurero. Si contIni=-1, se tomará como primer destino el contenedor más cercano al basurero.
     * @return retorna float[] donde la primer entrada es la distancia recorrida en metros del recorrido y la segunda entrada es el tiempo empleado para hacerlo
     */	
	public Itinerario solve(int contIni) { 	
		Itinerario iti = new Itinerario(cantidadCamiones*cantidadContenedores*diasMaxSinLevantar*2, cantidadCamiones, cantidadContenedores, diasMaxSinLevantar);
		float distanciaRecorrida = 0;
		float dis = 0;
		int camAct = 0; // camión actual
		int contAct = -1; // contenedor actual (arranca en el vertedero)
		int capAct = 0 ; // cuantos contenedores va levantando el camión actual

		int [] levantados = new int[cantidadContenedores];
		List<Integer> contOb = new ArrayList<Integer>();  // contenedores que hay que levantar hoy
		List<Integer> contNoOb = new ArrayList<Integer>(); // contenedores que no hay por que levantar 
		int[] basuraContenedores = basuraInicialContenedores;
		for (int j = 0 ; j < diasMaxSinLevantar && !(Arrays.stream(levantados).sum()==cantidadContenedores); j++) { // para cada día
			contNoOb.clear();
			contOb.clear();
			for (int z=0 ; z < cantidadContenedores ; z++) { // se agregan a contOb los contenedores que hay que levantar este dia
				if(levantados[z]==1)
					continue;
				basuraContenedores[z]++;
				if (basuraContenedores[z] >= diasMaxSinLevantar) {
					contOb.add(z);
				} else {
					contNoOb.add(z);
				}
			}
			for (int i = 0 ; i < 2 && !(contOb.isEmpty() && contNoOb.isEmpty()) ; i++) { // para cada turno
				camAct = 0; 
				capAct = 0 ; 
				if(contIni == -1)
					contAct = -1; 
				else {
					
					if(!contOb.isEmpty()) {
						contAct =  ThreadLocalRandom.current().nextInt(0, contOb.size());
						contOb.remove(Integer.valueOf(contAct));
					}
					else {
						contAct =  ThreadLocalRandom.current().nextInt(0, contNoOb.size());
						contNoOb.remove(Integer.valueOf(contAct));
					}
					distanciaRecorrida += distanciaFromStartpoint[contAct];
					capAct++;
					basuraContenedores[contAct] = 0; // el contenedore fue vaciado
					levantados[contAct] = 1;
					iti.set(camAct, contAct, j, i, true);
				}
				
				while (!contOb.isEmpty() && camAct < cantidadCamiones) {
					if (contAct == -1) { // estoy en el vertedero
						Iterator<Integer> it = contOb.iterator();
						dis = Float.POSITIVE_INFINITY; //  calcular distancia mínima desde el vertedero
						while (it.hasNext()) {
							int act = it.next();
							if (distanciaFromStartpoint[act] < dis) {
								contAct = act;
								dis = distanciaFromStartpoint[act];
							}	
						}
						distanciaRecorrida += dis;
					} else { // estoy en un contenedor
						float [] d = new float[1];
						contAct = contMasCerca(contAct, contOb,d);
						distanciaRecorrida += d[0];
					}
					iti.set(camAct, contAct, j, i, true);
					levantados[contAct] = 1;
					contOb.remove(Integer.valueOf(contAct));
					basuraContenedores[contAct] = 0; // el contenedore fue vaciado
					
					capAct++;
					if (capAct == CAPACIDAD_MAXIMA) { // el camión actual no soporta más contenedores
						distanciaRecorrida += distanciaToStartpoint[contAct]; // hacer que vuelva al vertedero
						camAct++; // paso al siguiente camión
						contAct = -1; // el próximo arranca en el vertedero
						capAct = 0;
					}
				}
				while (!contNoOb.isEmpty() && camAct < cantidadCamiones) { // sigo haciendo lo mismo con el resto de los contenedores
				// capaz se puede hacer de una forma más inteligente, pero así te aseguras que no van a haber problemas en los días siguientes
					if (contAct == -1) { // estoy en el vertedero
						Iterator<Integer> it = contNoOb.iterator();
						dis = Float.POSITIVE_INFINITY; //  calcular distancia mínima desde el vertedero
						while (it.hasNext()) {
							int act = it.next();
							if (distanciaFromStartpoint[act] < dis) {
								contAct = act;
								dis = distanciaFromStartpoint[act];
							}	
						}
						distanciaRecorrida += dis;
					} else { // estoy en un contenedor
						float [] d = new float[1];						
						contAct = contMasCerca(contAct, contNoOb,d);
						distanciaRecorrida += d[0];
					}
					iti.set(camAct, contAct, j, i, true);
					levantados[contAct] = 1;
					contNoOb.remove(Integer.valueOf(contAct));
					basuraContenedores[contAct] = 0; // el contenedore fue vaciado

					capAct++;
					if (capAct == CAPACIDAD_MAXIMA) { // el camión actual no soporta más contenedores
						distanciaRecorrida += distanciaToStartpoint[contAct]; // hacer que vuelva al vertedero
						camAct++; // paso al siguiente camión
						contAct = -1; // el próximo arranca en el vertedero
						capAct =0;
					}
				}
				if (contAct != -1) { // si no estoy en el vertedero
					distanciaRecorrida += distanciaToStartpoint[contAct];
				}
			}
		}
		iti.switchTurnos();
		iti.setDistancia(distanciaRecorrida);
		return iti;
	}
	
	//calcular distancia mínima desde el contenedor actual
	private int contMasCerca(int contenedor, List<Integer> contOb, float [] dist) {
		float dis = Float.POSITIVE_INFINITY;
		int cont = 0;
		for(Integer i: contOb) {
			if (distancia[contenedor][i] < dis) {
				cont = i;
				dis = distancia[contenedor][i];
			}
		}
		dist[0] = dis;
		return cont;
	}
	
	
	public Greedy setTiempo(float [][] tiempo) {
		this.tiempo = tiempo;
		return this;
		
	}
	public Greedy setDistancia(float [][] distancia) {
		this.distancia = distancia;
		return this;
	}

	public Greedy setTiempotoStartpoint(float [] tiempotoStartpoint) {
		this.tiempoToStartpoint = tiempotoStartpoint;
		return this;
	}

	public Greedy setTiempoFromStartpoint(float [] tiempoFromStartpoint) {
		this.tiempoFromStartpoint = tiempoFromStartpoint;
		return this;
	}

	public Greedy setDistanciatoStartpoint(float [] distanciatoStartpoint) {
		this.distanciaToStartpoint = distanciatoStartpoint;
		return this;
	}

	public Greedy setDistanciaFromStartpoint(float [] distanciaFromStartpoint) {
		this.distanciaFromStartpoint = distanciaFromStartpoint;
		return this;
	}

	public Greedy setCantidadCamiones(int cantidadCamiones) {
		this.cantidadCamiones = cantidadCamiones;
		return this;
	}
	
	public Greedy setCAPACIDAD_MAXIMA(int CAPACIDAD_MAXIMA) {
		this.CAPACIDAD_MAXIMA = CAPACIDAD_MAXIMA;
		return this;
	}
	public Greedy setBasuraInicialContenedores(int [] basuraInicialContenedores) {
		this.basuraInicialContenedores = basuraInicialContenedores;
		this.cantidadContenedores = basuraInicialContenedores.length;
		return this;
	}

	public float [][] getTiempo() {
		return tiempo;
	}

	public float [] getTiempoToStartpoint() {
		return tiempoToStartpoint;
	}

	public float [] getTiempoFromStartpoint() {
		return tiempoFromStartpoint;
	}

}



/*
 * 						Iterator<Integer> it = contNoOb.iterator(); 
						dis = Float.POSITIVE_INFINITY; //  calcular distancia mínima desde el contenedor actual
						int cont = 0;
						while (it.hasNext()) {
							int act = it.next();
							if (distancia[contAct][act] < dis) {
								cont = act;
								dis = distancia[contAct][act];
							}
							contAct = cont;
						} */
