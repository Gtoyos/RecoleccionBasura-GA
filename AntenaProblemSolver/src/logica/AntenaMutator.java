package logica;

import java.util.Random;

import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.solution.binarysolution.BinarySolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.checking.Check;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.pseudorandom.RandomGenerator;


@SuppressWarnings("serial")
public class AntenaMutator implements MutationOperator<BinarySolution>{
	private double mutationProb;
	private RandomGenerator<Double> randomGenerator;
	Random seed;
	
	public AntenaMutator(double mutationProbability) {
		this(mutationProbability, ()->JMetalRandom.getInstance().nextDouble());
	}
	
	public AntenaMutator(double mutationProbability, RandomGenerator<Double> randomGenerator) {
		if(mutationProbability < 0) {
			throw new JMetalException("Mutation probability is negative: " + mutationProbability);
		}
		setMutationProbability(mutationProbability);
		seed = new Random();
		this.randomGenerator = randomGenerator;
	}

	public double getMutationProbability() {
		return mutationProb;
	}

	public void setMutationProbability(double mutationProb) {
		this.mutationProb = mutationProb;
	}
	
	@Override
	public BinarySolution execute(BinarySolution solution) {
		Check.isNotNull(solution);
		doMutation(mutationProb, solution);
		return solution;
	}

	public void doMutation(double probability, BinarySolution solution) {
		if(randomGenerator.getRandomValue() <= probability) {
			for(int i = seed.nextInt(solution.getNumberOfBits(0)); i < solution.getNumberOfBits(0); i++) {
				if(solution.getVariable(0).get(i)) {
					solution.getVariable(0).flip(i);
					solution.getVariable(0).set(seed.nextInt(solution.getNumberOfBits(0)));
				}
			}
		}
	}

}

