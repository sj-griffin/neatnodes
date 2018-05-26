package com.neatnodes.neatnodes;

import java.util.ArrayList;

public class Node {
	public static final int INPUT = 1;
	public static final int OUTPUT = 2;
	public static final int HIDDEN = 3;
	public static final int BIAS = 4;
	
	private int type;
	private int label; //a label used to identify the node. Unique within a genome.
	
	private ArrayList<Double> inputs;
	private double value; //the value currently being output by the node
	
	public Node (int type, int label){
		this.type = type;
		this.label = label;
		
		this.inputs = new ArrayList<Double>();
		this.value = 0.0;
	}

	public int getType() {
		return type;
	}
	
	public int getLabel() {
		return label;
	}
	
	//add a value to the inputs currently being received by the node
	public void addInput(double input){
		inputs.add(input);
	}
	
	//return the value being output by the node
	public double getValue(){
		return value;
	}
	
	//take all the current inputs and use the sigmoid function to set the output. Inputs are cleared once they are used.
	public void fire(){
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
		//return 2/(1+Math.exp(-4.9*x))-1; //modified sigmoid from example code (returns results between -1 and 1)
		return 1/(1+Math.exp(-4.9*x)); //sigmoid from paper (returns results between 0 and 1)
	}
	
	
	public void setValue(double value){
		if(type != INPUT && type != BIAS){
			//fail if something is trying to set a non-input/bias value
			throw new GenomeException();
		}
		this.value = value;
	}
	
	//return the value to 0.0
	public void reset(){
		this.value = 0.0;
	}
	
}
