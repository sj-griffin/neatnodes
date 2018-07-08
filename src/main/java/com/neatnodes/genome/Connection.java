package com.neatnodes.genome;

/**
 * A directional link between two nodes. One of the basic elements used to construct Genomes. When it is fired, it takes 
 * the value being output by it's in-node , applies a weight to it, and sets it as an input to it's out-node.
 * 
 * @author Sam Griffin
 */
public class Connection {
	Node inNode;
	Node outNode;
	double weight;
	boolean enabled;
	int innovationNumber;
	
	/**
	 * Creates a Connection.
	 * @param inNode
	 * 		The Node that the Connection runs from.
	 * @param outNode
	 * 		The Node that the Connection runs to.
	 * @param weight
	 * 		The weight that will be applied to data flowing through the Connection
	 * @param enabled
	 * 		Whether the Connection is active or disabled.
	 * @param innovationNumber
	 * 		The innovation number used to mark the ancestry of the Connection.
	 */
	Connection(Node inNode, Node outNode, double weight, boolean enabled, int innovationNumber){
		this.inNode = inNode;
		this.outNode = outNode;
		this.weight = weight;
		this.enabled = enabled;
		this.innovationNumber = innovationNumber; //we take an innovation number as an argument rather than generating it because we need to be able to pass in specific innovations from previous generations after the innovation numbers from the previous generation have already been reset
	}

	/**
	 * Get the Connection's weight.
	 * @return
	 * 		The weight of the Connection.
	 */
	public double getWeight() {
		return weight;
	}

	/**
	 * Set the Connection's weight.
	 * @param weight
	 * 		The weight to set.
	 */
	public void setWeight(double weight) {
		this.weight = weight;
	}

	/**
	 * Check whether the Connection is enabled.
	 * @return
	 * 		True if the Connection is enabled.
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Set whether the Connection is enabled.
	 * @param enabled
	 * 		True to enable the Connection, false to disable it.
	 */
	void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Get the Node that the Connection starts at.
	 * @return
	 * 		The Node.
	 */
	public Node getInNode() {
		return inNode;
	}

	/**
	 * Get the Node that the Connection ends at.
	 * @return
	 * 		The Node.
	 */
	public Node getOutNode() {
		return outNode;
	}
	
	/**
	 * Get the Connection's innovation number.
	 * @return
	 * 		The innovation number.
	 */
	public int getInnovationNumber() {
		return innovationNumber;
	}
	
	/**
	 * Move the output from the in Node to the input of the out Node, applying the current weight. This 
	 * method will do nothing if the Connection is not enabled.
	 */
	void transfer(){
		//do nothing if the connection is not enabled
		if(!enabled){
			return;
		}
		
		double output = inNode.getValue() * weight;
		outNode.addInput(output);
	}
}
