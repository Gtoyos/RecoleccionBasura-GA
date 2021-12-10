package main;

import java.util.concurrent.ThreadLocalRandom;

import org.uma.jmetal.util.binarySet.BinarySet;

/**
 * Extiende la implementación de la binary solution de JMetal para brindar información enriquecida y editar la solucion de forma más
 * sencilla en el algoritmo.
 * @author Toyos, Vallcorba
 *
 */
public class Itinerario extends BinarySet{
	private static final long serialVersionUID = 4335200616163764698L;
	
	private int cantCamiones,cantContenedores,cantDias;
	
	private double fitness=-1;
	private float distancia=-1;
	private float tiempo=-1;
	private long computingTime=-1;
	private int hayDesborde = 0;
	
	/**\
	 * Constructor por copia. 
	 * @param cantCamiones: filas por matriz de la solución.
	 * @param cantContenedores: columnas según la matriz de resolución.
	 * @param cantDias: cantidad máxima de dias en las que se puede dejar un contenedor sin atender. 
	 * @param b: Arreglo bibario a copiar.
	 */
	public Itinerario(BinarySet b,int cantCamiones, int cantContenedores, int cantDias) {
		super(b.getBinarySetLength());
		for(int i=0; i<b.getBinarySetLength();i++)
			this.set(i, b.get(i));
		this.cantCamiones = cantCamiones;
		this.cantContenedores = cantContenedores;
		this.cantDias = cantDias;
	}
	/**\
	 * Constructor. Retorna un arreglo de tamaño cantCamiones*cantContenedores*cantDias*2 
	 * @param numberOfBits: tamaño de la matriz en bits.
	 * @param cantCamiones: filas por matriz de la solución.
	 * @param cantContenedores: columnas según la matriz de resolución.
	 * @param cantDias: cantidad máxima de dias en las que se puede dejar un contenedor sin atender. 
	 */	
	public Itinerario(int numberOfBits, int cantCamiones, int cantContenedores, int cantDias) {
		super(numberOfBits);
		this.cantCamiones = cantCamiones;
		this.cantContenedores = cantContenedores;
		this.cantDias = cantDias;
	}
	
	/** Edita el valor en el itinerario para  el camión n, contenedor m el díá d para el turno matutino y nocturno.
	 *  Se edita la posición: (dia*2+turno)*cantCamiones*cantContenedores + cantContenedores*camion + contenedor
	 * @param camion: index del camion
	 * @param contenedor: index del contenedor
	 * @param dia: dia del itinerario
	 * @param turno: matutino=0, nocturno=1
	 * @param valor: valor a settear
	 */
	public void set(int camion, int contenedor, int dia, int turno, boolean valor) {
		this.set( (dia*2+turno)*cantCamiones*cantContenedores + cantContenedores*camion + contenedor, valor);
	}
	
	/** Obtiene el valor en el itinerario para  el camión n, contenedor m el díá d para el turno matutino y nocturno.
	 *  Se edita la posición: (dia*2+turno)*cantCamiones*cantContenedores + cantContenedores*camion + contenedor
	 */
	public int get(int camion, int contenedor, int dia, int turno) {
		return this.get( (dia*2+turno)*cantCamiones*cantContenedores + cantContenedores*camion + contenedor) ? 1:0;
	}
	
	/**
	 * Devuelve los contenedores levantados por un camion en un dia y turno especificos

	 * @return arreglo de enteros indicando si el camión levantara algúmn contenedor de basura en este momento.
	 */
	public int[] getContenedores(int camion, int dia, int turno) {
		int [] res = new int[cantContenedores];
		for(int i=0; i<cantContenedores; i++) {
			res[i] = this.get((2*dia+turno)*cantCamiones*cantContenedores + camion*cantContenedores + i) ? 1:0;
		}
		return res;
	}

	/**
	 * Devuelve los contenedores levantados en el día
	 * @param dia: dia del itinerario
	 * @return arreglo de enteros indicando los contenedores que se levantaran en el dia
	 */
	public int[] getContenedoresLevantadosEnElDia(int dia) {
		int [] res = new int[cantContenedores];
		for(int cam=0; cam < cantCamiones; cam++)
			for(int contenedor=0; contenedor<cantContenedores; contenedor++)
				res[contenedor] = (res[contenedor]==1 ||
				this.get( (2*dia+0)*cantCamiones*cantContenedores + cantContenedores*cam + contenedor) ||
				this.get( (2*dia+1)*cantCamiones*cantContenedores + cantContenedores*cam + contenedor)) ? 1:0;
		return res;
	}

	public Itinerario setFit(double objective) {
		this.fitness = objective;
		return this;
	}

	public Itinerario setComp(long computingTime) {
		this.computingTime = computingTime;
		return this;
	}

	public long getComp() {
		return computingTime;
	}

	public double getFit() {
		return fitness;
	}

	/**
	 * Imprime el itinerario.
	 * 
	 */
	@Override
	public String toString() {
		String stream = "\n";
		for(int d=0; d<cantDias; d++) {
			stream += "Dia " + (d+1) + "\n";
			for(int cam=0; cam< cantCamiones; cam++) {
				for(int c=0; c<cantContenedores; c++)
					stream+=this.get((2*d+0)*cantCamiones*cantContenedores + cantContenedores*cam + c) ? 1:0;
				stream+="\t";
				for(int c=0; c<cantContenedores; c++)
					stream+=this.get((2*d+1)*cantCamiones*cantContenedores + cantContenedores*cam + c) ? 1:0;
				stream += "\n";
			}
			stream+= "\n";
		}
		stream+="fitness: "+fitness+"\n";
		stream+="distancia total: "+distancia+"\n";
		stream+="tiempo total: "+tiempo+"\n";
		stream+="desborde: "+hayDesborde+"\n";
		stream+="computo: "+computingTime+"\n";
		return stream;
	}
	/**
	 * Imprime la metadata del itinerario.
	 * @return string que contiene la metadata del itinerario en formato key: val \n
	 */
	public String getResults() {
		String stream = "";
		stream+="fitness: "+fitness+"\n";
		stream+="distancia total: "+distancia+"\n";
		stream+="tiempo total: "+tiempo+"\n";
		stream+="desborde: "+hayDesborde+"\n";
		stream+="computo: "+computingTime+"\n";
		return stream;
	}
	
	public float getDistancia() {
		return distancia;
	}

	public Itinerario setDistancia(float distancia) {
		this.distancia = distancia;
		return this;
	}

	public int hayDesborde() {
		return hayDesborde;
	}

	public Itinerario setHayDesborde(int hayDesborde) {
		this.hayDesborde = hayDesborde;
		return this;
	}

	public float getTiempo() {
		return tiempo;
	}

	public void setTiempo(float tiempo) {
		this.tiempo = tiempo;
	}
	
	/**
	 * Intercambia los contenedores que se levantan en el día por los que se levantan en el turno nocturno.
	 */
	public void switchTurnos() {
		int tmp;
		for(int d=0; d<cantDias; d++) {
			for(int cam=0; cam< cantCamiones; cam++)
				for(int c=0; c<cantContenedores; c++) {
					tmp = this.get(cam, c, d, 0);
					this.set(cam, c, d, 0,this.get(cam,c,d,1)==1);
					this.set(cam, c, d, 1,tmp==1);
				}
		}
	}
	
	/**
	 * Intercambia los turnos de un itinerario moviendolo entre los distitntos dias. No asegura que el itinerario
	 * siga respetando los tiempos de recogida de los contenedores para evitar desborde.
	 */
	public void shuffleDiasTurnos() {
		int tmp;
		for(int t=0; t<2; t++) {
			for(int d=0; d<cantDias; d++) {
				int targetDia = ThreadLocalRandom.current().nextInt(0, cantDias);
				int targetTurno = ThreadLocalRandom.current().nextInt(0, 2);
				
				for(int cam=0; cam< cantCamiones; cam++)
					for(int c=0; c<cantContenedores; c++) {
						tmp = this.get(cam, c, d, t);
						this.set(cam, c, d, t,this.get(cam,c,targetDia,targetTurno)==1);
						this.set(cam, c, targetDia, targetTurno,tmp==1);
					}
			}
		}
	}
}
