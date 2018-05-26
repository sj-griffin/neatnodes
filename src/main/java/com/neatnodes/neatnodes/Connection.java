package com.neatnodes.neatnodes;

public class Connection {
	private Node inNode;
	private Node outNode;
	private double weight;
	private boolean enabled;
	private int innovationNumber;
	
	public Connection(Node inNode, Node outNode, double weight, boolean enabled, int innovationNumber){
		this.inNode = inNode;
		this.outNode = outNode;
		this.weight = weight;
		this.enabled = enabled;
		this.innovationNumber = innovationNumber;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Node getInNode() {
		return inNode;
	}

	public Node getOutNode() {
		return outNode;
	}

	public int getInnovationNumber() {
		return innovationNumber;
	}
	
	//move the output from the input node to the input of the output node, applying the current weight
	public void transfer(){
		//do nothing if the connection is not enabled
		if(!enabled){
			return;
		}
		
		double output = inNode.getValue() * weight;
		outNode.addInput(output);
	}
}
