package com.neatnodes.neatnodes;

public class Innovation {
	//a class used to ensure that equivalent mutations in the same generation are assigned the same innovation number
	//a coonection is equivalent if it shares the same in and out nodes
	
	private int innovationNumber;
	private int inNodeLabel; //the label of the incoming node to the connection
	private int outNodeLabel; //the label of the outgoing node to the connection
	
	public Innovation(int innovationNumber, int inNodeLabel, int outNodeLabel){
		this.innovationNumber = innovationNumber;
		this.inNodeLabel = inNodeLabel;
		this.outNodeLabel = outNodeLabel;
	}
	
	//returns true if the innovation is equivalent to a connection defined by an in node and an out node
	public boolean isEquivalent(int inNode, int outNode){
		if(inNode == this.inNodeLabel && outNode == this.outNodeLabel){
			return true;
		}
		return false;
	}
	
	public int getInnovationNumber(){
		return this.innovationNumber;
	}
	
}
