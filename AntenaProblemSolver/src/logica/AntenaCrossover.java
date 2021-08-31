package logica;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.solution.binarysolution.BinarySolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.binarySet.BinarySet;
import org.uma.jmetal.util.checking.Check;
import org.uma.jmetal.util.pseudorandom.BoundedRandomGenerator;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.pseudorandom.RandomGenerator;

@SuppressWarnings("serial")
public class AntenaCrossover implements CrossoverOperator<BinarySolution>{
	private double crossoverProbability;
	private RandomGenerator<Double> crossoverRandomGenerator;
	private BoundedRandomGenerator<Integer> pointRandomGenerator;
	private Random seed;
	
	public AntenaCrossover(double crossoverProbability) {
		this(crossoverProbability,() -> JMetalRandom.getInstance().nextDouble(),(a, b) -> JMetalRandom.getInstance().nextInt(a, b));
	}
	public AntenaCrossover(double crossoverProbability, RandomGenerator<Double> randomGenerator) {
		this(crossoverProbability,randomGenerator,BoundedRandomGenerator.fromDoubleToInteger(randomGenerator));
	}
	public AntenaCrossover(double crossoverProbability,RandomGenerator<Double> crossoverRandomGenerator,BoundedRandomGenerator<Integer> pointRandomGenerator) {
		if (crossoverProbability < 0) {
			throw new JMetalException("Crossover probability is negative: " + crossoverProbability);
	    }
	    this.crossoverProbability = crossoverProbability;
	    this.crossoverRandomGenerator = crossoverRandomGenerator;
	    this.pointRandomGenerator = pointRandomGenerator;
	    seed = new Random();
	}
	@Override
	public double getCrossoverProbability() {
		return crossoverProbability;
	}

	/* Setter */
	public void setCrossoverProbability(double crossoverProbability) {
		this.crossoverProbability = crossoverProbability;
	}

	@Override
	public List<BinarySolution> execute(List<BinarySolution> solutions) {
		Check.isNotNull(solutions);
	    Check.that(solutions.size() == 2, "There must be two parents instead of " + solutions.size());

	    return doCrossover(crossoverProbability, solutions.get(0), solutions.get(1));
	}
	
	public List<BinarySolution> doCrossover(double probability, BinarySolution parent1, BinarySolution parent2){
		List<BinarySolution> offspring = new ArrayList<>(2);
	    offspring.add((BinarySolution) parent1.copy());
	    offspring.add((BinarySolution) parent2.copy());

	    if(crossoverRandomGenerator.getRandomValue() < probability) {
	    	// 1. Get the total number of bits
	    	int totalNumberOfBits = parent1.getTotalNumberOfBits();
	    	// 2. Calculate the point to make the crossover
	    	int crossoverPoint = pointRandomGenerator.getRandomValue(0, totalNumberOfBits - 1);
	    	// 3. Compute the variable containing the crossover bit
	    	int variable = 0;
	    	int bitsAccount = parent1.getVariable(variable).getBinarySetLength();
	    	while (bitsAccount < (crossoverPoint + 1)) {
	    		variable++;
	    	    bitsAccount += parent1.getVariable(variable).getBinarySetLength();
	    	}
	    	// 4. Compute the bit into the selected variable
	    	int diff = bitsAccount - crossoverPoint;
	    	int intoVariableCrossoverPoint = parent1.getVariable(variable).getBinarySetLength() - diff;
	    	// 5. Apply the crossover to the variable;
	    	BinarySet offspring1, offspring2;
	    	offspring1 = (BinarySet) parent1.getVariable(variable).clone();
	    	offspring2 = (BinarySet) parent2.getVariable(variable).clone();
	    	for (int i = intoVariableCrossoverPoint; i < offspring1.getBinarySetLength(); i++) {
	    		boolean swap = offspring1.get(i);
	    	    offspring1.set(i, offspring2.get(i));
	    	    offspring2.set(i, swap);
	    	}

	    	offspring.get(0).setVariable(variable, offspring1);
	    	offspring.get(1).setVariable(variable, offspring2);

	    	// 6. Apply the crossover to the other variables
	    	for (int i = variable + 1; i < parent1.getNumberOfVariables(); i++) {
	    		offspring.get(0).setVariable(i, (BinarySet) parent2.getVariable(i).clone());
	    	    offspring.get(1).setVariable(i, (BinarySet) parent1.getVariable(i).clone());
	    	}

	    	//7. Corregir Solucion.
	    	List<Integer> ii = new ArrayList<>();
	    	List<Integer> ii2 = new ArrayList<>();
	    	for (int i = 0; i < totalNumberOfBits; i++) {
	    		if(offspring.get(0).getVariable(0).get(i))
	    			ii.add(i);
	    		if(offspring.get(1).getVariable(0).get(i))
	    			ii2.add(i);
	    	}
	    	while(ii.size()>3) {
	    		int s = seed.nextInt(ii.size());
	    		offspring.get(0).getVariable(0).flip(s);
	    		ii.remove(s);
	    	}
	    	while(ii2.size()>3) {
	    		int s = seed.nextInt(ii2.size());
	    		offspring.get(1).getVariable(0).flip(s);
	    		ii2.remove(s);
	    	}
	    	
	    }
	    return offspring;
	}
	
	@Override
	public int getNumberOfRequiredParents() {
		return 2;
	}
	@Override
	public int getNumberOfGeneratedChildren() {
	    return 2;
	}
}
