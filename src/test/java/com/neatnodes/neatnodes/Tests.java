package com.neatnodes.neatnodes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class Tests {
	//Node tests
	@Test
	public void testCreateNode(){
		Node n = new Node(Node.INPUT, 1);
		
		assertEquals(Node.INPUT, n.getType());
		assertEquals(1, n.getLabel());
		assertEquals(0.0, n.getValue(), 0.0001);
	}
	
	@Test
	public void testFireNode(){
		Node inNode = new Node(Node.INPUT, 1);
		inNode.addInput(0.5);
		inNode.addInput(0);
		
		inNode.fire();
		assertEquals(0.0, inNode.getValue(), 0.0001);
		
		Node biasNode = new Node(Node.BIAS, 1);
		biasNode.addInput(0.5);
		biasNode.addInput(0);
		
		biasNode.fire();
		assertEquals(0.0, biasNode.getValue(), 0.0001);
		
		Node outNode = new Node(Node.OUTPUT, 1);
		outNode.addInput(0.5);
		outNode.addInput(0);
		
		outNode.fire();
		assertEquals(0.92, outNode.getValue(), 0.001);
		
		Node hiddenNode = new Node(Node.HIDDEN, 1);
		hiddenNode.addInput(0.5);
		hiddenNode.addInput(0);
		
		hiddenNode.fire();
		assertEquals(0.92, hiddenNode.getValue(), 0.001);
	}
	
	@Test
	public void testSetNodeValue(){
		Node inNode = new Node(Node.INPUT, 1);
		inNode.setValue(0.75);
		assertEquals(0.75, inNode.getValue(), 0.0001);
		
		Node biasNode = new Node(Node.BIAS, 1);
		biasNode.setValue(0.75);
		assertEquals(0.75, biasNode.getValue(), 0.0001);
	}
	
	@Test
	public void testSetNodeValueException() {
		Node outNode = new Node(Node.OUTPUT, 1);
	    Executable codeToTest1 = () -> { outNode.setValue(0.75); };		
	    assertThrows(GenomeException.class, codeToTest1);
	    
		Node hiddenNode = new Node(Node.HIDDEN, 1);
	    Executable codeToTest2 = () -> { hiddenNode.setValue(0.75); };		
	    assertThrows(GenomeException.class, codeToTest2);
	}
	
	//Connection tests
	@Test
	public void testCreateConnection() {
		Node inNode = new Node(Node.HIDDEN, 1);
		Node outNode = new Node(Node.HIDDEN, 2);
		Connection connection = new Connection(inNode, outNode, 5, true, 1);
		assertEquals(inNode, connection.getInNode());
		assertEquals(outNode, connection.getOutNode());
		assertEquals(5.0, connection.getWeight());
		assertEquals(true, connection.isEnabled());
		assertEquals(1, connection.getInnovationNumber());
	}
	
	@Test
	public void testConnectionTransfer() {
		Node inNode = new Node(Node.INPUT, 1);
		inNode.setValue(0.75);
		Node outNode = new Node(Node.HIDDEN, 2);
		Connection connection = new Connection(inNode, outNode, 5, false, 1);
		connection.transfer();
		
		//outNode should not be changed when connection is disabled
		ArrayList<Double> expectedInputs = new ArrayList<Double>();
		ArrayList<Double> actualInputs = outNode.getInputs();
		
		assertEquals(expectedInputs.size(), actualInputs.size());
		for(int i = 0; i < expectedInputs.size(); i ++) {
			assertEquals(expectedInputs.get(i), actualInputs.get(i));
		}
		
		connection.setEnabled(true);
		connection.transfer();
		
		//outNode should be changed when connection is enabled
		expectedInputs = new ArrayList<Double>();
		expectedInputs.add(0.75 * 5);
		actualInputs = outNode.getInputs();
		
		assertEquals(expectedInputs.size(), actualInputs.size());
		for(int i = 0; i < expectedInputs.size(); i ++) {
			assertEquals(expectedInputs.get(i), actualInputs.get(i));
		}
	}

	
}
