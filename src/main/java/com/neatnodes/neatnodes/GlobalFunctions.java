package com.neatnodes.neatnodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
	
	public static final int numberOfGenerations = 1500; // the number of generations to run the simulation for
	
	public static final double crossoverProportion = 0.75; //the fraction of offspring which are created by crossing two genomes. The rest are cloned from a single genome.
	
	public static final int depth = 3; //controls the number of cycles to run each genome for before reading a result. It is the equivalent of the "depth" in a feed-forward network
	
	private static int globalInnovationNumber = 1; //used to track gene history
	
	private static ArrayList<Innovation> currentInnovations = new ArrayList<Innovation>();
	
	//reset the list of innovations for a new generation
	public static void newGeneration(){
		currentInnovations = new ArrayList<Innovation>();
	}
	
	//add an innovation to the list of innovations for the current generation
	public static void addInnovation(Innovation i){
		currentInnovations.add(i);
	}
	
	/**
	*checks to see if the connection defined by the two nodes is equivalent to an existing innovation from the current generation
	*if it is, return its innovation number
	*if not, return the global innovation number and add it to the list of innovations
	*
	*WARNING: be careful using this method when setting up the initial genomes. Make sure equivalent nodes are actually numbered the same or it will stuff up the whole run.
	**/
	public static int getInnovationNumber(int inNodeLabel, int outNodeLabel){
		for(int i = 0; i < currentInnovations.size(); i++){
			if(currentInnovations.get(i).isEquivalent(inNodeLabel, outNodeLabel)){
				return currentInnovations.get(i).getInnovationNumber();
			}
		}
		
		//if the innovation is new, record it and assign it a new innovation number
		globalInnovationNumber ++;
		currentInnovations.add(new Innovation(globalInnovationNumber, inNodeLabel, outNodeLabel));
		return globalInnovationNumber;
	}
	
	
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
	public static Genome breed(Genome father, Genome mother){
		Genome offspring = new Genome();
		
		//fail if the fitness of either parent has not been set
		if(!father.isFitnessMeasured() || !mother.isFitnessMeasured()){
			throw new GenomeException();
		}
		
		//calculate who is the fitter parent
		//this code doesn't take in to account the case where parents are equally fit like the paper does, but this is a rare case with virtually no effect, and it is simpler to program it this way
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
		
		int geneNumber = 1;
		
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
	protected static void duplicateConnection(Connection c, Genome g, boolean enabled){
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
	
}
