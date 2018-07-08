package com.neatnodes.algorithm;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.neatnodes.genome.Connection;
import com.neatnodes.genome.Genome;
import com.neatnodes.genome.Node;
import com.neatnodes.utils.Configuration;

/**
 * This class provides static methods that implement the behaviour required by the NEAT algorithm.
 * @author Sam Griffin
 */
class StaticFunctions {
	
	/**
	 * Calculates the compatibility distance between two Genomes.
	 * @param g1
	 * 		The first Genome.
	 * @param g2
	 * 		The second Genome.
	 * @param configuration
	 * 		A Configuration object that provides the parameters required by the algorithm.
	 * @return
	 * 		The computed compatibility distance.
	 */
	static double calculateCompatabilityDistance(Genome g1, Genome g2, Configuration configuration){
		//key variables used by this algorithm:
		//numberOfExcessGenes: the number of excess genes (the E term)
		//numberOfDisjointGenes: the number of disjoint genes (the D term)
		//averageWeightDifference: the average weight difference between matching genes, including disabled genes (the W bar term)
		//maxGenes: the number of genes in the larger Genome (N)
		
		HashMap<Integer, Connection> g1Genes = g1.getConnectionGenes();
		HashMap<Integer, Connection> g2Genes = g2.getConnectionGenes();
		
		int g1GenesSize = g1Genes.size();
		int g2GenesSize = g2Genes.size();
		
		if (g1GenesSize == 0 || g2GenesSize == 0){
			//the algorithm will not work for empty Genomes
			throw new RuntimeException("Cannot calculate compatibility distance between empty Genomes");
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
		double term1 = (configuration.EWeight * numberOfExcessGenes)/maxGenes;
		double term2 = (configuration.DWeight * numberOfDisjointGenes)/maxGenes;
		double term3 = configuration.WWeight * averageWeightDifference;
		return term1 + term2 + term3;
	}
	
	/**
	 * Breed two Genomes to create a new offspring Genome.
	 * @param father
	 * 		The first Genome to breed.
	 * @param mother
	 * 		The second Genome to breed.
	 * @param iManager
	 * 		The InnovationManager to use when selecting innovation numbers as part of the breeding process.
	 * @param configuration
	 * 		A Configuration object that provides the parameters required by the algorithm.
	 * @return
	 * 		The new Genome.
	 */
	static Genome breed(Genome father, Genome mother, InnovationManager iManager, Configuration configuration){
		Genome offspring = new Genome(iManager);
		
		//fail if the fitness of either parent has not been set
		if(!father.isFitnessMeasured() || !mother.isFitnessMeasured()){
			throw new RuntimeException("Cannot breed Genomes before their fitness has been set");
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
					if(Math.random() < configuration.disableMutationChance){
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
						if(Math.random() < configuration.disableMutationChance){
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
						if(Math.random() < configuration.disableMutationChance){
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
	
	/**
	 * Create a copy of a Connection and the Nodes it depends on and add them to another Genome.
	 * @param config
	 * 		The Connection to add.
	 * @param genome
	 * 		The Genome to add the Connection to.
	 * @param enabled
	 * 		If true, the Connection will be enabled in the new Genome. If false, it will be disabled.
	 */
	static void duplicateConnection(Connection config, Genome genome, boolean enabled){
		Node nodeToAdd1 = config.getInNode();
		Node nodeToAdd2 = config.getOutNode();
		
		//add the required nodes to the offspring if they don't already exist.
		if(genome.getNode(nodeToAdd1.getLabel()) == null){
			genome.addNode(nodeToAdd1.getLabel(), nodeToAdd1.getType());
		}
		if(genome.getNode(nodeToAdd2.getLabel()) == null){
			genome.addNode(nodeToAdd2.getLabel(), nodeToAdd2.getType());
		}
		
		//add the required connection to the offspring.
		genome.addConnection(nodeToAdd1.getLabel(), nodeToAdd2.getLabel(), config.getWeight(), enabled, config.getInnovationNumber());
	}
	
	/**
	 * Set up an initial population of identical Genomes in an initial Species. The template used for the Genomes will 
	 * have one bias node, plus the specified number of input and output nodes and no hidden nodes. Each bias and 
	 * input node will have a connection to each output node.
	 * @param inputs
	 * 		The number of input Nodes that the template Genome will have.
	 * @param outputs
	 * 		The number of output Nodes that the template Genome will have.
	 * @param populationSize
	 * 		The number of Genomes to create.
	 * @param iManager
	 * 		The InnovationManager to use when selecting innovation numbers as part of the Genome creation process.
	 * @param configuration
	 * 		A Configuration object that provides the parameters required by the algorithm.
	 * @return
	 * 		The new Species populated with Genomes.
	 */
	static Species setupInitialSpecies(int inputs, int outputs, int populationSize, InnovationManager iManager, Configuration configuration) {
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
		
		Species result = new Species(baseGenome, 0.0, 0, configuration);
		
		//add the initial population to the first species
		for(int i = 0; i < populationSize; i++){
			if(result.addGenome(baseGenome.cloneGenome()) == false){
				throw new RuntimeException("Cannot add Genome to Species. Genome does not fall within the required compatibility distance. This indicates a bug.");
			}
		}
		return result;
	}
}
