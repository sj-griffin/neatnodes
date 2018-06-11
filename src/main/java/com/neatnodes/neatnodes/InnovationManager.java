package com.neatnodes.neatnodes;

import java.util.ArrayList;

class InnovationManager {
	private int currentInnovationNumber; //used to track gene history
	private ArrayList<Innovation> currentInnovations; //stores the innovations created during the current generation so that the same innovation number can be applied to equivalent mutations
	
	protected InnovationManager() {
		this.currentInnovationNumber = 0;
		this.currentInnovations = new ArrayList<Innovation>();
	}
	
	//reset the list of innovations for a new generation
	protected void newGeneration(){
		this.currentInnovations = new ArrayList<Innovation>();
	}
	
	/**
	*checks to see if the connection defined by two nodes is equivalent to an existing innovation from the current generation
	*if it is, return its innovation number
	*if not, return the next available innovation number and add it to the list of current innovations
	*
	*WARNING: be careful using this method when setting up the initial genomes. Make sure equivalent nodes are actually numbered the same or it will stuff up the whole run.
	**/
	protected int getInnovationNumber(int inNodeLabel, int outNodeLabel){
		for(int i = 0; i < this.currentInnovations.size(); i++){
			if(this.currentInnovations.get(i).isEquivalent(inNodeLabel, outNodeLabel)){
				return this.currentInnovations.get(i).getInnovationNumber();
			}
		}
		
		//if the innovation is new, record it and assign it a new innovation number
		this.currentInnovationNumber ++;
		this.currentInnovations.add(new Innovation(this.currentInnovationNumber, inNodeLabel, outNodeLabel));
		return this.currentInnovationNumber;
	}
	
}
