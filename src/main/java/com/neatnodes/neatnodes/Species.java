package com.neatnodes.neatnodes;

import java.util.ArrayList;

public class Species {
	
	private Genome representativeGenome; //a genome from the previous generation used to measure compatability against
	
	private ArrayList<Genome> genomes; //stores the genomes in this species
	
	private double averageFitness; //used to determine how many offspring the species can produce. Once it has been set, the species can no longer be edited.
	
	private boolean finalised; //true if the average fitness has been calculated, meaning the species can no longer be edited
	
	private double maxFitness; //the highest fitness any member of this species or its descendants has reached
	
	private int generationsWithoutImprovement; //the number of generations since the maxFitness was last increased
	
	public Species(Genome representativeGenome, double maxFitness, int generationsWithoutImprovement){
		this.representativeGenome = representativeGenome;
		this.genomes = new ArrayList<Genome>();
		this.averageFitness = 0.0;
		this.finalised = false;
		this.maxFitness = maxFitness;
		this.generationsWithoutImprovement = generationsWithoutImprovement;
	}
	
	//attempts to add a genome to the species. The genome will only be added if it falls within the compatability threshold for the species.
	//returns true only if successful
	public boolean addGenome(Genome g){
		//fail if the species is already finalised
		if(finalised){
			throw new GenomeException();
		}
		
		double compatabilityDistance = GlobalFunctions.calculateCompatabilityDistance(g, representativeGenome);
		if(compatabilityDistance <= GlobalFunctions.compatabilityThreshold){
			genomes.add(g);
			return true;
		}
		return false;
	}
	

	public ArrayList<Genome> getGenomes() {
		return genomes;
	}
	
	
	//calculates the adjusted fitnesses for all genomes in the species and sums them together
	//the adjusted fitness sum mentioned in the paper is the equivalent of the average fitness of the species
	//this value is used to determine how many offspring the species will produce
	public void calculateAverageFitness(){
		int numberOfGenomes = genomes.size();
		
		double sum = 0.0;
		for(int i = 0; i < genomes.size(); i ++){
			//the adjusted fitness is the fitness of the genome divided by the number of genomes in the species
			sum += genomes.get(i).getFitness()/numberOfGenomes;
		}
		averageFitness = sum;
		finalised = true;
	}
	
	public double getAverageFitness() {
		//fail if the average fitness hasn't been calculated yet
		if(!finalised){
			throw new GenomeException();
		}
		return averageFitness;
	}
	
	public double getMaxFitness(){
		return maxFitness;
	}
	
	public int getGenerationsWithoutImprovement(){
		return generationsWithoutImprovement;
	}

	public boolean isFinalised() {
		return finalised;
	}
	
	//returns an offspring bred and mutated from the species. If crossover is true, two random members will be bred , if not a single member will be bred with itself
	public Genome produceOffspring(boolean crossover, InnovationManager iManager){
		//fail if the species has not been finalised yet
		if(!finalised){
			throw new GenomeException();
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
		Genome offspring = GlobalFunctions.breed(this.genomes.get(fatherIndex), this.genomes.get(motherIndex), iManager);
		offspring.mutate();
		
		return offspring;
	}
	
	//remove the weakest 50% of genomes and return the best performing genome
	//if the fitness of that genome exceeds the maxFitness, that becomes the new maxFitness
	public Genome cull(){
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
			
			/*System.out.println("COMPLETED SORT:");
			for(int i = 0; i < input.length; i ++){
				System.out.println(input[i].getFitness());
			}*/
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
