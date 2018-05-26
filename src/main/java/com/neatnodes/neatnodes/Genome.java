package com.neatnodes.neatnodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Genome {
	private HashMap<Integer, Node> nodeGenes; //nodeGenes keys correspond to labels, which should be numbered 1-n. they are stored in a hashmap to allow them to be added in any order
	private HashMap<Integer, Connection> connectionGenes; //connectionGenes keys correspond to innovation numbers, which will be unique
	
	private int numberOfInputs; //both inputs and biases are included in this value
	private int numberOfOutputs;
	
	private double fitness; //the fitness of the genome for its purpose
	private boolean fitnessMeasured; //true if fitness has been measured. Once fitness has been measured, the genome is locked and no further changes to its design can occur.
	
	public Genome(){
		nodeGenes = new HashMap<Integer, Node>();
		connectionGenes = new HashMap<Integer, Connection>();
		numberOfInputs = 0;
		numberOfOutputs = 0;
		
		fitness = 0.0; //fitness is considered to be not set if it is negative
		fitnessMeasured = false;
	}
	
	public int getNumberOfInputs() {
		return numberOfInputs;
	}

	public int getNumberOfOutputs() {
		return numberOfOutputs;
	}

	public Node getNode(int n){
		return nodeGenes.get(n);
	}
	
	public Connection getConnection(int n){
		return connectionGenes.get(n);
	}
	
	public HashMap<Integer, Node> getNodeGenes() {
		return nodeGenes;
	}

	public HashMap<Integer, Connection> getConnectionGenes() {
		return connectionGenes;
	}

	//adds a new node defined by the arguments and returns true if successful
	public void addNode(int label, int type){
		//if the genome has already been finalised, fail
		if(fitnessMeasured){
			throw new GenomeException();
		}
		
		//if the type is not valid, fail
		if(type < 1 || type > 4){
			throw new GenomeException();
		}
		
		//if a gene with the specified number already exists, fail
		if(nodeGenes.containsKey(label)){
			throw new GenomeException();
		}
		
		nodeGenes.put(label, new Node(type, label));
		if(type == Node.INPUT || type == Node.BIAS){
			numberOfInputs ++;
		} else if (type == Node.OUTPUT){
			numberOfOutputs ++;
		}
	}
	
	//adds a new connection defined by the arguments and returns true if successful
	//in and out nodes are defined by their numbers in the genome to ensure that the nodes exist in the genome
	public void addConnection(int inNodeNumber, int outNodeNumber, double weight, boolean enabled, int innovationNumber){
		//if the genome has already been finalised, fail
		if(fitnessMeasured){
			throw new GenomeException();
		}
		
		//if a gene with the specified innovationNumber already exists, fail
		if(connectionGenes.containsKey(innovationNumber)){
			throw new GenomeException();
		}
		
		//the nodes being connected can be the same node as recursive connections are allowed
		
		//the nodes being connected must exist in the genome
		Node inNode = null;
		Node outNode = null;
		
		inNode = nodeGenes.get(inNodeNumber);
		outNode = nodeGenes.get(outNodeNumber);
		
		if(inNode == null || outNode == null){
			throw new GenomeException();
		}
		
		connectionGenes.put(innovationNumber, new Connection(inNode, outNode, weight, enabled, innovationNumber));
	}
	
	//applies a set of mutations to the genome. Should be called after a new genome is bred.
	public void mutate(){
		//if the genome has already been finalised, fail
		if(fitnessMeasured){
			throw new GenomeException();
		}
		
		//do nothing if there are no genes to mutate
		if(connectionGenes.size() < 1){
			return;
		}
		
		//the chance of each type of mutation occuring are set by global variables
		if(Math.random() < GlobalFunctions.weightMutationChance){
			mutateWeights();
		}
		if(Math.random() < GlobalFunctions.nodeMutationChance){
			nodeMutation();
		}
		if(Math.random() < GlobalFunctions.linkMutationChance){
			linkMutation();
		}
	}
	
	private void mutateWeights(){
		//this function is based on the pointMutate function of the reference code
		for (Map.Entry<Integer, Connection> connection : connectionGenes.entrySet()){
			//each gene has a 90% chance of being perturbed and a 10% chance of being assigned a new random value
			if(Math.random() < 0.9){
				double currentWeight = connection.getValue().getWeight();
				connection.getValue().setWeight(currentWeight + Math.random() * 0.1 * 2 - 0.1); //increase the existing weight by a random value between -0.1 and 0.1
			} else {
				connection.getValue().setWeight(Math.random() * 4 - 2); //set a new random value between -2 and 2
			}
		}
	}
	
	private void nodeMutation(){
		//pick a random connection
		Random random = new Random();
		ArrayList<Integer> keys = new ArrayList<Integer>(connectionGenes.keySet());
		int randomKey = keys.get(random.nextInt(keys.size()));
		Connection randomConnection = connectionGenes.get(randomKey);
		
		//if the connection is already disabled, do nothing further
		if(!randomConnection.isEnabled()){
			return;
		}
		
		//disable the chosen connection
		randomConnection.setEnabled(false);
		
		//add a new node
		int newNodeNumber = nodeGenes.size() + 1;
		addNode(newNodeNumber, Node.HIDDEN);
		
		//add two new connections in place of the disabled one
		addConnection(randomConnection.getInNode().getLabel(), newNodeNumber, 1.0, true, GlobalFunctions.getInnovationNumber(randomConnection.getInNode().getLabel(), newNodeNumber));
		addConnection(newNodeNumber, randomConnection.getOutNode().getLabel(), randomConnection.getWeight(), true, GlobalFunctions.getInnovationNumber(newNodeNumber, randomConnection.getOutNode().getLabel()));
	}
	
	private void linkMutation(){
		//check if the genome already contains all possible connections to avoid an infinite loop
		//number of possible connections = number of nodes * (number of nodes - number of input nodes)
		int possibleConnections = nodeGenes.size() * (nodeGenes.size() - numberOfInputs);
		if(connectionGenes.size() >= possibleConnections){
			return;
		}
		
		//continuously attempt to pick two random nodes until they meet the criteria for a new connection
		while(true){
			Random random = new Random();
			ArrayList<Integer> keys = new ArrayList<Integer>(nodeGenes.keySet());
			int randomKey1 = keys.get(random.nextInt(keys.size()));
			int randomKey2 = keys.get(random.nextInt(keys.size()));
			
			Node randomNode1 = nodeGenes.get(randomKey1);
			Node randomNode2 = nodeGenes.get(randomKey2);
			
			//if the nodes are both inputs/biases, pick again
			if((randomNode1.getType() == Node.INPUT || randomNode1.getType() == Node.BIAS) && (randomNode2.getType() == Node.INPUT || randomNode2.getType() == Node.BIAS)){
				continue;
			}
			
			//if the second node is an input or a bias, swap them
			if(randomNode2.getType() == Node.INPUT || randomNode2.getType() == Node.BIAS){
				Node temp = randomNode2;
				randomNode2 = randomNode1;
				randomNode1 = temp;
			}
			
			
			//if the nodes already have a connection between them in the same direction, pick again
			if(containsLink(randomNode1, randomNode2)){
				continue;
			}
			
			//if all conditions have been met, add the connection and break out of the loop
			addConnection(randomNode1.getLabel(), randomNode2.getLabel(), Math.random()*4-2, true, GlobalFunctions.getInnovationNumber(randomNode1.getLabel(), randomNode2.getLabel()));
			break;
		}
	}
	
	//returns true if a connection defined by two nodes already exists in the genome
	private boolean containsLink(Node input, Node output){
		for (Map.Entry<Integer, Connection> connection : connectionGenes.entrySet()){
			Node inNode = connection.getValue().getInNode();
			Node outNode = connection.getValue().getOutNode();
			if(inNode == input && outNode == output){
				return true;
			}
		}
		return false;
	}
	
	//returns a clone of this genome with all the same nodes and connections represented by new objects. The new genome will have no fitness and be open for editing even if this one has already been locked.
	public Genome cloneGenome(){
		Genome newGenome = new Genome();
		for (Map.Entry<Integer, Node> node : nodeGenes.entrySet()){
			Node n = node.getValue();
			newGenome.addNode(n.getLabel(), n.getType());
		}
		
		for (Map.Entry<Integer, Connection> connection : connectionGenes.entrySet()){
			Connection c = connection.getValue();
			newGenome.addConnection(c.getInNode().getLabel(), c.getOutNode().getLabel(), c.getWeight(), c.isEnabled(), c.getInnovationNumber());
		}
		
		return newGenome;
	}
	
	//takes a hashmap mapping node labels to values and sets the values on the input and bias nodes
	//this works because node labels remain consistent across generations
	public void writeInputs(HashMap<Integer, Double> inputs){
		if (inputs.size() != numberOfInputs){
			//fail if the number of inputs is incorrect
			throw new GenomeException();
		}
		
		for (Map.Entry<Integer, Double> input : inputs.entrySet()){
			//get the node with the specified label
			Node node = nodeGenes.get(input.getKey());
			//fail if the specified node doesn't exist or is not an input/bias
			if (node == null || (node.getType() != Node.INPUT && node.getType() != Node.BIAS)){
				throw new GenomeException();
			}
			node.setValue(input.getValue()); //set the input node
		}
	}
	
	//returns a hashmap mapping node labels to values representing the current outputs of the genome
	//this works because node labels remain consistent across generations
	public HashMap<Integer, Double> readOutputs(){
		HashMap<Integer, Double> outputs = new HashMap<Integer, Double>();
		
		for (Map.Entry<Integer, Node> node : nodeGenes.entrySet()){
			//if the node is an output, add it to the hashmap to be returned
			Node n = node.getValue();
			if(n.getType() == Node.OUTPUT){
				outputs.put(n.getLabel(), n.getValue());
			}
		}
		
		if(outputs.size() != numberOfOutputs){
			throw new GenomeException();
		}
		
		return outputs;
	}
	
	//run the whole network for one step
	public void run(){
		//tell all connections to conduct a transfer
		for (Map.Entry<Integer, Connection> connection : connectionGenes.entrySet()){
			connection.getValue().transfer();
		}
		//tell all nodes to fire
		for (Map.Entry<Integer, Node> node : nodeGenes.entrySet()){
			node.getValue().fire();
		}
	}
	
	//sets all values back to 0
	public void reset(){
		for (Map.Entry<Integer, Node> node : nodeGenes.entrySet()){
			node.getValue().reset();
		}
	}
	
	public boolean isFitnessMeasured(){
		return fitnessMeasured;
	}
	
	public void setFitness(double fitness){
		this.fitness = fitness;
		fitnessMeasured = true;
	}
	
	public double getFitness(){
		//if the fitness has not been set, fail
		if(!fitnessMeasured){
			throw new GenomeException();
		}
		return fitness;
	}
}
