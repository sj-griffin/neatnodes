package com.neatnodes.examples;

import java.sql.Timestamp;

import com.neatnodes.algorithm.InnovationManager;
import com.neatnodes.algorithm.Simulation;
import com.neatnodes.genome.Genome;
import com.neatnodes.utils.*;

public class Example {

	public static void main(String[] args) {
		InnovationManager iManager = new InnovationManager();
		Configuration configuration = new Configuration("./sample_config.txt");
		
		// create an XOR genome manually
		Genome genome = new Genome(iManager);
		
		genome.addNode(1, Node.INPUT);
		genome.addNode(2, Node.INPUT);
		genome.addNode(3, Node.OUTPUT);
		genome.addNode(4, Node.HIDDEN);

		genome.addConnection(0, 4, -0.36501593036936775, true, 1);
		genome.addConnection(1, 4, -1.046399730632218, true, 2);
		genome.addConnection(0, 3, 0.556513492344894, true, 3);
		genome.addConnection(4, 3, -0.48188502205686023, true, 4);
		genome.addConnection(2, 4, -0.576176328388935, true, 5);
		genome.addConnection(1, 3, 0.8995301845901448, true, 6);
		genome.addConnection(2, 3, -1.00442116184059, true, 7);
		genome.addConnection(3, 3, -1.0283375490454472, true, 8);
		genome.addConnection(3, 4, 2.0252790846258084, true, 9);
		genome.addConnection(4, 4, 0.24926976625922484, true, 10);
		
		//try running it manually
		Double[] inputs = {1.0, 0.0};
		Double[] outputs = Simulation.runFunction(genome, inputs, 2);
		System.out.println("Result for 1,0: " + outputs[0]);
		
		//test it against the XOR dataset
		DataSet dataset = new DataSet("./datasets/XOR.csv");
		double fitness = Simulation.testFitness(genome, dataset, 2);
		System.out.println("XOR fitness: " + fitness);
		
		//set up a simulation
		String functionName = "XOR";
		Genome testGenome = Simulation.runSimulation(dataset, configuration, false);
		
		//write the champion genome to a file so it can be retrieved later
		//the filename has a unique timestamp so it doesn't overwrite other genomes
		String timestamp = new Timestamp(System.currentTimeMillis()).toString().replace(' ', '.').replace(':', '.').replace('.', '-');
		String outputPath = "./genomes/" + functionName + "-" + testGenome.getFitness() + "-" + timestamp + ".json";
		JSONTools.writeGenomeToFile(testGenome, outputPath, "Champion " + functionName + " genome, depth: " + configuration.depth + ", fitness: " + testGenome.getFitness());
		
		//display the champion genome
		Simulation.viewGenome(testGenome, configuration);
	}
}
