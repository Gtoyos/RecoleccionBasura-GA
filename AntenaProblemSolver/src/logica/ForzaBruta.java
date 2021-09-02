package logica;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ForzaBruta {
	private List<Integer> populationDensity;
	public ForzaBruta(Integer filas,Integer columnas) {

		populationDensity = new ArrayList<>();
		try {
			byte[] buf = Files.readAllBytes(Paths.get("instancias/primera_parte/instancia.in"));
			for(byte b: buf) {
				if(b >= 48 && b <=57) {
					populationDensity.add(b-48);
				}
			}
		} catch (IOException e) {
			System.out.println("Hubo un problema al cargar la matriz");
			e.printStackTrace();
		}
		
	}
	
	public void optimo() {
		int solu=10;
		//int sx=0,sy=0,sz=0;
		for(int x=0;x<36;x++) {
			for(int y=0;y<36;y++) {
				for(int z=0; z<36;z++) {
					//System.out.println("Probando con: "+x+" "+y+" "+z);
					int [] populationCovered = new int[6*6];
				    for (int i = 0; i < 36; i++) {
				    	if (i==x || i==y || i==z) {

				    		populationCovered[i]=1;
				    		if(!(i%6==0))
				    			populationCovered[i-1]=1;
				    		if(!((i-5)%6==0))
				    			populationCovered[i+1]=1;
				    		if(!(i<6))
				    			populationCovered[i-6]=1;
				    		if(!(i>29))
				    			populationCovered[i+6]=1;
				    	}
				    }
				    int fitness=0;
				    for(int i = 0; i < 36; i++) {
				    	fitness = fitness + populationCovered[i]*populationDensity.get(i);
				    }
				    if(solu<fitness) {
				    	solu=fitness;
				    	//sx=x;
				    	//sy=y;
				    	//sz=z;
				    }

				}
			}
		}
		System.out.println("Solución óptima (fuerza bruta) = "+solu);
	}
	
}
