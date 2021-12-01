package main;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Greedy {
	
	private static int cantidadContenedores;
	private static int cantidadCamiones;
	private final static int diasMaxSinLevantar = 2;
	private static int [] basuraInicialContenedores;

	
	public final static int CAPACIDAD_MAXIMA = 200; //Cantidad maxima de residuos que se pueden levantar.
	public final int COSTO_POR_DISTANCIA = 1; //Costo por metro recorrido
	public final float COSTO_POR_TIEMPO = 0;//00.001f; // Costo por segundo de transporte (En milisegundos)
	public final int COSTO_FIJO = 0;//100; //Costo fijo por utilizar el camion
	public final double MAX_TIME = 2880000000.0*1000; //8hs (En ms)
	public final double TIEMPOXCONTENEDOR = 60*3*1000; //Tiempo que se permanece en cada contenedor (en ms).
	
	private float [][] tiempo;
	private static float [][] distancia;
	private float [] tiempoToStartpoint;
	private float [] tiempoFromStartpoint;
	private static float [] distanciaToStartpoint;
	private static float [] distanciaFromStartpoint;
	
	
	public static float solve() { // devuelve la distancia recorrida por los camiones en el greedy
		float distanciaRecorrida = 0;
		float dis = 0;
		Set<Integer> contOb = new HashSet<Integer>();  // contenedores que hay que levantar hoy
		Set<Integer> contNoOb = new HashSet<Integer>(); // contenedores que no hay por que levantar 

		for (int j = 0 ; j < diasMaxSinLevantar; j++) { // para cada día
			int[] basuraContenedores = basuraInicialContenedores;
			for (int z=0 ; z < cantidadContenedores ; z++) { // se agregan a contOb los contenedores que hay que levantar este dia
				basuraContenedores[z]++;
				if (basuraContenedores[z] == diasMaxSinLevantar) {
					contOb.add(z);
				} else {
					contNoOb.add(z);
				}
			}
			for (int i = 1 ; i <= 2 ; i++) { // para cada turno
				int camAct = 0; // camión actual
				int contAct = -1; // contenedor actual (arranca en el vertedero)
				int capAct = 0 ; // cuantos contenedores va levantando el camión actual
				
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
					} else { // estoy en un contenedor
						Iterator<Integer> it = contOb.iterator(); 
						dis = Float.POSITIVE_INFINITY; //  calcular distancia mínima desde el contenedor actual
						int cont = 0;
						while (it.hasNext()) {
							int act = it.next();
							if (distancia[contAct][act] < dis) {
								cont = act;
								dis = distancia[contAct][act];
							}
							contAct = cont;
						}
					}
					contOb.remove(contAct);
					basuraContenedores[contAct] = 0; // el contenedore fue vaciado
					distanciaRecorrida += dis;
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
					} else { // estoy en un contenedor
						Iterator<Integer> it = contNoOb.iterator(); 
						dis = Float.POSITIVE_INFINITY; //  calcular distancia mínima desde el contenedor actual
						int cont = 0;
						while (it.hasNext()) {
							int act = it.next();
							if (distancia[contAct][act] < dis) {
								cont = act;
								dis = distancia[contAct][act];
							}
							contAct = cont;
						}
					}
					contNoOb.remove(contAct);
					basuraContenedores[contAct] = 0; // el contenedore fue vaciado
					distanciaRecorrida += dis;
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
		return distanciaRecorrida;
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
	
}