package com.neatnodes.algorithm;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;

import com.neatnodes.genome.Genome;
import com.neatnodes.ui.CommandQueue;
import com.neatnodes.ui.GenomeRenderer;
import com.neatnodes.utils.Configuration;
import com.neatnodes.utils.DataSet;

/**
 *  Provides entry point methods for the library.
 *  @author Sam Griffin
 */
public class Simulation {
	
	/**
	 * Runs the function implemented by the supplied Genome. This method assumes that the Genome has all 
	 * its inputs in consecutive node positions starting from node 1.
	 * @param genome
	 * 		The Genome to run.
	 * @param inputs
	 * 		An array of input values to pass to the Genome when running it. the number of inputs must 
	 * 		match what is expected by the Genome or this method will throw an exception.
	 * @param depth
	 * 		The number of iterations to run the Genome for before reading off a result.
	 * @return
	 * 		An array of output values produced by running the Genome. The number of outputs is determined by 
	 * 		the number of output nodes in the Genome.
	 */
	public static Double[] runFunction(Genome genome, Double[] inputs, int depth) {
		if(inputs.length != genome.getNumberOfInputs()) {
			throw new RuntimeException("Invalid number of inputs for the Genome");
		}
		
		//set the inputs
		HashMap<Integer, Double> inputMap = new HashMap<Integer, Double>();
		for(int i = 0; i < inputs.length; i++) {
			inputMap.put(i + 1, inputs[i]);
		}
		genome.writeInputs(inputMap);
		
		//run the genome for a preset number of cycles
		for(int i = 0; i < depth; i++){
			genome.run();
		}
		
		//read the outputs
		HashMap<Integer, Double> outputMap = genome.readOutputs();
		genome.reset();
		
		Double[] outputs = new Double[outputMap.size()];
		int index = 0;
		for(Integer key : outputMap.keySet()) {
			outputs[index] = outputMap.get(key);
			index ++;
		}
		return outputs;
	}
		
	/**
	 * Tests the fitness of a Genome for computing a function represented by the supplied DataSet. The entries in the 
	 * DataSet must align with what is expected by the Genome, otherwise an exception will be thrown.
	 * @param genome
	 * 		The Genome to test.
	 * @param dataset
	 * 		The DataSet to test against.
	 * @param depth
	 * 		The number of iterations to run the Genome for before reading off a result each time.
	 * @return
	 * 		A fitness score out of 100, with 100 being a Genome that perfectly reproduces the DataSet, and 0 being a 
	 * 		Genome that gets every result completely wrong.
	 */
	public static double testFitness(Genome genome, DataSet dataset, int depth) {
		//validate that the DataSet fits the Genome
		if(dataset.getInputNumber() != genome.getNumberOfInputs()) {
			throw new RuntimeException("Number of inputs in the dataset does not match the Genome");
		}
		
		if(dataset.getOutputNumber() != genome.getNumberOfOutputs()) {
			throw new RuntimeException("Number of outputs in the dataset does not match the Genome");
		}
		
		double totalDifference = 0.0; //tracks the accumulated difference between the expected set of outputs and the actual output		
		double maxPossibleDifference = 0.0; //the total possible difference for when all results are completely wrong
		
		//test the Genome against each entry in the DataSet
		for(int i = 0; i < dataset.getNumberOfEntries(); i++) {
			Double[] inputs = dataset.getInputsForRow(i);
			Double[] result = runFunction(genome, inputs, depth);
			Double[] expectedOutputs = dataset.getOutputsForRow(i);
			Double weight = 1.0;
			if(dataset.isWeighted()) {
				weight = dataset.getWeightForRow(i);
			}
			maxPossibleDifference += (weight * dataset.getOutputNumber() * 2); //we multiply by 2 because the sigmoid function produces results between -1 and 1. In the worst case, a given result will differ from the expected result by 2.
			
			//for each result, we find the difference between it and the expected output in that position, then add it to the total difference
			//entries have an impact on the total difference proportional to their weight
			for(int j = 0; j < expectedOutputs.length; j++) {
				totalDifference += weight * (Math.abs(result[j] - expectedOutputs[j]));
			}
		}
		
		//the greater the difference, the lower the fitness
		//we square the result to give proportionally more fitness the closer it is to a solution
		double rawFitnessScore = Math.pow(maxPossibleDifference - totalDifference, 2);
		
		//the maximum possible fitness should be the square of the maximum possible difference
		//we use this to calculate a percentage representing how accurate the genome is
		double maxPossibleFitness = Math.pow(maxPossibleDifference, 2);
		return (rawFitnessScore / maxPossibleFitness) * 100.00;
	}
	
	/**
	 * Runs the complete NEAT algorithm to produce a Genome that implements the supplied DataSet as well as possible.
	 * @param dataset
	 * 		The DataSet to run the algorithm on.
	 * @param config
	 * 		A Configuration object that provides the parameters to run the algorithm with.
	 * @param visualize
	 * 		If true, the simulation will run in visualize mode, where it is artificially slowed down and rendered 
	 * 		interactively in a window. If false, the algorithm runs at normal speed and the only feedback will be 
	 * 		via stdout.
	 * @return
	 * 		The resulting Genome that best implements the DataSet.
	 */
	public static Genome runSimulation(DataSet dataset, Configuration config, boolean visualize) {

		InnovationManager iManager = new InnovationManager();
		ArrayList<Species> allSpecies = new ArrayList<Species>();
		NumberFormat doubleFormat = new DecimalFormat("#0.00");
		
		CommandQueue commandQueue = new CommandQueue(); //stores the commands that will be executed by the GenomeRenderer
		final GenomeRenderer renderer = new GenomeRenderer(config.stylePath, config.renderStyle, commandQueue, true);

		if(visualize) {
			new Thread(renderer).start(); //start the GenomeRenderer running in it's own thread
		}
		
		//setup an initial uniform population in an initial species
		allSpecies.add(StaticFunctions.setupInitialSpecies(dataset.getInputNumber(), dataset.getOutputNumber(), config.initialPopulationSize, iManager, config));
		
		Genome globalChampion = null;
		
		//run the simulation for the configured number of generations
		for(int generation = 0; generation < config.generations; generation ++){
			System.out.println("Starting generation " + generation);
						
			double globalFitnessSum = 0.0; //the sum of all average fitnesses of all species
			
			//test each genome in each species for its fitness for reproducing the dataset, calculate the average fitness for each species and the total sum of all average fitnesses
			for(int i = 0; i < allSpecies.size(); i++){
				ArrayList<Genome> currentGenomes = allSpecies.get(i).getGenomes();
				for(int j = 0; j < currentGenomes.size(); j++){
					Genome current = currentGenomes.get(j);
					current.setFitness(testFitness(current, dataset, config.depth));
				}
				allSpecies.get(i).calculateAverageFitness(); //calculate the average fitness of the species
				globalFitnessSum += allSpecies.get(i).getAverageFitness(); //add the average fitness of the species to the sum of all average fitnesses
			}
			
			System.out.println("Fitnesses calculated. Global fitness sum is " + doubleFormat.format(globalFitnessSum));

			//go through each species and have it produce offspring for the next generation
			ArrayList<Genome> nextGeneration = new ArrayList<Genome>(); //stores the next generation
			for(int i = 0; i < allSpecies.size(); i++){
				Species currentSpecies = allSpecies.get(i);
				//decide what proportion of the next generation each species will produce
				//a species which contributes more to the globalFitnessSum gets to produce more of the offspring
				double offspringPercentage = currentSpecies.getAverageFitness() / globalFitnessSum;
				int numberOfOffspring = (int)Math.floor(offspringPercentage * config.initialPopulationSize);
				int numberOfCrossovers = (int)Math.floor(numberOfOffspring * config.crossoverProportion);
				
				//cull the weakest genomes from the species before breeding and retrieve the champion of the species
				Genome champion = currentSpecies.cull();
				
				//render the surviving genomes in the species if visualize mode is on
				if(visualize) {
					ArrayList<Genome> remainingGenomes = currentSpecies.getGenomes();
					for(Genome g : remainingGenomes) {
						//queue the command so that it will be picked up by the GenomeRenderer thread
						commandQueue.push(() -> renderer.addGenomeToCurrentGeneration(g));
					}
				}
				
				//set the global champion for this generation if necessary				
				if(globalChampion == null || champion.getFitness() > globalChampion.getFitness()){
					globalChampion = champion;
				}
				
				//if the species has more than 5 members, copy the champion to the next generation unchanged
				if(currentSpecies.getGenomes().size() > 5){
					nextGeneration.add(champion);
					//reduce the number of offspring by 1 to accommodate this
					numberOfOffspring --;
				}
				
				//perform the breeding
				for(int j = 0; j < numberOfOffspring; j ++){
					if(j < numberOfCrossovers){
						nextGeneration.add(currentSpecies.produceOffspring(true, iManager));
					}
					else{
						nextGeneration.add(currentSpecies.produceOffspring(false, iManager));
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
				int generationsWithoutImprovement = currentSpecies.getGenerationsWithoutImprovement() + 1;
				System.out.println("Generation: " + generation + ", Species: " + i + ", Max fitness: " + doubleFormat.format(maxFitness) + ", Stagnant generations: " + generationsWithoutImprovement);
				allSpecies.remove(i);
				allSpecies.add(i, new Species(representative, maxFitness, generationsWithoutImprovement, config));
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
					Species newSpecies = new Species(currentGenome, 0.0, 0, config);
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
			iManager.newGeneration();
			
			//if visualization mode is enabled, notify the renderer that a new generation has started
			if(visualize) {
				//queue the command so that it will be picked up by the GenomeRenderer thread
				commandQueue.push(() -> renderer.newGeneration());

				//sleep for 5 seconds to give the auto-positioning time to work
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		//switch off the renderer auto layout feature once all genomes have been rendered to stop them drifting out of their grid formation
		if(visualize) {
			//sleep for 10 seconds to give the auto-layout time to work
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//queue the command so that it will be picked up by the GenomeRenderer thread
			commandQueue.push(() -> renderer.autoLayoutOn(false));
		}
		
		System.out.println("Simulation complete. The best genome produces the following results:");
		
		//print a sample of the results produced by the best genome
		int entriesToShow = 4;
		if(dataset.getNumberOfEntries() < 4) {
			entriesToShow = dataset.getNumberOfEntries();
		}
		for(int i = 0; i < entriesToShow; i++) {
			Double[] inputs = dataset.getInputsForRow(i);
			for(int j = 0; j < inputs.length - 1; j++) {
				System.out.print(doubleFormat.format(inputs[j]) + ", ");
			}
			System.out.print(doubleFormat.format(inputs[inputs.length - 1]) + "-> ");
			Double[] outputs = runFunction(globalChampion, inputs, config.depth);
			for(int j = 0; j < outputs.length - 1; j++) {
				System.out.print(doubleFormat.format(outputs[j]) + ", ");
			}
			System.out.println(doubleFormat.format(outputs[outputs.length - 1]));
		}
		System.out.println("Fitness: " + doubleFormat.format(globalChampion.getFitness()));
		return globalChampion;
	}
	
	/**
	 * Displays a single Genome in a window.
	 * @param genome
	 * 		The Genome to render.
	 * @param config
	 * 		A Configuration containing the parameters to use for the display.
	 */
	public static void viewGenome(Genome genome, Configuration config) {
		CommandQueue commandQueue = new CommandQueue(); //stores the commands that will be executed by the GenomeRenderer
		final GenomeRenderer renderer = new GenomeRenderer(config.stylePath, config.renderStyle, commandQueue, false);
		new Thread(renderer).start(); //start the GenomeRenderer running in it's own thread
		commandQueue.push(() -> renderer.renderGenome(genome, null, null, "large"));
	}

}
