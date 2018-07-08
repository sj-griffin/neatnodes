package com.neatnodes.algorithm;

/**
 * A class used by an InnovationManager to track genetic innovations. It can be used to test whether two Connections are 
 * equivalent in order to ensure that equivalent mutations in the same generation are assigned the same innovation 
 * number, as specified by the NEAT algorithm. Two Connections are considered equivalent if they share the same in and 
 * out node labels.
 * @author Sam Griffin
 */
class Innovation {
	int innovationNumber;
	int inNodeLabel; //the label of the incoming node to the connection
	int outNodeLabel; //the label of the outgoing node to the connection
	
	/**
	 * Creates a new Innovation.
	 * @param innovationNumber
	 * 		The innovation number assigned to this Innovation.
	 * @param inNodeLabel
	 * 		The label of the in-node.
	 * @param outNodeLabel
	 * 		The label of the out-node.
	 */
	Innovation(int innovationNumber, int inNodeLabel, int outNodeLabel){
		this.innovationNumber = innovationNumber;
		this.inNodeLabel = inNodeLabel;
		this.outNodeLabel = outNodeLabel;
	}
	
	/**
	 * Check if a potential new Connection represents the same innovation defined by this object.
	 * @param inNode
	 * 		The in-node of the Connection.
	 * @param outNode
	 * 		The out-node of the Connection.
	 * @return
	 * 		True if the innovation is equivalent to the Connection.
	 */
	boolean isEquivalent(int inNode, int outNode){
		if(inNode == this.inNodeLabel && outNode == this.outNodeLabel){
			return true;
		}
		return false;
	}
	
	/**
	 * Get the innovation number of this Innovation.
	 * @return
	 * 		The innovation number.
	 */
	int getInnovationNumber(){
		return this.innovationNumber;
	}
	
}
