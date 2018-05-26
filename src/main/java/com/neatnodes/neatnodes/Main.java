package com.neatnodes.neatnodes;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.SwingUtilities;

public class Main {

	public static void main(String[] args) {
		
		ArrayList<Species> allSpecies = new ArrayList<Species>();
		//we start with a uniform population with no hidden nodes
		
		//set up an initial population of genomes in an initial species
		Genome baseGenome = new Genome();
		baseGenome.addNode(1, Node.BIAS); //the bias node
		baseGenome.addNode(2, Node.INPUT);
		baseGenome.addNode(3, Node.INPUT);
		baseGenome.addNode(4, Node.OUTPUT);
		baseGenome.addConnection(1, 4, 1.0, true, GlobalFunctions.getInnovationNumber(1,4));
		baseGenome.addConnection(2, 4, 1.0, true, GlobalFunctions.getInnovationNumber(2,4));
		baseGenome.addConnection(3, 4, 1.0, true, GlobalFunctions.getInnovationNumber(3,4));
		
		allSpecies.add(new Species(baseGenome, 0.0, 0));
		
		//add the initial population to the first species
		for(int i = 0; i < GlobalFunctions.initialPopulationSize; i++){
			if(allSpecies.get(0).addGenome(baseGenome.cloneGenome()) == false){
				throw new GenomeException();
			}
		}
		
		Genome globalChampion = null;
		
		//run the simulation for 1500 generations
		for(int generation = 0; generation < 1500; generation ++){
			System.out.println("Starting generation " + generation);
						
			double globalFitnessSum = 0.0; //the sum of all average fitnesses of all species
			
			//test each genome in each species for its fitness for calculating XOR, calculate the average fitness for each species and the total sum of all average fitnesses
			for(int i = 0; i < allSpecies.size(); i++){
				ArrayList<Genome> currentGenomes = allSpecies.get(i).getGenomes();
				for(int j = 0; j < currentGenomes.size(); j++){
					Genome current = currentGenomes.get(j);
					current.setFitness(testXORFitness(current));
					//System.out.println("Species: " + i + ", Genome: " + j + ", Fitness: " + current.getFitness());
				}
				allSpecies.get(i).calculateAverageFitness(); //calculate the average fitness of the species
				globalFitnessSum += allSpecies.get(i).getAverageFitness(); //add the average fitness of the species to the sum of all average fitnesses
			}
			
			System.out.println("Fitnesses calculated. Global fitness sum is " + globalFitnessSum);

			//go through each species and have it produce offspring for the next generation
			ArrayList<Genome> nextGeneration = new ArrayList<Genome>(); //stores the next generation
			for(int i = 0; i < allSpecies.size(); i++){
				Species currentSpecies = allSpecies.get(i);
				//decide what proportion of the next generation each species will produce
				//a species which contributes more to the globalFitnessSum gets to produce more of the offspring
				double offspringPercentage = currentSpecies.getAverageFitness() / globalFitnessSum;
				int numberOfOffspring = (int)Math.floor(offspringPercentage * GlobalFunctions.initialPopulationSize);
				int numberOfCrossovers = (int)Math.floor(numberOfOffspring * GlobalFunctions.crossoverProportion);
				
				//cull the weakest genomes from the species before breeding and retrieve the champion of the species
				Genome champion = currentSpecies.cull();
				
				
				
				//set the global champion for this generation if necessary				
				if(globalChampion == null || champion.getFitness() > globalChampion.getFitness()){
					globalChampion = champion;
				}
				
				//if the species has more than 5 members, copy the champion to the next generation unchanged
				if(currentSpecies.getGenomes().size() > 5){
					nextGeneration.add(champion);
					//reduce the number of offspring by 1 to accomodate this
					numberOfOffspring --;
				}
				
				//perform the breeding
				for(int j = 0; j < numberOfOffspring; j ++){
					if(j < numberOfCrossovers){
						nextGeneration.add(currentSpecies.produceOffspring(true));
					}
					else{
						nextGeneration.add(currentSpecies.produceOffspring(false));
					}
				}
			}
			
			System.out.println("Breeding complete");

			//replace all species with a new empty species represented by a random member of the species from the previous generation
			for(int i = 0; i < allSpecies.size(); i ++){
				Species currentSpecies = allSpecies.get(i);
				//pick a random existing member of the species to represent the species for the new generation
				int randomIndex = (int)Math.floor(Math.random() * currentSpecies.getGenomes().size());
				Genome representative = currentSpecies.getGenomes().get(randomIndex);
				double maxFitness = currentSpecies.getMaxFitness();
				int generationsWithoutImproval = currentSpecies.getGenerationsWithoutImproval() + 1;
				System.out.println("Generation: " + generation + ", Species: " + i + ", Max fitness: " + maxFitness + ", Stagnant generations: " + generationsWithoutImproval);
				allSpecies.remove(i);
				allSpecies.add(i, new Species(representative, maxFitness, generationsWithoutImproval));
			}
			
			System.out.println("Created species for the next generation");

			
			//sort each member of the new generation into a species
			for(int i = 0; i < nextGeneration.size(); i++){
				Genome currentGenome = nextGeneration.get(i);
				boolean added = false;
				for(int j = 0; j < allSpecies.size(); j++){
					//attempt to add the genome to each of the species until one is successful
					if(allSpecies.get(j).addGenome(currentGenome)){
						added = true;
						break;
					}
				}
				
				//if the genome has not been accepted into any species, create a new one for it
				if(!added){
					Species newSpecies = new Species(currentGenome, 0.0, 0);
					newSpecies.addGenome(currentGenome);
					allSpecies.add(newSpecies);
				}
			}
			
			//remove any species which have no members
			//we iterate backwards so that removing doesn't cause the loop to skip elements when the indexes change
			for(int i = allSpecies.size() - 1; i >= 0; i --){
				if(allSpecies.get(i).getGenomes().size() == 0){
					allSpecies.remove(i);
				}
			}
			
			System.out.println("New generation sorted into species. Resetting innovations.");

			//reset the record of innovations for a new generation
			GlobalFunctions.newGeneration();
		}
		
		//print the results produced by the best genome
		System.out.println("Results produced by the global champion:");
		System.out.println("0,0: "+ runXOR(globalChampion, 0, 0));
		System.out.println("1,1: "+ runXOR(globalChampion, 1, 1));
		System.out.println("0,1: "+ runXOR(globalChampion, 0, 1));
		System.out.println("1,0: "+ runXOR(globalChampion, 1, 0));
		
		System.out.println("Fitness: " + globalChampion.getFitness());
		
		//write the global champion to a file so it can be retrieved later
		//the filename has a unique timestamp so it doesn't overwrite other genomes
		//the fitness comes first in the filename so that files can be sorted by fitness
		String timestamp = new Timestamp(System.currentTimeMillis()).toString().replace(' ', '.').replace(':', '.').replace('.', '-');
		File output = new File("C:/genomes/genome-" + globalChampion.getFitness() + "-" + timestamp + ".json");
		JSONTools.writeGenomeToFile(globalChampion, output, "Champion XOR genome, fitness: " + globalChampion.getFitness() + "/21^2");
		
		startRenderer(globalChampion);
		
		//verify that the JSON writer and parser are replicating the genome correctly
		File f = new File("C:/genomes/genome-" + globalChampion.getFitness() + "-" + timestamp + ".json");
		Genome testGenome = JSONTools.readGenomeFromFile(f);
		
		startRenderer(testGenome);
		
	}
	
	//start an instance of the renderer with the given genome
	private static void startRenderer(Genome genome){
		final Renderer mApplication = new Renderer(genome);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				mApplication.initApp();
			}
		});
	}
	
	//test the fitness of a genome for calculating XOR
	private static double testXORFitness(Genome g){
		//there are 4 possible inputs to XOR. The fitness is a function of the differences between each of the 4 results and the expected output.
		//outputs will be between 0 and 1
		double r1 = runXOR(g, 0.0, 0.0); //expected result 0
		double r2 = runXOR(g, 1.0, 1.0); //expected result 0
		double r3 = runXOR(g, 0.0, 1.0); //expected result 1
		double r4 = runXOR(g, 1.0, 0.0); //expected result 1
		
		//adding additional inputs to try and smooth the function and make it easier for it to get past the limits it can't get over
		double r5 = runXOR(g, 0.5, 0.5); //expected result 0.5
		double r6 = runXOR(g, 0.25, 0.75); //expected result 0.75
		double r7 = runXOR(g, 0.25, 0.25); //expected result 0.25
		double r8 = runXOR(g, 0.75, 0.25); //expected result 0.75
		double r9 = runXOR(g, 0.75, 0.75); //expected result 0.25
		
		double r10 = runXOR(g, 0.125, 0.125); //expected result 0.125
		double r11 = runXOR(g, 0.375, 0.375); //expected result 0.375
		double r12 = runXOR(g, 0.625, 0.375); //expected result 0.625
		double r13 = runXOR(g, 0.875, 0.125); //expected result 0.875
		double r14 = runXOR(g, 0.125, 0.875); //expected result 0.875
		double r15 = runXOR(g, 0.375, 0.625); //expected result 0.625
		double r16 = runXOR(g, 0.625, 0.625); //expected result 0.375
		double r17 = runXOR(g, 0.875, 0.875); //expected result 0.125

		
		//the differences between each result and the expected result
		
		double d1 = Math.abs(r1 - 0.0);
		double d2 = Math.abs(r2 - 0.0);
		double d3 = Math.abs(r3 - 1.0);
		double d4 = Math.abs(r4 - 1.0);
		
		double d5 = Math.abs(r5 - 0.5);
		double d6 = Math.abs(r6 - 0.75);
		double d7 = Math.abs(r7 - 0.25);
		double d8 = Math.abs(r8 - 0.75);
		double d9 = Math.abs(r9 - 0.25);
		
		double d10 = Math.abs(r10 - 0.125);
		double d11 = Math.abs(r11 - 0.375);
		double d12 = Math.abs(r12 - 0.625);
		double d13 = Math.abs(r13 - 0.875);
		double d14 = Math.abs(r14 - 0.875);
		double d15 = Math.abs(r15 - 0.625);
		double d16 = Math.abs(r16 - 0.375);
		double d17 = Math.abs(r17 - 0.125);
		
		//the total difference. The maximum is 21.0, for when all answers are completely wrong.
		//experimenting with doubling the weight of the 4 key input sets as they are the ones which count
		double sum = 2*(d1 + d2 + d3 + d4) + d5 + d6 + d7 + d8 + d9 + d10 + d11 + d12 + d13 + d14 + d15 + d16 + d17;
		
		//the greater the difference, the lower the fitness
		//we square it as in the paper to give proportionally more fitness the closer it is to a solution
		//the maximum possible fitness should be 21^2 = 441
		return Math.pow(21.0 - sum, 2);
	}
	
	//makes a genome run the XOR function with the specified inputs and returns the output
	private static double runXOR(Genome g, double input1, double input2){
		g.reset(); //reset the genome before giving it new inputs
		
		HashMap<Integer, Double> inputs;
		HashMap<Integer, Double> results;
		
		//set the inputs
		inputs = new HashMap<Integer, Double>();
		inputs.put(1, 1.0); //the bias input
		inputs.put(2, input1);
		inputs.put(3, input2);
		g.writeInputs(inputs);
		
		//run the genome for a preset number of cycles
		for(int i = 0; i < GlobalFunctions.depth; i++){
			g.run();
		}
		
		//read the output
		results = g.readOutputs();
		g.reset();
		return results.get(4);
	}

}
