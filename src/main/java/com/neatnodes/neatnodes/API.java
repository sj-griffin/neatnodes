package com.neatnodes.neatnodes;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.DataFormatException;

import javax.swing.SwingUtilities;

public class API {
	//runs the function implemented by the supplied genome with the specified inputs and returns the outputs
	//the number of inputs must match what is expected by the genome or this method will throw an exception
	//this function assumes that the genome has all its inputs in consecutive node positions starting from 1
	public static Double[] runFunction(Genome g, Double[] inputs) {
		if(inputs.length != g.getNumberOfInputs()) {
			throw new GenomeException();
		}
		
		//set the inputs
		HashMap<Integer, Double> inputMap = new HashMap<Integer, Double>();
		for(int i = 0; i < inputs.length; i++) {
			inputMap.put(i + 1, inputs[i]);
		}
		g.writeInputs(inputMap);
		
		//run the genome for a preset number of cycles
		for(int i = 0; i < StaticFunctions.depth; i++){
			g.run();
		}
		
		//read the outputs
		HashMap<Integer, Double> outputMap = g.readOutputs();
		g.reset();
		
		Double[] outputs = new Double[outputMap.size()];
		int index = 0;
		for(Integer key : outputMap.keySet()) {
			outputs[index] = outputMap.get(key);
			index ++;
		}
		return outputs;
	}
	
	//test the fitness of a genome for computing a function represented by the supplied DataSet
	//returns a fitness score out of 100, with 100 being a Genome that perfectly reproduces the DataSet, and 0 being a Genome that gets every result completely wrong
	//if the entries in the DataSet do not align with what is expected by the genome, this function will throw an exception
	public static double testFitness(Genome g, DataSet d) {
		//validate that the DataSet fits the Genome
		if(d.getInputNumber() != g.getNumberOfInputs()) {
			throw new GenomeException();
		}
		
		if(d.getOutputNumber() != g.getNumberOfOutputs()) {
			throw new GenomeException();
		}
		
		double totalDifference = 0.0; //tracks the accumulated difference between the expected set of outputs and the actual output		
		double maxPossibleDifference = 0.0; //the total possible difference for when all results are completely wrong
		
		//test the Genome against each entry in the DataSet
		for(int i = 0; i < d.getNumberOfEntries(); i++) {
			Double[] inputs = d.getInputsForRow(i);
			Double[] result = runFunction(g, inputs);
			Double[] expectedOutputs = d.getOutputsForRow(i);
			Double weight = 1.0;
			if(d.isWeighted()) {
				weight = d.getWeightForRow(i);
			}
			maxPossibleDifference += (weight * d.getOutputNumber());
			
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
	
	//functionName is a name for the function represented by the test data
	//dataSetPath is the path to a CSV file containing the dataset
	public static void runSimulation(String functionName, String dataSetPath) {
		DataSet dataset = null;
		try {
			dataset = new DataSet(dataSetPath);
		} catch (DataFormatException e) {
			e.printStackTrace();
			System.exit(1);
		}
		InnovationManager iManager = new InnovationManager();
		ArrayList<Species> allSpecies = new ArrayList<Species>();
		NumberFormat doubleFormat = new DecimalFormat("#0.00");  
		
		//setup an initial uniform population in an initial species
		allSpecies.add(StaticFunctions.setupInitialSpecies(dataset.getInputNumber(), dataset.getOutputNumber(), StaticFunctions.initialPopulationSize, iManager));
		
		Genome globalChampion = null;
		
		//run the simulation for the configured number of generations
		for(int generation = 0; generation < StaticFunctions.numberOfGenerations; generation ++){
			System.out.println("Starting generation " + generation);
						
			double globalFitnessSum = 0.0; //the sum of all average fitnesses of all species
			
			//test each genome in each species for its fitness for reproducing the dataset, calculate the average fitness for each species and the total sum of all average fitnesses
			for(int i = 0; i < allSpecies.size(); i++){
				ArrayList<Genome> currentGenomes = allSpecies.get(i).getGenomes();
				for(int j = 0; j < currentGenomes.size(); j++){
					Genome current = currentGenomes.get(j);
					current.setFitness(testFitness(current, dataset));
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
				int numberOfOffspring = (int)Math.floor(offspringPercentage * StaticFunctions.initialPopulationSize);
				int numberOfCrossovers = (int)Math.floor(numberOfOffspring * StaticFunctions.crossoverProportion);
				
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
				allSpecies.add(i, new Species(representative, maxFitness, generationsWithoutImprovement));
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
			iManager.newGeneration();
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
			Double[] outputs = runFunction(globalChampion, inputs);
			for(int j = 0; j < outputs.length - 1; j++) {
				System.out.print(doubleFormat.format(outputs[j]) + ", ");
			}
			System.out.println(doubleFormat.format(outputs[outputs.length - 1]));
		}
		System.out.println("Fitness: " + doubleFormat.format(globalChampion.getFitness()));
		
		//write the global champion to a file so it can be retrieved later
		//the filename has a unique timestamp so it doesn't overwrite other genomes
		//the fitness comes first in the filename so that files can be sorted by fitness
		String timestamp = new Timestamp(System.currentTimeMillis()).toString().replace(' ', '.').replace(':', '.').replace('.', '-');
		String outputPath = "C:/genomes/" + functionName + "-" + globalChampion.getFitness() + "-" + timestamp + ".json";
		JSONTools.writeGenomeToFile(globalChampion, outputPath, "Champion " + functionName + " genome, fitness: " + globalChampion.getFitness());
	}
	
	//start an instance of the renderer with the given genome
	public static void startRenderer(Genome genome){
		final Renderer mApplication = new Renderer(genome);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				mApplication.initApp();
			}
		});
	}
	
	public static void main(String[] args) {
		runSimulation("Addition", "./datasets/Addition.csv");
		
		//Genome testGenome = JSONTools.readGenomeFromFile("C:/genomes/genome-92.31298566971546-2018-06-11-10-10-49-473.json");
		//startRenderer(testGenome);
	}
}
