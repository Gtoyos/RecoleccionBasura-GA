package main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class MatrixLoader {

	public static float[][] readCSV(String url) throws IOException{
		List<Float[]> rowList = new ArrayList<Float[]>();
		try (Stream<String> stream = Files.lines(Paths.get(url))) {
	        stream.forEach(l -> {
	        	String[] lineItems = l.split(","); 	
	        	rowList.add(Arrays.stream(lineItems).map(Float::valueOf).toArray(Float[]::new));
	        });
		}
		float [][] res = new float[rowList.size()][];
		int j=0;
		for (Float [] ff: rowList) {
			int i=0;
			float[] niceFloat = new float[ff.length];
			for(Float f : ff)
				niceFloat[i++] = f.floatValue();
			res[j++] = niceFloat;
		}
		return res;
	}
	

	//Testing 
	public static void main(String[] args) {
		try {
			float[][] r =readCSV("data/ContenedoresGraphHopper/distancia.csv");
			for(int i=0; i< r.length; i++)
				for(int j=0; j<r[i].length; j++)
					System.out.println(r[i][j]);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
