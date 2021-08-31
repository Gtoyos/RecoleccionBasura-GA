package logica;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.uma.jmetal.problem.binaryproblem.impl.AbstractBinaryProblem;
import org.uma.jmetal.solution.binarysolution.BinarySolution;
import org.uma.jmetal.solution.binarysolution.impl.DefaultBinarySolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.binarySet.BinarySet;

@SuppressWarnings("serial")
public class AntenaProblem extends AbstractBinaryProblem {
	private int gridH,gridW;
	Random seed;
	
	//Por defecto se aplica el problema 6x6
	public AntenaProblem() {
		this(6,6);
	}

	/** Constructor */
	public AntenaProblem(Integer filas,Integer columnas) {
	    setNumberOfVariables(1);
	    setNumberOfObjectives(1);
	    setName("AntenaProblemV1");
	    gridH = filas;
	    gridW = columnas;
	    seed = new Random();
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
		//bits con 3 bits encendidos en posiciones aleatorias
		//La tira de bits representa una matriz comenzando en la fila 0 y lee por filas.
		
		BinarySolution x = new DefaultBinarySolution(getListOfBitsPerVariable(), getNumberOfObjectives());
		BinarySet b = new BinarySet(gridH*gridW);
			
		Set<Integer> p = new HashSet<>();
		while(p.size() < 3)
			p.add(seed.nextInt(gridH*gridW));
		for(Integer i: p)
			b.set(i);
		x.setVariable(0, b);
		return x;
	}

	/** Evaluate() method */
	@Override
	public void evaluate(BinarySolution solution) {
		List<Integer> populationDensity = new ArrayList<>();
		try {
			byte[] buf = Files.readAllBytes(Paths.get("instancias/primera_parte/instancia.in"));
			for(byte b: buf) {
				if(b >= 48 && b <=57) {
					populationDensity.add(b-48);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		int [] populationCovered = new int[gridH*gridW];
	    BitSet bitset = solution.getVariable(0);
	    int antenas=3;
	    for (int i = 0; i < bitset.length() && antenas>=0; i++) {
	    	if (bitset.get(i)) {
	    		antenas--;
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
	    for(int i = 0; i < bitset.length(); i++) {
	    	fitness = fitness + populationCovered[i]*populationDensity.get(i);
	    }

	    // maximization problem: multiply by -1 to minimize
	    if(antenas>=0)
	    	solution.setObjective(0, -1.0 * fitness);
	    else
	    	solution.setObjective(0, -1.0 * 0);
	  }
}
