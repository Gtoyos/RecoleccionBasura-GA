package main;

import java.io.IOException;

import com.graphhopper.jsprit.analysis.toolbox.GraphStreamViewer;
import com.graphhopper.jsprit.analysis.toolbox.Plotter;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter.Print;

public class Main {

	public static void main(String[] args) {
		TSPSolver s = new TSPSolver();
		try {
			System.out.print("Loading matrix data... ");
			long startTime = System.nanoTime();
			s.setPositions(MatrixLoader.readCSV("data/coordenadasContenedores.csv"));
			s.setDistancia(MatrixLoader.readCSV("data/ContenedoresGraphHopper/distancia.csv"));
			s.setTiempo(MatrixLoader.readCSV("data/ContenedoresGraphHopper/tiempo.csv"));
			s.setTiempoFromStartpoint(MatrixLoader.readCSV("data/ContenedoresGraphHopper/tiempoDesdeCardozo.csv")[0]);
			s.setTiempotoStartpoint(MatrixLoader.readCSV("data/ContenedoresGraphHopper/tiempoHaciaCardozo.csv")[0]);
			s.setDistanciaFromStartpoint(MatrixLoader.readCSV("data/ContenedoresGraphHopper/distanciaDesdeCardozo.csv")[0]);
			s.setDistanciatoStartpoint(MatrixLoader.readCSV("data/ContenedoresGraphHopper/distanciaHaciaCardozo.csv")[0]);
			long endTime = System.nanoTime();
			long duration = (endTime - startTime)/1000000;
			System.out.println("DONE ["+duration/1000.0+" s]");
		} catch(IOException e) {
			e.printStackTrace();
		}
		int [] index = new int[100];
		for(int i=0;i<50;i++)
			index[i++] = i;

		System.out.print("Calculating route... ");
		long startTime = System.nanoTime();
		s.solve(index,false);
		long endTime = System.nanoTime();
		long duration = (endTime - startTime)/1000000;
		System.out.println("DONE ["+duration/1000.0+" s]");
	}

}
