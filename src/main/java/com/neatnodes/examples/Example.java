package com.neatnodes.examples;

import java.sql.Timestamp;

import com.neatnodes.algorithm.Simulation;
import com.neatnodes.genome.Genome;
import com.neatnodes.utils.*;

public class Example {

	public static void main(String[] args) {
		String functionName = "XOR";
		Configuration configuration = new Configuration("./sample_config.txt");
		DataSet dataset = new DataSet("./datasets/" + functionName + ".csv");

		Genome testGenome = Simulation.runSimulation(dataset, configuration, true); //with defaults
		//Genome testGenome = runSimulation("./datasets/" + functionName + ".csv", configuration, true); //with custom config
		
		//write the global champion to a file so it can be retrieved later
		//the filename has a unique timestamp so it doesn't overwrite other genomes
		//the fitness comes first in the filename so that files can be sorted by fitness
		String timestamp = new Timestamp(System.currentTimeMillis()).toString().replace(' ', '.').replace(':', '.').replace('.', '-');
		String outputPath = "C:/genomes/" + functionName + "-" + testGenome.getFitness() + "-" + timestamp + ".json";
		JSONTools.writeGenomeToFile(testGenome, outputPath, "Champion " + functionName + " genome, fitness: " + testGenome.getFitness());
		
		//Genome testGenome = JSONTools.readGenomeFromFile("C:/genomes/XOR-93.12877329362878-2018-06-23-11-17-09-02.json");
		Simulation.viewGenome(testGenome, configuration); //display the champion genome
	}
}
