package com.neatnodes.genome;

import java.util.ArrayList;

/**
 * A Node is the basic element used to construct Genomes. It takes inputs and applies a sigmoid function to 
 * process them into a single output value when it is fired. The "value" of the Node refers to the value that 
 * it is currently outputting.
 * 
 * @author Sam Griffin
 */
public class Node {
	public static final int INPUT = 1;
	public static final int OUTPUT = 2;
	public static final int HIDDEN = 3;
	public static final int BIAS = 4;
	
	int type;
	int label; //a label used to identify the node. Unique within a genome.
	
	ArrayList<Double> inputs;
	double value; //the value currently being output by the node
	
	/**
	 * Creates a new node.
	 * @param type
	 * 		The node type. Valid values are Node.INPUT, Node.OUTPUT, Node.HIDDEN, or Node.BIAS.
	 * @param label
	 * 		The label for the Node.
	 */
	Node (int type, int label){
		this.type = type;
		this.label = label;
		
		this.inputs = new ArrayList<Double>();
		this.value = 0.0;
	}

	/**
	 * Get the node type. The result of this method can be checked against Node.INPUT, Node.OUTPUT, 
	 * Node.HIDDEN, and Node.BIAS to see which one it matches.
	 * @return
	 * 		The type.
	 */
	public int getType() {
		return type;
	}
	
	/**
	 * Get the node label.
	 * @return
	 * 		The label.
	 */
	public int getLabel() {
		return label;
	}
	
	/**
	 * Add a value to the inputs currently being received by the node. These values will be processed the next
	 * time the Node fires. There is no limit to the number of inputs values a Node can have simultaneously.
	 * @param input
	 * 		The input value.
	 */
	void addInput(double input){
		inputs.add(input);
	}
	
	/**
	 * Get a list of all inputs being received by the Node. For use by unit tests only.
	 * @return
	 * 		The list of inputs.
	 */
	ArrayList<Double> getInputs(){
		return this.inputs;
	}
	
	/**
	 * Get the value currently being output by the Node.
	 * @return
	 * 		The output value.
	 */
	double getValue(){
		return value;
	}
	
	/**
	 * Take all the current inputs and apply a sigmoid function to set a new value for the Node. Inputs are cleared 
	 * once they have been processed.
	 */
	void fire(){
		//do nothing if the node is an input or bias node or there are no inputs
		if(type == INPUT || type == BIAS || inputs.isEmpty()){
			return;
		}
		
		double x = 0.0;
		//sum all values on the inputs
		for(double n : inputs){
			x += n;
		}
		//apply the sigmoid function to the sum to set the output.
		this.value = sigmoid(x);
		
		//clear all inputs
		inputs = new ArrayList<Double>();
	}
	
	
	private double sigmoid(double x){
		return 1/(1+Math.exp(-4.9*x)); //sigmoid function (returns results between 0 and 1)
	}
	
	
	/**
	 * Set the value currently being output by the Node.
	 * @param value
	 * 		The new node value.
	 */
	void setValue(double value){
		if(type != INPUT && type != BIAS){
			//fail if something is trying to set a non-input/bias value
			throw new RuntimeException("Cannot set the value for a non input/bias node.");
		}
		this.value = value;
	}
	
	/**
	 * Reset the Node's value to 0.0
	 */
	void reset(){
		this.value = 0.0;
	}
	
}
