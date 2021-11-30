package main;

import org.uma.jmetal.util.binarySet.BinarySet;

public class Itinerario extends BinarySet{
	private static final long serialVersionUID = 4335200616163764698L;
	private int cantCamiones,cantContenedores,cantDias;
	
	private double fitness;
	private long computingTime;
	
	public Itinerario(int numberOfBits, int cantCamiones, int cantContenedores, int cantDias) {
		super(numberOfBits);
		this.cantCamiones = cantCamiones;
		this.cantContenedores = cantContenedores;
		this.cantDias = cantDias;
	}

	public void set(int camion, int contenedor, int dia, int turno, boolean valor) {
		this.set( (dia+turno)*cantCamiones*cantContenedores + cantContenedores*camion + contenedor, valor);
	}
	
	public int get(int camion, int contenedor, int dia, int turno, boolean valor) {
		return this.get( (dia+turno)*cantCamiones*cantContenedores + cantContenedores*camion + contenedor) ? 1:0;
	}
	
	public int[] getContenedores(int camion, int dia, int turno) {
		int [] res = new int[cantContenedores];
		for(int i=0; i<cantContenedores; i++) {
			res[i] = this.get((dia+turno)*cantCamiones*cantContenedores + i) ? 1:0;
		}
		return res;
	}

	public int[] getContenedoresLevantadosEnElDia(int dia) {
		int [] res = new int[cantContenedores];
		for(int cam=0; cam < cantCamiones; cam++)
			for(int contenedor=0; contenedor<cantContenedores; contenedor++)
				res[contenedor] = (res[contenedor]==1 ||
				this.get( (dia+0)*cantCamiones*cantContenedores + cantContenedores*cam + contenedor) ||
				this.get( (dia+1)*cantCamiones*cantContenedores + cantContenedores*cam + contenedor)) ? 1:0;
		return res;
	}

	public Itinerario setFitness(double objective) {
		this.fitness = objective;
		return this;
	}

	public Itinerario setComputingTime(long computingTime) {
		this.computingTime = computingTime;
		return this;
	}

	public long getComputingTime() {
		return computingTime;
	}

	public double getFitness() {
		return fitness;
	}

	@Override
	public String toString() {
		String stream = "\n";
		for(int d=0; d<cantDias; d++) {
			stream += "Dia " + (d+1) + "\n";
			for(int cam=0; cam< cantCamiones; cam++) {
				for(int c=0; c<cantContenedores; c++)
					stream+=this.get((d+0)*cantCamiones*cantContenedores + cantContenedores*cam + c) ? 1:0;
				stream+="\t";
				for(int c=0; c<cantContenedores; c++)
					stream+=this.get((d+1)*cantCamiones*cantContenedores + cantContenedores*cam + c) ? 1:0;
				stream += "\n";
			}
			stream+= "\n";
		}
		
		return stream;
	}
}
