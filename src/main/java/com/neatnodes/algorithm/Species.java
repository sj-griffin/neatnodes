package com.neatnodes.algorithm;

import java.util.ArrayList;

import com.neatnodes.genome.Genome;
import com.neatnodes.utils.Configuration;

/**
 * A class used to group together Genomes that are sufficiently similar to each other. Each Species has a representative 
 * Genome that potential members will have their similarity tested against.
 * @author Sam Griffin
 */
class Species {
	
	Genome representativeGenome; //a Genome from the previous generation used to measure compatibility against
	
	ArrayList<Genome> genomes; //stores the Genomes in this Species
	
	double averageFitness; //used to determine how many offspring the Species can produce. Once it has been set, the Species can no longer be edited.
	
	boolean finalised; //true if the average fitness has been calculated, meaning the Species can no longer be edited
	
	double maxFitness; //the highest fitness any member of this Species or its descendants has reached
	
	int generationsWithoutImprovement; //the number of generations since the maxFitness was last increased
	
	Configuration configuration;
	
	/**
	 * Creates a new empty Species. Because a Species is generally created as a continuation of a Species from the 
	 * previous generation, arguments are available which allow the parameters of the previous generation's equivalent 
	 * Species to be carried forward.
	 * @param representativeGenome
	 * 		The Genome that will be used to test other Genomes for their compatibility with this Species.
	 * @param maxFitness
	 * 		The maximum fitness attained by a member of the Species so far.
	 * @param generationsWithoutImprovement
	 * 		The number of generations since this Species last improved it's maximum fitness.
	 * @param configuration
	 * 		A Configuration object containing the parameters required by the NEAT algorithm.
	 */
	Species(Genome representativeGenome, double maxFitness, int generationsWithoutImprovement, Configuration configuration){
		this.representativeGenome = representativeGenome;
		this.genomes = new ArrayList<Genome>();
		this.averageFitness = 0.0;
		this.finalised = false;
		this.maxFitness = maxFitness;
		this.generationsWithoutImprovement = generationsWithoutImprovement;
		this.configuration = configuration;
	}
	
	/**
	 * Attempts to add a Genome to the Species. The Genome will only be added if it falls within the compatibility 
	 * threshold specified by the Configuration object. This method will throw an exception if the Species has already 
	 * been finalised.
	 * @param genome
	 * 		The Genome to add.
	 * @return
	 * 		True if successful, or false if the Genome did not meet the criteria.
	 */
	boolean addGenome(Genome genome){
		//fail if the Species is already finalised
		if(finalised){
			throw new RuntimeException("Cannot add Genome to Species after the Species has been finalised");
		}
		
		double compatabilityDistance = StaticFunctions.calculateCompatabilityDistance(genome, representativeGenome, this.configuration);
		if(compatabilityDistance <= this.configuration.compatabilityThreshold){
			genomes.add(genome);
			return true;
		}
		return false;
	}
	

	/**
	 * Get the list of all Genomes in the Species.
	 * @return
	 * 		The list of Genomes.
	 */
	ArrayList<Genome> getGenomes() {
		return genomes;
	}
	
	/**
	 * Calculates the adjusted fitnesses for all Genomes in the species and sums them together to create the 
	 * adjusted fitness sum specified by the NEAT algorithm (i.e. the average fitness across the whole Species).
	 * This value is used to determine how many offspring the Species will produce. Calling this method will 
	 * finalise the Species, which means that no new Genomes can be added.
	 */
	void calculateAverageFitness(){
		int numberOfGenomes = genomes.size();
		
		double sum = 0.0;
		for(int i = 0; i < genomes.size(); i ++){
			//the adjusted fitness is the fitness of the genome divided by the number of genomes in the species
			sum += genomes.get(i).getFitness()/numberOfGenomes;
		}
		averageFitness = sum;
		finalised = true;
	}
	
	/**
	 * Get the average fitness (aka the adjusted fitness sum) of the Species. This method will throw an exception if 
	 * it is called before the Species has been finalised.
	 * @return
	 * 		The average fitness.
	 */
	double getAverageFitness() {
		//fail if the average fitness hasn't been calculated yet
		if(!finalised){
			throw new RuntimeException("Cannot get average fitness before it has been calculated");
		}
		return averageFitness;
	}
	
	/**
	 * Get the maximum fitness attained by any member of the Species.
	 * @return
	 * 		The maximum fitness.
	 */
	double getMaxFitness(){
		return maxFitness;
	}
	
	/**
	 * Get the number of generations since this Species last improved it's maximum fitness.
	 * @return
	 * 		The number of generations without improvement.
	 */
	int getGenerationsWithoutImprovement(){
		return generationsWithoutImprovement;
	}

	/**
	 * Check if the Species has been finalised yet.
	 * @return
	 * 		True if the Species has been finalised.
	 */
	boolean isFinalised() {
		return finalised;
	}
	
	/**
	 * Creates an offspring Genome bred from members of this Species. Mutations will be applied to the new Genome as 
	 * specified by the NEAT algorithm. This method will throw an exception if it is called before the Species has 
	 * been finalised.
	 * @param crossover
	 * 		If true, two random members will be bred with each other, otherwise a single member will be bred with itself.
	 * @param iManager
	 * 		The InnovationManager to use when selecting innovation numbers as part of the breeding process.
	 * @return
	 * 		The new offspring Genome.
	 */
	Genome produceOffspring(boolean crossover, InnovationManager iManager){
		//fail if the species has not been finalised yet
		if(!finalised){
			throw new RuntimeException("Cannot produce offspring before the Species has been finalised");
		}
		
		//pick the genomes to breed
		int fatherIndex = (int)Math.floor(Math.random() * this.genomes.size());
		int motherIndex = -1;
		if(crossover){
			motherIndex = (int)Math.floor(Math.random() * this.genomes.size());
		}
		else{
			motherIndex = fatherIndex;
		}
		
		//breed them and apply a mutation to the offspring
		Genome offspring = StaticFunctions.breed(this.genomes.get(fatherIndex), this.genomes.get(motherIndex), iManager, this.configuration);
		offspring.mutate(configuration);
		
		return offspring;
	}
	
	/**
	 * Remove the weakest 50% of Genomes in the Species and return the Genome with the highest fitness. If 
	 * the fitness of that Genome exceeds the current maximum fitness of the Species, that becomes the new 
	 * maximum fitness.
	 * @return
	 * 		The most fit Genome in the Species.
	 */
	Genome cull(){
		//sort the genomes by fitness
		Genome[] asArray = new Genome[this.genomes.size()];
		this.genomes.toArray(asArray);
		quicksort(asArray, 0, asArray.length - 1);
		//add only the top half of the sorted array back into the list of genomes
		this.genomes = new ArrayList<Genome>();
		int cullThreshold = (int)Math.floor(asArray.length / 2.0);
		for(int i = cullThreshold; i < asArray.length; i ++){
			this.genomes.add(asArray[i]);
		}
		
		Genome champion = this.genomes.get(this.genomes.size() - 1);
		if(champion.getFitness() > maxFitness){
			maxFitness = champion.getFitness();
			generationsWithoutImprovement = 0;
		}
		
		return champion;
	}
	
	private void quicksort (Genome[] input, int first, int last) {
		if (first < last) {
			int difference = last - first;
			int pIndex = first + (int)Math.floor(Math.random() * difference);

			Genome temp = input[first];
			input[first] = input[pIndex];
			input[pIndex] = temp;

			int pivotIndex = partition(input, first, last);

			quicksort(input, first, pivotIndex - 1);

			quicksort(input, pivotIndex + 1, last);
		}
	}
	
	private int partition(Genome[] input, int first, int last){
		Genome p = input[first];
		double pFitness = p.getFitness();
		int lessPointer = first; //points to the next index to be added to the "less than" portion of the array
		int greaterPointer = first; //points to  the next index to be added to the "greater than" portion of the array
		for(int i = first + 1; i <= last; i ++){
			if(input[i].getFitness() < pFitness){
				if(lessPointer != greaterPointer){
					input[greaterPointer] = input[lessPointer];
				}
				input[lessPointer] = input[i];
				lessPointer ++;
				greaterPointer ++;
			}
			else {
				input[greaterPointer] = input[i];
				greaterPointer ++;
			}
		}
		input[greaterPointer] = input[lessPointer];
		input[lessPointer] = p;
		
		return lessPointer;
	}
}
