package logica;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import org.uma.jmetal.problem.binaryproblem.impl.AbstractBinaryProblem;
import org.uma.jmetal.solution.binarysolution.BinarySolution;
import org.uma.jmetal.solution.binarysolution.impl.DefaultBinarySolution;
import org.uma.jmetal.util.JMetalException;

@SuppressWarnings("serial")
public class AntenaProblem extends AbstractBinaryProblem {
	private int bits ;
	
	  //Pro defecto se crea el problema 6x6
	  public AntenaProblem() {
	    this(6*6);
	  }

	  /** Constructor */
	  public AntenaProblem(Integer numberOfBits) {
	    setNumberOfVariables(1);
	    setNumberOfObjectives(1);
	    setName("AntenaProblem");
	    bits = numberOfBits ;
	  }

	  @Override
	  public int getBitsFromVariable(int index) {
	  	if (index != 0) {
	  		throw new JMetalException("Problem OneMax has only a variable. Index = " + index) ;
	  	}
	  	return bits ;
	  }

	  @Override
	  public List<Integer> getListOfBitsPerVariable() {
		  return Arrays.asList(bits) ;
	  }

	  @Override
	  public BinarySolution createSolution() {
	    return new DefaultBinarySolution(getListOfBitsPerVariable(), getNumberOfObjectives()) ;
	  }

	  /** Evaluate() method */
	  @Override
	  public void evaluate(BinarySolution solution) {
	    int counterOnes;

	    counterOnes = 0;

	    BitSet bitset = solution.getVariable(0) ;
	    for (int i = 0; i < bitset.length(); i++) {
	      if (bitset.get(i)) {
	        counterOnes++;
	      }
	    }

	    // OneMax is a maximization problem: multiply by -1 to minimize
	    solution.setObjective(0, -1.0 * counterOnes);
	  }
}
