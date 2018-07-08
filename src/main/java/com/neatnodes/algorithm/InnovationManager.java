package com.neatnodes.algorithm;

import java.util.ArrayList;

/**
 * A class that tracks the innovations that occur each generation and provides innovation numbers to use for new 
 * Connections created as part of the NEAT algorithm. The purpose of this class is to ensure that equivalent mutations 
 * in the same generation are assigned the same innovation number, as specified by the NEAT algorithm.
 * @author Sam Griffin
 */
public class InnovationManager {
	int currentInnovationNumber; //used to track gene history
	ArrayList<Innovation> currentInnovations; //stores the innovations created during the current generation so that the same innovation number can be applied to equivalent mutations
	
	/**
	 * Creates a new InnovationManager.
	 */
	public InnovationManager() {
		this.currentInnovationNumber = 0;
		this.currentInnovations = new ArrayList<Innovation>();
	}
	
	/**
	 * Resets the list of innovations for a new generation.
	 */
	public void newGeneration(){
		this.currentInnovations = new ArrayList<Innovation>();
	}
		
	/**
	 * Checks to see if a potential new Connection defined by two Nodes is equivalent to an existing innovation from 
	 * the current generation. If it is, returns its innovation number. If not, returns the next available innovation 
	 * number and adds it to the list of current innovations. This method should be used to assign innovation numbers 
	 * for all new Connections created as part of the NEAT algorithm.
	 * @param inNodeLabel
	 * 		The label of the in-node.
	 * @param outNodeLabel
	 * 		The label of the out-node.
	 * @return
	 * 		The innovation number to use for the new Connection.
	 */
	public int getInnovationNumber(int inNodeLabel, int outNodeLabel){
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
