package com.neatnodes.neatnodes;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DataFormatException;

import javax.swing.SwingUtilities;

public class GlobalFunctions {
	//the chances of various mutations occuring
	public static final double weightMutationChance = 0.8;
	public static final double nodeMutationChance = 0.03;
	public static final double linkMutationChance = 0.05;
	public static final double disableMutationChance = 0.75;
	
	//coefficients used to adjust the importance of the three factors used to calculate compatability distance
	private static final double c1 = 1.0; //importance of E
	private static final double c2 = 1.0; //importance of D
	private static final double c3 = 0.4; //importance of W bar
	
	public static final double compatabilityThreshold = 1.0; //called dt in the paper. default 3.0
	
	public static final int initialPopulationSize = 150;
	
	public static final int numberOfGenerations = 1000; // the number of generations to run the simulation for
	
	public static final double crossoverProportion = 0.75; //the fraction of offspring which are created by crossing two genomes. The rest are cloned from a single genome.
	
	public static final int depth = 3; //controls the number of cycles to run each genome for before reading a result. It is the equivalent of the "depth" in a feed-forward network
	
	/**return the compatability distance between two genomes
	*key variables:
	*numberOfExcessGenes: the number of excess genes (E)
	*numberOfDisjointGenes: the number of disjoint genes (D)
	*averageWeightDifference: the average weight difference between matching genes, including disabled genes (W bar)
	*maxGenes: the number of genes in the larger genme (N)
	**/
	public static double calculateCompatabilityDistance(Genome g1, Genome g2){		
		HashMap<Integer, Connection> g1Genes = g1.getConnectionGenes();
		HashMap<Integer, Connection> g2Genes = g2.getConnectionGenes();
		
		int g1GenesSize = g1Genes.size();
		int g2GenesSize = g2Genes.size();
		
		if (g1GenesSize == 0 || g2GenesSize == 0){
			//the algorithm will not work for empty genomes
			throw new GenomeException();
		}
		
		int g1GenesCounted = 0;
		int g2GenesCounted = 0;
		
		int geneNumber = 1;
		
		ArrayList<Double> weightDifferences = new ArrayList<Double>(); //store the weight differences between each matching pair of genes
		
		int numberOfExcessGenes = 0; //called E in the paper
		int numberOfDisjointGenes = 0; //called D in the paper
		
		//these variables are used to decide whether genes are excess or disjoint
		int g1sHighestInnovationNumber = 0;
		int g2sHighestInnovationNumber = 0;
		
		for (Map.Entry<Integer, Connection> connection : g1Genes.entrySet()){
			int currentValue = connection.getValue().getInnovationNumber();
			if(currentValue > g1sHighestInnovationNumber){
				g1sHighestInnovationNumber = currentValue;
			}
		}
		
		for (Map.Entry<Integer, Connection> connection : g2Genes.entrySet()){
			int currentValue = connection.getValue().getInnovationNumber();
			if(currentValue > g2sHighestInnovationNumber){
				g2sHighestInnovationNumber = currentValue;
			}
		}
		
		//attempt to match the genes of the two genomes
		while(g1GenesCounted < g1GenesSize || g2GenesCounted < g2GenesSize){
			Connection g1Gene = g1Genes.get(geneNumber);
			Connection g2Gene = g2Genes.get(geneNumber);
			
			//if a matching pair of genes are found
			if(g1Gene != null && g2Gene != null){
				g1GenesCounted ++;
				g2GenesCounted ++;
				
				//record the weight difference between the two genes
				weightDifferences.add(Math.abs(g1Gene.getWeight() - g2Gene.getWeight()));
				
			} else if (g1Gene != null){
				//if g1 has a gene with no match
				g1GenesCounted ++;
				
				//check if the extra gene is disjoint or excess
				//it is disjoint if its innovation number is less than the highest innovation number of the other genome, otherwise it is excess
				if(g1Gene.getInnovationNumber() < g2sHighestInnovationNumber){
					numberOfDisjointGenes ++;
				}
				else{
					numberOfExcessGenes ++;
				}
				
				
			} else if (g2Gene != null){
				//if g2 has a gene with no match
				g2GenesCounted ++;
				
				//check if the extra gene is disjoint or excess
				//it is disjoint if its innovation number is less than the highest innovation number of the other genome, otherwise it is excess
				if(g2Gene.getInnovationNumber() < g1sHighestInnovationNumber){
					numberOfDisjointGenes ++;
				}
				else{
					numberOfExcessGenes ++;
				}
			}
			geneNumber ++;
		}
				
		double sum = 0;
		for(int i = 0; i < weightDifferences.size(); i ++){
			sum += weightDifferences.get(i);
		}
		double averageWeightDifference = sum / weightDifferences.size(); //called W bar in the paper
		
		int maxGenes = 0; //called N in the paper
		if(g1GenesSize >= g2GenesSize){
			maxGenes = g1GenesSize;
		}
		else {
			maxGenes = g2GenesSize;
		}
		
		//perform the calculation
		double term1 = (c1 * numberOfExcessGenes)/maxGenes;
		double term2 = (c2 * numberOfDisjointGenes)/maxGenes;
		double term3 = c3 * averageWeightDifference;
		return term1 + term2 + term3;
	}
	
	//breed two genomes and return their offspring
	public static Genome breed(Genome father, Genome mother, InnovationManager iManager){
		Genome offspring = new Genome(iManager);
		
		//fail if the fitness of either parent has not been set
		if(!father.isFitnessMeasured() || !mother.isFitnessMeasured()){
			throw new GenomeException();
		}
		
		//calculate who is the fitter parent
		//this code doesn't take into account the case where parents are equally fit like the paper does, but this is a rare case with virtually no effect on the outcome
		boolean fatherIsFitter = false;
		if(father.getFitness() > mother.getFitness()){
			fatherIsFitter = true;
		}
		
		HashMap<Integer, Connection> fatherGenes = father.getConnectionGenes();
		HashMap<Integer, Connection> motherGenes = mother.getConnectionGenes();
		
		int fatherGenesSize = fatherGenes.size();
		int motherGenesSize = motherGenes.size();
		
		int fatherGenesCounted = 0;
		int motherGenesCounted = 0;
		
		int geneNumber = 0;
		
		//attempt to match the genes of the two parents
		while(fatherGenesCounted < fatherGenesSize || motherGenesCounted < motherGenesSize){
			Connection fatherGene = fatherGenes.get(geneNumber);
			Connection motherGene = motherGenes.get(geneNumber);
			
			//if a matching pair of genes are found
			if(fatherGene != null && motherGene != null){
				//if the genes match, create a new gene from them
				fatherGenesCounted ++;
				motherGenesCounted ++;
				
				//randomly pick one of the connections to inherit
				Connection connectionToAdd = null;
				
				double random = Math.random();
				if(random < 0.5){
					connectionToAdd = fatherGene;
				} else {
					connectionToAdd = motherGene;
				}
				
				//if the connection is disabled in either parent, there is a 75% chance that it will be disabled in the offspring
				boolean offspringGeneEnabled = true;
				if(!fatherGene.isEnabled() || !motherGene.isEnabled()){
					if(Math.random() < GlobalFunctions.disableMutationChance){
						offspringGeneEnabled = false;
					}
				}
				
				//add the connection and its dependencies
				duplicateConnection(connectionToAdd, offspring, offspringGeneEnabled);
				
			} else if (fatherGene != null){
				//if the father has a gene with no match
				fatherGenesCounted ++;
				
				//add the unmatched gene if the father is fitter
				if(fatherIsFitter){
					//add the connection and its dependencies
					
					//if the connection is disabled in the parent, there is a 75% chance that it will be disabled in the offspring
					boolean offspringGeneEnabled = true;
					if(!fatherGene.isEnabled()){
						if(Math.random() < GlobalFunctions.disableMutationChance){
							offspringGeneEnabled = false;
						}
					}
					
					duplicateConnection(fatherGene, offspring, offspringGeneEnabled);
				}
				
			} else if (motherGene != null){
				//if the mother has a gene with no match
				motherGenesCounted ++;
				
				//add the unmatched gene if the mother is fitter
				if(!fatherIsFitter){
					//add the connection and its dependencies
					
					//if the connection is disabled in the parent, there is a 75% chance that it will be disabled in the offspring
					boolean offspringGeneEnabled = true;
					if(!motherGene.isEnabled()){
						if(Math.random() < GlobalFunctions.disableMutationChance){
							offspringGeneEnabled = false;
						}
					}
					
					duplicateConnection(motherGene, offspring, offspringGeneEnabled);
				}
			}
			geneNumber ++;
		}
		
		return offspring;
	}
	
	
	//add a connection and the nodes it depends on to another genome
	private static void duplicateConnection(Connection c, Genome g, boolean enabled){
		Node nodeToAdd1 = c.getInNode();
		Node nodeToAdd2 = c.getOutNode();
		
		//add the required nodes to the offspring if they don't already exist.
		if(g.getNode(nodeToAdd1.getLabel()) == null){
			g.addNode(nodeToAdd1.getLabel(), nodeToAdd1.getType());
		}
		if(g.getNode(nodeToAdd2.getLabel()) == null){
			g.addNode(nodeToAdd2.getLabel(), nodeToAdd2.getType());
		}
		
		//add the required connection to the offspring.
		g.addConnection(nodeToAdd1.getLabel(), nodeToAdd2.getLabel(), c.getWeight(), enabled, c.getInnovationNumber());
	}
	
	//set up an initial population of genomes with the specified number of inputs and outputs in an initial species
	//we start with a uniform population with no hidden nodes
	//takes a populationSize which is the number of genomes to create
	public static Species setupInitialSpecies(int inputs, int outputs, int populationSize, InnovationManager iManager) {
		Genome baseGenome = new Genome(iManager);
		//create the input nodes
		for(int i = 1; i <= inputs; i++) {
			baseGenome.addNode(i, Node.INPUT);
		}
		
		//create the output nodes
		for(int i = 1; i <= outputs; i++) {
			baseGenome.addNode(inputs + i, Node.OUTPUT);
		}
		
		//create a connection from each bias/input to each output
		for(int i = 0; i <= inputs; i++) { //we start from 0 to include the bias node
			for(int j = 1; j <= outputs; j++) {
				baseGenome.addConnection(i, inputs + j, 1.0, true, iManager.getInnovationNumber(i, inputs + j));
			}
		}
		
		Species result = new Species(baseGenome, 0.0, 0);
		
		//add the initial population to the first species
		for(int i = 0; i < populationSize; i++){
			if(result.addGenome(baseGenome.cloneGenome()) == false){
				throw new GenomeException();
			}
		}
		return result;
	}
	
	//test the fitness of a genome for computing a function represented by the supplied DataSet
	//returns a fitness score out of 100, with 100 being a Genome that perfectly reproduces the DataSet, and 0 being a Genome that gets every result completely wrong
	//if the entries in the DataSet do not align with what is expected by the genome, this function will throw an exception
	protected static double testFitness(Genome g, DataSet d) {
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
	
	//runs the function implemented by the supplied genome with the specified inputs and returns the outputs
	//the number of inputs must match what is expected by the genome or this method will throw an exception
	//this function assumes that the genome has all its inputs in consecutive node positions starting from 1
	protected static Double[] runFunction(Genome g, Double[] inputs) {
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
		for(int i = 0; i < GlobalFunctions.depth; i++){
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
	
	public static void runSimulation() {
		DataSet dataset = null;
		try {
			dataset = new DataSet("./XOR.csv");
		} catch (DataFormatException e) {
			e.printStackTrace();
			System.exit(1);
		}
		InnovationManager iManager = new InnovationManager();
		ArrayList<Species> allSpecies = new ArrayList<Species>();
		
		//setup an initial uniform population in an initial species
		allSpecies.add(GlobalFunctions.setupInitialSpecies(2, 1, GlobalFunctions.initialPopulationSize, iManager));
		
		Genome globalChampion = null;
		
		//run the simulation for the configured number of generations
		for(int generation = 0; generation < GlobalFunctions.numberOfGenerations; generation ++){
			System.out.println("Starting generation " + generation);
						
			double globalFitnessSum = 0.0; //the sum of all average fitnesses of all species
			
			//test each genome in each species for its fitness for calculating XOR, calculate the average fitness for each species and the total sum of all average fitnesses
			for(int i = 0; i < allSpecies.size(); i++){
				ArrayList<Genome> currentGenomes = allSpecies.get(i).getGenomes();
				for(int j = 0; j < currentGenomes.size(); j++){
					Genome current = currentGenomes.get(j);
					current.setFitness(testFitness(current, dataset));
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
				System.out.println("Generation: " + generation + ", Species: " + i + ", Max fitness: " + maxFitness + ", Stagnant generations: " + generationsWithoutImprovement);
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
		
		//print the results produced by the best genome
		System.out.println("Simulation complete. The global champion produces the following results:");
		Double[] inputs1 = {0.0, 0.0};
		System.out.println("0,0: "+ runFunction(globalChampion, inputs1)[0]);
		Double[] inputs2 = {1.0, 1.0};
		System.out.println("1,1: "+ runFunction(globalChampion, inputs2)[0]);
		Double[] inputs3 = {0.0, 1.0};
		System.out.println("0,1: "+ runFunction(globalChampion, inputs3)[0]);
		Double[] inputs4 = {1.0, 0.0};
		System.out.println("1,0: "+ runFunction(globalChampion, inputs4)[0]);
		System.out.println("Fitness: " + globalChampion.getFitness());
		
		//write the global champion to a file so it can be retrieved later
		//the filename has a unique timestamp so it doesn't overwrite other genomes
		//the fitness comes first in the filename so that files can be sorted by fitness
		String timestamp = new Timestamp(System.currentTimeMillis()).toString().replace(' ', '.').replace(':', '.').replace('.', '-');
		String outputPath = "C:/genomes/genome-" + globalChampion.getFitness() + "-" + timestamp + ".json";
		JSONTools.writeGenomeToFile(globalChampion, outputPath, "Champion XOR genome, fitness: " + globalChampion.getFitness() + "/21^2");
		
		startRenderer(globalChampion);
		
		//verify that the JSON writer and parser are replicating the genome correctly
		String inputPath = "C:/genomes/genome-" + globalChampion.getFitness() + "-" + timestamp + ".json";
		Genome testGenome = JSONTools.readGenomeFromFile(inputPath);
		
		startRenderer(testGenome);
	}
	
	//start an instance of the renderer with the given genome
	protected static void startRenderer(Genome genome){
		final Renderer mApplication = new Renderer(genome);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				mApplication.initApp();
			}
		});
	}
}
