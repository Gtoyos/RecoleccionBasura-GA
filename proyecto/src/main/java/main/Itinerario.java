package main;

import java.util.concurrent.ThreadLocalRandom;

import org.uma.jmetal.util.binarySet.BinarySet;

public class Itinerario extends BinarySet{
	private static final long serialVersionUID = 4335200616163764698L;
	private int cantCamiones,cantContenedores,cantDias;
	
	private double fitness=-1;
	private float distancia=-1;
	private float tiempo=-1;
	private long computingTime=-1;
	private boolean hayDesborde = false;
	
	public Itinerario(BinarySet b,int cantCamiones, int cantContenedores, int cantDias) {
		super(b.getBinarySetLength());
		for(int i=0; i<b.getBinarySetLength();i++)
			this.set(i, b.get(i));
		this.cantCamiones = cantCamiones;
		this.cantContenedores = cantContenedores;
		this.cantDias = cantDias;
	}
	
	public Itinerario(int numberOfBits, int cantCamiones, int cantContenedores, int cantDias) {
		super(numberOfBits);
		this.cantCamiones = cantCamiones;
		this.cantContenedores = cantContenedores;
		this.cantDias = cantDias;
	}

	public void set(int camion, int contenedor, int dia, int turno, boolean valor) {
		this.set( (dia*2+turno)*cantCamiones*cantContenedores + cantContenedores*camion + contenedor, valor);
	}
	
	public int get(int camion, int contenedor, int dia, int turno) {
		return this.get( (dia*2+turno)*cantCamiones*cantContenedores + cantContenedores*camion + contenedor) ? 1:0;
	}
	
	public int[] getContenedores(int camion, int dia, int turno) {
		int [] res = new int[cantContenedores];
		for(int i=0; i<cantContenedores; i++) {
			res[i] = this.get((2*dia+turno)*cantCamiones*cantContenedores + camion*cantContenedores + i) ? 1:0;
		}
		return res;
	}

	public int[] getContenedoresLevantadosEnElDia(int dia) {
		int [] res = new int[cantContenedores];
		for(int cam=0; cam < cantCamiones; cam++)
			for(int contenedor=0; contenedor<cantContenedores; contenedor++)
				res[contenedor] = (res[contenedor]==1 ||
				this.get( (2*dia+0)*cantCamiones*cantContenedores + cantContenedores*cam + contenedor) ||
				this.get( (2*dia+1)*cantCamiones*cantContenedores + cantContenedores*cam + contenedor)) ? 1:0;
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
		return stream;
	}

	public float getDistancia() {
		return distancia;
	}

	public Itinerario setDistancia(float distancia) {
		this.distancia = distancia;
		return this;
	}

	public boolean hayDesborde() {
		return hayDesborde;
	}

	public Itinerario setHayDesborde(boolean hayDesborde) {
		this.hayDesborde = hayDesborde;
		return this;
	}

	public float getTiempo() {
		return tiempo;
	}

	public void setTiempo(float tiempo) {
		this.tiempo = tiempo;
	}
	
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
