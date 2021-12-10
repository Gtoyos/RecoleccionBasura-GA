package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Utilidades para cargar y leer archivos csv y convertirlos a matrices de float. 
 * <p> Los arhivos csv necesariamente debe estar separados por , y no contener espacios
 * entre los datos. </p>
 * @author Toyos,Vallcorba
 *
 */
public class MatrixLoader {

	/**
	 * Lee una matriz y la carga en un arreglo de floats
	 * @param url: path del archivo a leer
	 * @return arreglo 2d correspondiente a la matriz leída.
	 * @throws IOException
	 */
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
	
	/**
	 * Permite escribir un arreglo de floats en un archivo csv.
	 * @param m: arreglo a persistir
	 * @param fileName: nombre del archivo a crear
	 */
	public static void writeCSV(float[][] m,String fileName) {
		PrintWriter pw = null;
        try {
            pw = new PrintWriter(new File(fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("Writing csv!");
        StringBuilder builder = new StringBuilder();
        for(int x=0;x<m.length;x++) {
        	for(int y=0;y<m[x].length;y++) {
        		builder.append(m[x][y]+",");
        	}
        	builder.deleteCharAt(builder.length()-1);
        	builder.append("\n");
        }
        pw.write(builder.toString());
        System.out.println("done!");
	}
	public static void writeCSV(float[] m,String fileName) {
		PrintWriter pw = null;
        try {
            pw = new PrintWriter(new File(fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("Writing csv!");
        StringBuilder builder = new StringBuilder();
        for(int x=0;x<m.length;x++) {
        	builder.append(m[x]+",");
        }
        builder.deleteCharAt(builder.length()-1);
        builder.append("\n");
        pw.write(builder.toString());
        System.out.println("done!");
	}
	
	public static float[][] slice(int x,int y, float[][] m){
		float [][] resu = new float[x][y];
		for(int k=0;k<x;k++)
			for(int l=0; l<y;l++)
				resu[k][l]=m[k][l];
		return resu;
	}
	public static float[] slice(int x, float[] m){
		float [] resu = new float[x];
		for(int k=0;k<x;k++)
			resu[k]=m[k];
		return resu;
	}
	
	//Testing 
	private static void main(String[] args) {
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
