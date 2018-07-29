package com.neatnodes.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

//
/**
 * Configuration objects parse configuration files and store the parameters used by the NEAT algorithm.
 * Configuration files must follow the Java property file format. The following is a list of valid properties 
 * that will be picked up by this class and their default values:
 * <pre>
 * 
 * WEIGHT_MUTATION_CHANCE=0.8
 * NODE_MUTATION_CHANCE=0.03
 * LINK_MUTATION_CHANCE=0.05
 * DISABLE_MUTATION_CHANCE=0.75
 * E_WEIGHT=1.0
 * D_WEIGHT=1.0
 * W_WEIGHT=0.4
 * COMPATABILITY_THRESHOLD=1.0
 * INITIAL_POPULATION_SIZE=150
 * GENERATIONS=1000
 * CROSSOVER_PROPORTION=0.75
 * DEPTH=3
 * STYLE_PATH={jar location}/styles
 * RENDER_STYLE=normal
 * </pre>
 * 
 * @author Sam Griffin
 */
public class Configuration {
	static final String defaultWeightMutationChance = "0.8";
	static final String defaultNodeMutationChance = "0.03";
	static final String defaultLinkMutationChance = "0.05";
	static final String defaultDisableMutationChance = "0.75";
	static final String defaultEWeight = "1.0";
	static final String defaultDWeight = "1.0";
	static final String defaultWWeight = "0.4";
	static final String defaultCompatabilityThreshold = "1.0";
	static final String defaultInitialPopulationSize = "150";
	static final String defaultGenerations = "1000";
	static final String defaultCrossoverProportion = "0.75";
	static final String defaultDepth = "3";
	static final String defaultRenderStyle = "normal";
	
	/**
	 * The chance of a weight mutation occurring.
	 */
	public final double weightMutationChance;
	/**
	 * The chance of a node mutation occurring.
	 */
	public final double nodeMutationChance;
	/**
	 * The chance of a link mutation occurring.
	 */
	public final double linkMutationChance;
	/**
	 * The chance of a disable mutation occurring.
	 */
	public final double disableMutationChance;
	
	/**
	 * A coefficient used to adjust the importance of the E term when calculating compatibility distance.
	 */
	public final double EWeight;
	
	/**
	 * A coefficient used to adjust the importance of the D term when calculating compatibility distance.
	 */
	public final double DWeight;
	
	/**
	 * A coefficient used to adjust the importance of the "W bar" term when calculating compatibility distance.
	 */
	public final double WWeight;
	
	/**
	 * The compatibility threshold used to determine whether a Genome qualifies as a member of a Species.
	 */
	public final double compatabilityThreshold;
	
	/**
	 * The number of Genomes in the initial population used to begin the NEAT algorithm.
	 */
	public final int initialPopulationSize;
	
	/**
	 * The number of generations to run the NEAT algorithm for
	 */
	public final int generations;
	
	/**
	 *  The fraction of offspring which are created by crossing two Genomes. The rest are cloned from a 
	 *  single Genome.
	 */
	public final double crossoverProportion;
	
	/**
	 * The number of cycles to run each Genome for before reading a result. It is the equivalent of the 
	 * "depth" in a feed-forward network.
	 */
	public final int depth;
	
	/**
	 * The full path that the GenomeRenderer will look for style sheets in. Must not be a relative path. If you use backslashes, you must escape them (e.g. \\).
	 */
	public final String stylePath;
	
	/**
	 * The name of the style that the GenomeRenderer will use to display Genomes. The name must correspond 
	 * to a style sheet.
	 */
	public final String renderStyle;
	
	/**
	 * Creates a new Configuration from a properties file. Any parameters not listed in the file will be
	 * assigned their default values.
	 * @param propertiesFile
	 * 		The path to the properties file.
	 */
	public Configuration(String propertiesFile) {
		//create default properties object
		Properties defaultProps = new Properties();
		defaultProps.setProperty("WEIGHT_MUTATION_CHANCE", defaultWeightMutationChance);
		defaultProps.setProperty("NODE_MUTATION_CHANCE", defaultNodeMutationChance);
		defaultProps.setProperty("LINK_MUTATION_CHANCE", defaultLinkMutationChance);
		defaultProps.setProperty("DISABLE_MUTATION_CHANCE", defaultDisableMutationChance);
		defaultProps.setProperty("E_WEIGHT", defaultEWeight);
		defaultProps.setProperty("D_WEIGHT", defaultDWeight);
		defaultProps.setProperty("W_WEIGHT", defaultWWeight);
		defaultProps.setProperty("COMPATABILITY_THRESHOLD", defaultCompatabilityThreshold);
		defaultProps.setProperty("INITIAL_POPULATION_SIZE", defaultInitialPopulationSize);
		defaultProps.setProperty("GENERATIONS", defaultGenerations);
		defaultProps.setProperty("CROSSOVER_PROPORTION", defaultCrossoverProportion);
		defaultProps.setProperty("DEPTH", defaultDepth);
		
		//set the default style path dynamically depending on the location of the containing jar file
		String sPath;
		try {
			sPath =  new File(Configuration.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent() + "/styles";
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new RuntimeException("Error determining default style path");
		}
		defaultProps.setProperty("STYLE_PATH", sPath);
		
		defaultProps.setProperty("RENDER_STYLE", defaultRenderStyle);
		
		Properties properties = new Properties(defaultProps);

		//create properties object from the supplied file
		
		FileInputStream in;
		try {
			in = new FileInputStream(propertiesFile);
			properties.load(in);
			in.close();
		} catch (IOException e) {
			throw new RuntimeException("Unable to read the properties file. This may mean that the filepath is invalid.");
		}

		//load properties from either the supplied file or the defaults and cast them to the correct type
		try {
			this.weightMutationChance = Double.parseDouble(properties.getProperty("WEIGHT_MUTATION_CHANCE"));
			this.nodeMutationChance = Double.parseDouble(properties.getProperty("NODE_MUTATION_CHANCE"));
			this.linkMutationChance = Double.parseDouble(properties.getProperty("LINK_MUTATION_CHANCE"));
			this.disableMutationChance = Double.parseDouble(properties.getProperty("DISABLE_MUTATION_CHANCE"));
			this.EWeight = Double.parseDouble(properties.getProperty("E_WEIGHT"));
			this.DWeight = Double.parseDouble(properties.getProperty("D_WEIGHT"));
			this.WWeight = Double.parseDouble(properties.getProperty("W_WEIGHT"));
			this.compatabilityThreshold = Double.parseDouble(properties.getProperty("COMPATABILITY_THRESHOLD"));
			this.initialPopulationSize = Integer.parseInt(properties.getProperty("INITIAL_POPULATION_SIZE"));
			this.generations = Integer.parseInt(properties.getProperty("GENERATIONS"));
			this.crossoverProportion = Double.parseDouble(properties.getProperty("CROSSOVER_PROPORTION"));
			this.depth = Integer.parseInt(properties.getProperty("DEPTH"));
			this.stylePath = properties.getProperty("STYLE_PATH");
			this.renderStyle = properties.getProperty("RENDER_STYLE");
		}
		catch(NumberFormatException e) {
			throw new RuntimeException("Invalid values found in configuration file");
		}
	}
	
	/**
	 * Creates a new Configuration with all parameters set to their default values.
	 */
	public Configuration() {
		this.weightMutationChance = Double.parseDouble(Configuration.defaultWeightMutationChance);
		this.nodeMutationChance = Double.parseDouble(Configuration.defaultNodeMutationChance);
		this.linkMutationChance = Double.parseDouble(Configuration.defaultLinkMutationChance);
		this.disableMutationChance = Double.parseDouble(Configuration.defaultDisableMutationChance);
		this.EWeight = Double.parseDouble(Configuration.defaultEWeight);
		this.DWeight = Double.parseDouble(Configuration.defaultDWeight);
		this.WWeight = Double.parseDouble(Configuration.defaultWWeight);
		this.compatabilityThreshold = Double.parseDouble(Configuration.defaultCompatabilityThreshold);
		this.initialPopulationSize = Integer.parseInt(Configuration.defaultInitialPopulationSize);
		this.generations = Integer.parseInt(Configuration.defaultGenerations);
		this.crossoverProportion = Double.parseDouble(Configuration.defaultCrossoverProportion);
		this.depth = Integer.parseInt(Configuration.defaultDepth);
		
		//set the default style path dynamically depending on the location of the containing jar file
		String sPath;
		try {
			sPath =  new File(Configuration.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent() + "/styles";
		} catch (URISyntaxException e) {
			e.printStackTrace();
			throw new RuntimeException("Error determining default style path");
		}
		this.stylePath = sPath;
				
		this.renderStyle = Configuration.defaultRenderStyle;
	}
}
