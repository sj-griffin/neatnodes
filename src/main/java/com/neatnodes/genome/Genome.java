package com.neatnodes.genome;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.neatnodes.algorithm.InnovationManager;
import com.neatnodes.utils.Configuration;

/**
 * A Genome consists of Nodes and Connections and implements a function.
 * 
 * @author Sam Griffin
 */
public class Genome {
	HashMap<Integer, Node> nodeGenes; //nodeGenes keys correspond to labels, which should be numbered 0-n. they are stored in a hashmap to ensure that they can always be accessed by the same key
	HashMap<Integer, Connection> connectionGenes; //connectionGenes keys correspond to innovation numbers, which will be unique
	
	int numberOfInputs; //both inputs and biases are included in this value
	int numberOfOutputs;
	
	double fitness; //the fitness of the genome for its purpose
	boolean fitnessMeasured; //true if fitness has been measured. Once fitness has been measured, the genome is locked and no further changes to its design can occur.
	
	InnovationManager iManager; //the InnovationManager to retrieve innovation numbers from
	
	/**
	 * Creates a new Genome with a single bias node. After creating a Genome, input, hidden and output nodes must be 
	 * added manually. The bias node will be created automatically by this constructor. However, the bias node will 
	 * still need to be manually connected to other nodes. This ensures that every Genome has a bias node as it's node 0.
	 * @param iManager
	 * 		The InnovationManager that the Genome should use when creating it's Connections and reproducing.
	 */
	public Genome(InnovationManager iManager){
		this.iManager = iManager;
		nodeGenes = new HashMap<Integer, Node>();
		connectionGenes = new HashMap<Integer, Connection>();
		numberOfInputs = 0;
		numberOfOutputs = 0;
		
		
		fitness = 0.0;
		fitnessMeasured = false;
		
		//add the bias node. All Genomes have exactly one bias node as their node 0
		addNode(0, Node.BIAS);
		nodeGenes.get(0).setValue(1.0);
	}
	
	/**
	 * Get the number of input nodes in the Genome.
	 * @return
	 * 		The number of input nodes.
	 */
	public int getNumberOfInputs() {
		return numberOfInputs;
	}

	/**
	 * Get the number of output nodes in the Genome.
	 * @return
	 * 		The number of output nodes.
	 */
	public int getNumberOfOutputs() {
		return numberOfOutputs;
	}

	/**
	 * Get the Node with a given key in the Genome.
	 * @param n
	 * 		The key (label) of the Node.
	 * @return
	 * 		The Node.
	 */
	public Node getNode(int n){
		return nodeGenes.get(n);
	}
	
	/**
	 * Get the Connection with a given key in the Genome. The key will be the same as the Connection's innovation number.
	 * @param n
	 * 		The key (label) of the Connection.
	 * @return
	 * 		The Connection.
	 */
	public Connection getConnection(int n){
		return connectionGenes.get(n);
	}
	
	/**
	 * Get a HashMap containing all Nodes in the Genome.
	 * @return
	 * 		The map of Nodes.
	 */
	public HashMap<Integer, Node> getNodeGenes() {
		return nodeGenes;
	}

	/**
	 * Get a HashMap containing all Connections in the Genome.
	 * @return
	 * 		The map of Connections.
	 */
	public HashMap<Integer, Connection> getConnectionGenes() {
		return connectionGenes;
	}

	/**
	 * Adds a new node to the Genome.
	 * @param label
	 * 		The label for the Genome, which will also be the key used to reference it.
	 * @param type
	 * 		The Node type. Valid values are Node.BIAS, Node.INPUT, Node.OUTPUT, or Node.HIDDEN.
	 */
	public void addNode(int label, int type){
		//if the Genome has already been finalised, fail
		if(fitnessMeasured){
			throw new RuntimeException();
		}
		
		//if the type is not valid, fail
		if(type < 1 || type > 4){
			throw new RuntimeException();
		}
		
		//if a gene with the specified label already exists, fail
		if(nodeGenes.containsKey(label)){
			throw new RuntimeException();
		}
		
		//if this node is a second bias node, fail
		if(type == Node.BIAS && nodeGenes.containsKey(0)) {
			throw new RuntimeException();
		}
		
		nodeGenes.put(label, new Node(type, label));
		if(type == Node.INPUT){
			numberOfInputs ++;
		} else if (type == Node.OUTPUT){
			numberOfOutputs ++;
		}
	}
	

	/**
	 * Adds a new Connection to the Genome. Takes an innovation number as an argument rather than generating it 
	 * to allow for passing in specific innovation numbers from previous generations after the innovations from 
	 * the previous generation have already been reset.
	 * @param inNodeNumber
	 * 		The key {label) of an existing Node in the Genome to be the in Node of the Connection.
	 * @param outNodeNumber
	 * 		The key {label) of an existing Node in the Genome to be the out Node of the Connection.
	 * @param weight
	 * 		The weight that the Connection will have.
	 * @param enabled
	 * 		If true the Connection will be enabled.
	 * @param innovationNumber
	 * 		The innovation number for the connection. This will also be used as the key to reference the 
	 * 		Connection in the Genome.
	 */
	public void addConnection(int inNodeNumber, int outNodeNumber, double weight, boolean enabled, int innovationNumber){
		//if the genome has already been finalised, fail
		if(fitnessMeasured){
			throw new RuntimeException();
		}
		
		//if a gene with the specified innovationNumber already exists, fail
		if(connectionGenes.containsKey(innovationNumber)){
			throw new RuntimeException();
		}
		
		//the nodes being connected can be the same node as recursive connections are allowed
		
		//the nodes being connected must exist in the genome		
		Node inNode = nodeGenes.get(inNodeNumber);
		Node outNode = nodeGenes.get(outNodeNumber);
		
		if(inNode == null || outNode == null){
			throw new RuntimeException();
		}
		
		connectionGenes.put(innovationNumber, new Connection(inNode, outNode, weight, enabled, innovationNumber));
	}
	
	/**
	 * Applies a set of mutations to the Genome. Should be called after a new Genome is bred as part of the NEAT algorithm.
	 * @param configuration
	 * 		A Configuration object that parameters for the mutation will be drawn from.
	 */
	public void mutate(Configuration configuration){
		//if the genome has already been finalised, fail
		if(fitnessMeasured){
			throw new RuntimeException();
		}
		
		//do nothing if there are no genes to mutate
		if(connectionGenes.size() < 1){
			return;
		}
		
		//the chance of each type of mutation occurring are set by global variables
		if(Math.random() < configuration.weightMutationChance){
			mutateWeights();
		}
		if(Math.random() < configuration.nodeMutationChance){
			nodeMutation();
		}
		if(Math.random() < configuration.linkMutationChance){
			linkMutation();
		}
	}
	
	/**
	 * Performs a weight mutation. Should only be called internally and by unit tests.
	 */
	void mutateWeights(){
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
	
	/**
	 * Performs a node mutation. Should only be called internally and by unit tests.
	 */
	void nodeMutation(){
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
		int newNodeNumber = nodeGenes.size();
		addNode(newNodeNumber, Node.HIDDEN);
		
		//add two new connections in place of the disabled one
		addConnection(randomConnection.getInNode().getLabel(), newNodeNumber, 1.0, true, iManager.getInnovationNumber(randomConnection.getInNode().getLabel(), newNodeNumber));
		addConnection(newNodeNumber, randomConnection.getOutNode().getLabel(), randomConnection.getWeight(), true, iManager.getInnovationNumber(newNodeNumber, randomConnection.getOutNode().getLabel()));
	}
	
	/**
	 * Performs a link mutation. Should only be called internally and by unit tests.
	 */
	void linkMutation(){
		//check if the genome already contains all possible connections to avoid an infinite loop
		//number of possible connections = number of nodes * (number of nodes - number of input nodes)
		int possibleConnections = nodeGenes.size() * (nodeGenes.size() - numberOfInputs - 1); //the -1 is to account for the single bias node that occurs in all genomes
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
			
			//Note that recurrent connections (where a node connects to itself) are permitted
			
			//if all conditions have been met, add the connection and break out of the loop
			addConnection(randomNode1.getLabel(), randomNode2.getLabel(), Math.random()*4-2, true, iManager.getInnovationNumber(randomNode1.getLabel(), randomNode2.getLabel()));
			break;
		}
	}
	
	/**
	 * Check whether a Connection defined by two Nodes already exists in the Genome
	 * @param input
	 * 		The in Node of the Connection.
	 * @param output
	 * 		The out Node of the COnnection.
	 * @return
	 * 		True if the Connection exists in the Genome.
	 */
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
	
	
	/**
	 * Returns a clone of this Genome with all the same nodes and connections represented by new objects. The 
	 * new Genome will have no fitness set and be open for editing even if this one has already been locked.
	 * @return
	 * 		The clone of the Genome.
	 */
	public Genome cloneGenome(){
		Genome newGenome = new Genome(this.iManager);
		for (Map.Entry<Integer, Node> node : nodeGenes.entrySet()){

			Node n = node.getValue();
			if(n.getType() == Node.BIAS) {
				continue; //we do not manually re-add bias nodes as they will already have been created by the constructor
			}
			newGenome.addNode(n.getLabel(), n.getType());
		}
		
		for (Map.Entry<Integer, Connection> connection : connectionGenes.entrySet()){
			Connection c = connection.getValue();
			newGenome.addConnection(c.getInNode().getLabel(), c.getOutNode().getLabel(), c.getWeight(), c.isEnabled(), c.getInnovationNumber());
		}
		
		return newGenome;
	}
	
	/**
	 * Write a set of input values to the input nodes of the Genome in preparation for running it. This method 
	 * will throw an exception if you try to set the value of any non-input nodes.
	 * @param inputs
	 * 		A map containing keys mapping to input values. The keys must correspond to the keys of the input 
	 * 		nodes of the Genome, or an exception will be thrown.
	 */
	public void writeInputs(HashMap<Integer, Double> inputs){
		if (inputs.size() != numberOfInputs){
			//fail if the number of inputs is incorrect
			throw new RuntimeException();
		}
		
		for (Map.Entry<Integer, Double> input : inputs.entrySet()){
			//get the node with the specified label
			Node node = nodeGenes.get(input.getKey());
			//fail if the specified node doesn't exist or is not an input
			if (node == null || (node.getType() != Node.INPUT)){
				throw new RuntimeException();
			}
			node.setValue(input.getValue()); //set the input node
		}
	}
	
	/**
	 * Retrieve the current values on the output nodes of the Genome after running it.
	 * @return
	 * 		A map containing keys mapping to values. The keys correspond to the keys of the output nodes of 
	 * 		the Genome.
	 */
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
			throw new RuntimeException();
		}
		
		return outputs;
	}
	
	/**
	 * Run the Genome's whole network for one step.
	 */
	public void run(){
		//ensure that the bias node has a value of 1
		nodeGenes.get(0).setValue(1.0);
		
		//tell all connections to conduct a transfer
		for (Map.Entry<Integer, Connection> connection : connectionGenes.entrySet()){
			connection.getValue().transfer();
		}
		//tell all nodes to fire
		for (Map.Entry<Integer, Node> node : nodeGenes.entrySet()){
			node.getValue().fire();
		}
	}
	
	/**
	 * Sets all node values except the bias node back to 0. The bias node is set to 1.
	 */
	public void reset(){
		for (Map.Entry<Integer, Node> node : nodeGenes.entrySet()){
			node.getValue().reset();
		}
		nodeGenes.get(0).setValue(1.0);
	}
	
	/**
	 * Check if the Genome's fitness has already been measured.
	 * @return
	 * 		True if the fitness has been measured.
	 */
	public boolean isFitnessMeasured(){
		return fitnessMeasured;
	}
	
	/**
	 * Set a fitness value for the Genome. Calling this method will finalise the Genome so that it can 
	 * no longer be changed, as doing so would invalidate the fitness value.
	 * @param fitness
	 * 		The fitness value to set.
	 */
	public void setFitness(double fitness){
		this.fitness = fitness;
		fitnessMeasured = true;
	}
	
	/**
	 * Get the Genome's fitness value. This method will throw an exception if the fitness has not been set yet.
	 * @return
	 * 		The fitness value.
	 */
	public double getFitness(){
		//if the fitness has not been set, fail
		if(!fitnessMeasured){
			throw new RuntimeException();
		}
		return fitness;
	}
}
