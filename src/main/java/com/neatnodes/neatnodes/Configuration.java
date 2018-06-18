package com.neatnodes.neatnodes;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

//a class for generating Properties objects that are used to store configuration values
public class Configuration {
	//the chances of various mutations occuring
	private static final String defaultWeightMutationChance = "0.8";
	private static final String defaultNodeMutationChance = "0.03";
	private static final String defaultLinkMutationChance = "0.05";
	private static final String defaultDisableMutationChance = "0.75";
	
	//coefficients used to adjust the importance of the three factors used to calculate compatability distance
	private static final String defaultC1 = "1.0"; //importance of E
	private static final String defaultC2 = "1.0"; //importance of D
	private static final String defaultC3 = "0.4"; //importance of W bar
	
	private static final String defaultCompatabilityThreshold = "1.0"; //called dt in the paper. default 3.0
	
	private static final String defaultInitialPopulationSize = "150";
	
	private static final String defaultGenerations = "1000"; // the number of generations to run the simulation for
	
	private static final String defaultCrossoverProportion = "0.75"; //the fraction of offspring which are created by crossing two genomes. The rest are cloned from a single genome.
	
	private static final String defaultDepth = "3"; //controls the number of cycles to run each genome for before reading a result. It is the equivalent of the "depth" in a feed-forward network		
	
	protected final double weightMutationChance;
	protected final double nodeMutationChance;
	protected final double linkMutationChance;
	protected final double disableMutationChance;
	protected final double c1;
	protected final double c2;
	protected final double c3;
	protected final double compatabilityThreshold;
	protected final int initialPopulationSize;
	protected final int generations;
	protected final double crossoverProportion;
	protected final int depth;
	
	//takes a path to a properties files that will override the default values
	protected Configuration(String propertiesFile) throws IOException {
		//create default properties object
		Properties defaultProps = new Properties();
		defaultProps.setProperty("WEIGHT_MUTATION_CHANCE", defaultWeightMutationChance);
		defaultProps.setProperty("NODE_MUTATION_CHANCE", defaultNodeMutationChance);
		defaultProps.setProperty("LINK_MUTATION_CHANCE", defaultLinkMutationChance);
		defaultProps.setProperty("DISABLE_MUTATION_CHANCE", defaultDisableMutationChance);
		defaultProps.setProperty("C1", defaultC1);
		defaultProps.setProperty("C2", defaultC2);
		defaultProps.setProperty("C3", defaultC3);
		defaultProps.setProperty("COMPATABILITY_THRESHOLD", defaultCompatabilityThreshold);
		defaultProps.setProperty("INITIAL_POPULATION_SIZE", defaultInitialPopulationSize);
		defaultProps.setProperty("GENERATIONS", defaultGenerations);
		defaultProps.setProperty("CROSSOVER_PROPORTION", defaultCrossoverProportion);
		defaultProps.setProperty("DEPTH", defaultDepth);
		Properties properties = new Properties(defaultProps);

		//create properties object from the supplied file
		FileInputStream in = new FileInputStream(propertiesFile);
		properties.load(in);
		in.close();
				
		//load properties from either the supplied file or the defaults and cast them to the correct type
		try {
			this.weightMutationChance = Double.parseDouble(properties.getProperty("WEIGHT_MUTATION_CHANCE"));
			this.nodeMutationChance = Double.parseDouble(properties.getProperty("NODE_MUTATION_CHANCE"));
			this.linkMutationChance = Double.parseDouble(properties.getProperty("LINK_MUTATION_CHANCE"));
			this.disableMutationChance = Double.parseDouble(properties.getProperty("DISABLE_MUTATION_CHANCE"));
			this.c1 = Double.parseDouble(properties.getProperty("C1"));
			this.c2 = Double.parseDouble(properties.getProperty("C2"));
			this.c3 = Double.parseDouble(properties.getProperty("C3"));
			this.compatabilityThreshold = Double.parseDouble(properties.getProperty("COMPATABILITY_THRESHOLD"));
			this.initialPopulationSize = Integer.parseInt(properties.getProperty("INITIAL_POPULATION_SIZE"));
			this.generations = Integer.parseInt(properties.getProperty("GENERATIONS"));
			this.crossoverProportion = Double.parseDouble(properties.getProperty("CROSSOVER_PROPORTION"));
			this.depth = Integer.parseInt(properties.getProperty("DEPTH"));
		}
		catch(NumberFormatException e) {
			throw new RuntimeException("Invalid values found in configuration file");
		}
	}
	
	//when a properties file is not provided, uses the default values
	protected Configuration() {
		this.weightMutationChance = Double.parseDouble(Configuration.defaultWeightMutationChance);
		this.nodeMutationChance = Double.parseDouble(Configuration.defaultNodeMutationChance);
		this.linkMutationChance = Double.parseDouble(Configuration.defaultLinkMutationChance);
		this.disableMutationChance = Double.parseDouble(Configuration.defaultDisableMutationChance);
		this.c1 = Double.parseDouble(Configuration.defaultC1);
		this.c2 = Double.parseDouble(Configuration.defaultC2);
		this.c3 = Double.parseDouble(Configuration.defaultC3);
		this.compatabilityThreshold = Double.parseDouble(Configuration.defaultCompatabilityThreshold);
		this.initialPopulationSize = Integer.parseInt(Configuration.defaultInitialPopulationSize);
		this.generations = Integer.parseInt(Configuration.defaultGenerations);
		this.crossoverProportion = Double.parseDouble(Configuration.defaultCrossoverProportion);
		this.depth = Integer.parseInt(Configuration.defaultDepth);
	}
}
