package main;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
	private int contIni;
	
	private float [][] tiempo;
	private float [][] distancia;
	private float [] tiempoToStartpoint;
	private float [] tiempoFromStartpoint;
	private float [] distanciaToStartpoint;
	private float [] distanciaFromStartpoint;
	
	
	public Itinerario solve(int xr) { 	
		Itinerario iti = new Itinerario(cantidadCamiones*cantidadContenedores*diasMaxSinLevantar*2, cantidadCamiones, cantidadContenedores, diasMaxSinLevantar);
		float distanciaRecorrida = 0;
		float dis = 0;
		int camAct = 0; // camión actual
		int contAct = -1; // contenedor actual (arranca en el vertedero)
		int capAct = 0 ; // cuantos contenedores va levantando el camión actual
		if (contIni != -1) {
			contAct = contIni;
			distanciaRecorrida += distanciaFromStartpoint[contAct];
			capAct++;
		}
		Set<Integer> contOb = new HashSet<Integer>();  // contenedores que hay que levantar hoy
		Set<Integer> contNoOb = new HashSet<Integer>(); // contenedores que no hay por que levantar 
		int [] levantados = new int[cantidadContenedores];
		levantados[contIni] = 1;
		
		for (int j = 0 ; j < diasMaxSinLevantar; j++) { // para cada día
			contNoOb.clear();
			int[] basuraContenedores = basuraInicialContenedores;
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
			for (int i = 0 ; i < 2 ; i++) { // para cada turno
				camAct = 0; 
				contAct = -1; 
				capAct = 0 ; 
				
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
					contOb.remove(contAct);
					basuraContenedores[contAct] = 0; // el contenedore fue vaciado
					
					capAct++;
					if (capAct == CAPACIDAD_MAXIMA) { // el camión actual no soporta más contenedores
						distanciaRecorrida += distanciaToStartpoint[contAct]; // hacer que vuelva al vertedero
						camAct++; // paso al siguiente camión
						contAct = -1; // el próximo arranca en el vertedero
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
					
					contNoOb.remove(contAct);
					basuraContenedores[contAct] = 0; // el contenedore fue vaciado

					capAct++;
					if (capAct == CAPACIDAD_MAXIMA) { // el camión actual no soporta más contenedores
						distanciaRecorrida += distanciaToStartpoint[contAct]; // hacer que vuelva al vertedero
						camAct++; // paso al siguiente camión
						contAct = -1; // el próximo arranca en el vertedero
					}
				}
				if (contAct != -1) { // si no estoy en el vertedero
					distanciaRecorrida += distanciaToStartpoint[contAct];
				}
			}
		}
		iti.setDistancia(distanciaRecorrida);
		return iti;
	}
	
	//calcular distancia mínima desde el contenedor actual
	private int contMasCerca(int contenedor, Set<Integer> contOb, float [] dist) {
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

	public Greedy setCantidadContenedores(int cantidadContenedores) {
		this.cantidadContenedores = cantidadContenedores;
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
		return this;
	}

	public Greedy setContIni(int contIni) {
		this.contIni = contIni;
		return this;
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
