package logica;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

import org.uma.jmetal.problem.binaryproblem.impl.AbstractBinaryProblem;
import org.uma.jmetal.solution.binarysolution.BinarySolution;
import org.uma.jmetal.solution.binarysolution.impl.DefaultBinarySolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.binarySet.BinarySet;

@SuppressWarnings("serial")
public class AntenaProblem extends AbstractBinaryProblem {
	private int gridH,gridW;
	Random seed;
	private List<Integer> populationDensity;
	private int genteQueViveEnLaMatriz;
	//Por defecto se aplica el problema 6x6 de la entrega 1
	public AntenaProblem() {
		this(6,6,"instancias/primera_parte/instancia.in");
	}

	/** Constructor */
	public AntenaProblem(Integer filas,Integer columnas,String path) {
	    setNumberOfVariables(1);
	    setNumberOfObjectives(1);
	    setName("AntenaProblemV1");
	    gridH = filas;
	    gridW = columnas;
	    seed = new Random();
		populationDensity = new ArrayList<>();
		try {
			byte[] buf = Files.readAllBytes(Paths.get(path));
			for(byte b: buf) {
				if(b >= 48 && b <=57) {
					populationDensity.add(b-48);
				}
			}
		} catch (IOException e) {
			System.out.println("Hubo un problema al cargar la matriz");
			e.printStackTrace();
		}
		genteQueViveEnLaMatriz=0;
		for(int x: populationDensity) {
			genteQueViveEnLaMatriz=genteQueViveEnLaMatriz+x;
		}
	}
	
	@Override
	public int getBitsFromVariable(int index) {
		if (index != 0)
			throw new JMetalException("Problem AntenaProblemV1 has only a variable. Index = " + index) ;
	  	return gridH*gridW;
	}

	@Override
	public List<Integer> getListOfBitsPerVariable() {
		return Arrays.asList(gridH*gridW) ;
	}

	@Override
	public BinarySolution createSolution() {
		//Crea una Solucion al problema. Las soluciones que se crean son tiras de gridH*gridW
		//Se inicializan las poblaciones insertando antenas aleatorias hasta que todas las celdas qubiertas por alguna antena
		
		BinarySolution x = new DefaultBinarySolution(getListOfBitsPerVariable(), getNumberOfObjectives());
		BinarySet b = new BinarySet(gridH*gridW);
		x.setVariable(0, b);		
		while(poblacionCubierta(gridW,gridH,x)!=genteQueViveEnLaMatriz) {
			x.getVariable(0).set(seed.nextInt(gridH*gridW));
			//System.out.println(x.getVariable(0).toString()+" "+poblacionCubierta(gridW,gridH,x)+"  "+genteQueViveEnLaMatriz);
		}
		return x;
	}

	/** Evaluate() method */
	@Override
	public void evaluate(BinarySolution solution) {

	    BitSet bitset = solution.getVariable(0);    
	    int celdas = poblacionCubierta(gridW,gridH,solution);
	    int antenas=0,fitness=0;
	    for(int i=0;i<gridH*gridW;i++)
	    	if(bitset.get(i))
	    		antenas = antenas+1;
	    if(celdas==genteQueViveEnLaMatriz)
	    	fitness = gridH*gridW - antenas;
	    else
	    	fitness = -genteQueViveEnLaMatriz+celdas;
	    
	
	    // maximization problem: multiply by -1 to minimize
	    solution.setObjective(0, -1*fitness);

	}
	
	private int poblacionCubierta(int x, int y, BinarySolution solution) {
		int [] populationCovered = new int[gridH*gridW];
		int cubiertos=0;
	    BitSet bitset = solution.getVariable(0);
	    for(int i=0; i< x*y; i++) {
	    	if (bitset.get(i)) {
	    		if(populationCovered[i]==0)
	    			cubiertos=cubiertos+populationDensity.get(i);
	    		populationCovered[i]=1;
	    		if(!(i%x==0)) {
		    		if(populationCovered[i-1]==0)
		    			cubiertos=cubiertos+populationDensity.get(i-1);
	    			populationCovered[i-1]=1;
	    		}
	    		if(!((i-(x-1))%x==0)) {
		    		if(populationCovered[i+1]==0)
		    			cubiertos=cubiertos+populationDensity.get(i+1);
	    			populationCovered[i+1]=1;
	    		}
	    		if(!(i<y)) {
		    		if(populationCovered[i-y]==0)
		    			cubiertos=cubiertos+populationDensity.get(i-y);
	    			populationCovered[i-y]=1;
	    		}
	    		if(!(i>x*y-y-1)) {
		    		if(populationCovered[i+y]==0)
		    			cubiertos=cubiertos+populationDensity.get(i+y);
	    			populationCovered[i+y]=1;
	    		}
	    	}
	    }
	    //System.out.println(cubiertos);
	    return cubiertos;
	}
	
}
