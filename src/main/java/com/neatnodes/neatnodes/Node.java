package com.neatnodes.neatnodes;

import java.util.ArrayList;

class Node {
	protected static final int INPUT = 1;
	protected static final int OUTPUT = 2;
	protected static final int HIDDEN = 3;
	protected static final int BIAS = 4;
	
	private int type;
	private int label; //a label used to identify the node. Unique within a genome.
	
	private ArrayList<Double> inputs;
	private double value; //the value currently being output by the node
	
	protected Node (int type, int label){
		this.type = type;
		this.label = label;
		
		this.inputs = new ArrayList<Double>();
		this.value = 0.0;
	}

	protected int getType() {
		return type;
	}
	
	protected int getLabel() {
		return label;
	}
	
	//add a value to the inputs currently being received by the node
	protected void addInput(double input){
		inputs.add(input);
	}
	
	//return the inputs list. For use by unit tests only.
	protected ArrayList<Double> getInputs(){
		return this.inputs;
	}
	
	//return the value being output by the node
	protected double getValue(){
		return value;
	}
	
	//take all the current inputs and use the sigmoid function to set the output. Inputs are cleared once they are used.
	protected void fire(){
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
	
	
	protected void setValue(double value){
		if(type != INPUT && type != BIAS){
			//fail if something is trying to set a non-input/bias value
			throw new GenomeException();
		}
		this.value = value;
	}
	
	//return the value to 0.0
	protected void reset(){
		this.value = 0.0;
	}
	
}
