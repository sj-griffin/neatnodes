package com.neatnodes.genome;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.neatnodes.algorithm.InnovationManager;
import com.neatnodes.genome.Connection;
import com.neatnodes.genome.Genome;
import com.neatnodes.genome.Node;

public class GenomeTests {
	//Node tests
	@Test
	public void testNodeCreate(){
		Node n = new Node(Node.INPUT, 1);
		
		assertEquals(Node.INPUT, n.getType());
		assertEquals(1, n.getLabel());
		assertEquals(0.0, n.getValue(), 0.0001);
	}
	
	@Test
	public void testNodeFire(){
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
	public void testNodeSetValue(){
		//value should be set for input or bias nodes
		Node inNode = new Node(Node.INPUT, 1);
		inNode.setValue(0.75);
		assertEquals(0.75, inNode.getValue(), 0.0001);
		
		Node biasNode = new Node(Node.BIAS, 1);
		biasNode.setValue(0.75);
		assertEquals(0.75, biasNode.getValue(), 0.0001);
		
		//exception should be thrown when trying to set an output or hidden node
		Node outNode = new Node(Node.OUTPUT, 1);
	    Executable testBlock = () -> { outNode.setValue(0.75); };		
	    assertThrows(RuntimeException.class, testBlock);
	    
		Node hiddenNode = new Node(Node.HIDDEN, 1);
	    testBlock = () -> { hiddenNode.setValue(0.75); };		
	    assertThrows(RuntimeException.class, testBlock);
	}
	
	//Connection tests
	@Test
	public void testConnectionCreate() {
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

	//Genome tests
	
	@Test
	public void testGenomeCreate() {
		InnovationManager iManager = new InnovationManager();
		Genome g = new Genome(iManager);
		assertEquals(0, g.getNumberOfInputs());
		assertEquals(0, g.getNumberOfOutputs());
		assertEquals(false, g.isFitnessMeasured());
		
		assertEquals(1, g.getNodeGenes().size());
		assertEquals(0, g.getConnectionGenes().size());
	}
	
	@Test
	public void testGenomeAddNode(){
		InnovationManager iManager = new InnovationManager();
		Genome g = new Genome(iManager);
		g.addNode(1, Node.INPUT);
		assertEquals(2, g.getNodeGenes().size());
		assertEquals(1, g.getNumberOfInputs());
		assertEquals(0, g.getNumberOfOutputs());
		Node n = g.getNode(1);
		assertEquals(Node.INPUT, n.getType());
		assertEquals(1, n.getLabel());
		
		g.addNode(2, Node.HIDDEN);
		assertEquals(3, g.getNodeGenes().size());
		assertEquals(1, g.getNumberOfInputs());
		assertEquals(0, g.getNumberOfOutputs());
		n = g.getNode(2);
		assertEquals(Node.HIDDEN, n.getType());
		assertEquals(2, n.getLabel());
		
		g.addNode(3, Node.OUTPUT);
		assertEquals(4, g.getNodeGenes().size());
		assertEquals(1, g.getNumberOfInputs());
		assertEquals(1, g.getNumberOfOutputs());
		n = g.getNode(3);
		assertEquals(Node.OUTPUT, n.getType());
		assertEquals(3, n.getLabel());
		
		//add should fail if the node type is invalid
		Executable testBlock = () -> { g.addNode(4, 0); };		
	    assertThrows(RuntimeException.class, testBlock);
	    testBlock = () -> { g.addNode(4, 5); };
	    assertThrows(RuntimeException.class, testBlock);

		//add should fail if the label already exists
		testBlock = () -> { g.addNode(1, Node.HIDDEN); };		
	    assertThrows(RuntimeException.class, testBlock);
	    
		//add should fail if the fitness has already been measured
	    g.setFitness(1);
		testBlock = () -> { g.addNode(4, Node.HIDDEN); };		
	    assertThrows(RuntimeException.class, testBlock);
	    
	    //add should fail if the node is a second bias node
		testBlock = () -> { g.addNode(4, Node.BIAS); };		
	    assertThrows(RuntimeException.class, testBlock);
	}
	
	@Test
	public void testGenomeAddConnection() {
		InnovationManager iManager = new InnovationManager();
		Genome g = new Genome(iManager);
		g.addNode(1, Node.INPUT);
		g.addNode(2, Node.OUTPUT);
		g.addConnection(1, 2, 5, true, 3);
		
		assertEquals(1, g.getConnectionGenes().size());
		Connection c = g.getConnection(3);
		assertEquals(g.getNode(1), c.getInNode());
		assertEquals(g.getNode(2), c.getOutNode());
		assertEquals(5.0, c.getWeight());
		assertEquals(true, c.isEnabled());
		assertEquals(3, c.getInnovationNumber());
		
		//add should fail if the innovation number already exists in the genome
		Executable testBlock = () -> { g.addConnection(2, 1, 4, true, 3); };
		assertThrows(RuntimeException.class, testBlock);
		
		//add should fail if either node doesn't exist in the genome
		testBlock = () -> { g.addConnection(3, 2, 1, true, 4); };
		assertThrows(RuntimeException.class, testBlock);
		
		testBlock = () -> { g.addConnection(2, 3, 1, true, 5); };
		assertThrows(RuntimeException.class, testBlock);
		
		//add should fail if the fitness has already been measured
	    g.setFitness(1);
		testBlock = () -> { g.addConnection(1, 2, 1, true, 6); };		
	    assertThrows(RuntimeException.class, testBlock);
	}
	
	@Test
	public void testGenomeMutateWeights() {
		InnovationManager iManager = new InnovationManager();
		Genome g = new Genome(iManager);
		g.addNode(1, Node.INPUT);
		g.addNode(2, Node.OUTPUT);
		g.addConnection(1, 2, 5, true, 1);
		
		g.mutateWeights();
		double weight = g.getConnection(1).getWeight();
		assertNotSame(5.0, weight);
		assertTrue((weight >= 4.9 && weight <= 5.1) || (weight >= -2 && weight <= 2));
	}
	
	@Test
	public void testGenomeNodeMutatation() {
		InnovationManager iManager = new InnovationManager();
		Genome g = new Genome(iManager);
		g.addNode(1, Node.INPUT);
		g.addNode(2, Node.OUTPUT);
		g.addConnection(1, 2, 5, false, iManager.getInnovationNumber(1, 2));
		
		g.nodeMutation();
		
		//nothing should happen if the connection is disabled
		assertNull(g.getNode(3));
		assertNull(g.getConnection(iManager.getInnovationNumber(1, 3)));
		assertNull(g.getConnection(iManager.getInnovationNumber(3, 2)));
		
		g.getConnection(iManager.getInnovationNumber(1, 2)).setEnabled(true);
		g.nodeMutation();
		
		//if the connection is enabled, the mutation should occur
		Node newNode = g.getNode(3);
		Connection oldConnection = g.getConnection(iManager.getInnovationNumber(1, 2));
		Connection newConnection1 = g.getConnection(iManager.getInnovationNumber(1, 3));
		Connection newConnection2 = g.getConnection(iManager.getInnovationNumber(3, 2));
		
		assertEquals(Node.HIDDEN, newNode.getType());
		assertEquals(3, newNode.getLabel());
		assertEquals(false, oldConnection.isEnabled());
		assertEquals(1, newConnection1.getInNode().getLabel());
		assertEquals(3, newConnection1.getOutNode().getLabel());
		assertEquals(3, newConnection2.getInNode().getLabel());
		assertEquals(2, newConnection2.getOutNode().getLabel());
		assertEquals(3, g.getConnectionGenes().size());
	}
	
	@Test
	public void testGenomeLinkMutation() {
		InnovationManager iManager = new InnovationManager();
		Genome g = new Genome(iManager);
		g.addNode(1, Node.INPUT);
		g.addNode(2, Node.INPUT);
		g.addNode(3, Node.OUTPUT);
		g.addNode(4, Node.HIDDEN);
		g.addConnection(0, 3, 1, true, iManager.getInnovationNumber(0, 3));
		g.addConnection(1, 3, 1, true, iManager.getInnovationNumber(1, 3));
		g.addConnection(2, 3, 1, true, iManager.getInnovationNumber(2, 3));
		g.addConnection(4, 3, 1, true, iManager.getInnovationNumber(4, 3));
		g.addConnection(0, 4, 1, true, iManager.getInnovationNumber(0, 4));
		g.addConnection(1, 4, 1, true, iManager.getInnovationNumber(1, 4));
		g.addConnection(2, 4, 1, true, iManager.getInnovationNumber(2, 4));
		g.addConnection(3, 4, 1, true, iManager.getInnovationNumber(3, 4));
		g.addConnection(4, 4, 1, true, iManager.getInnovationNumber(4, 4));
		g.addConnection(3, 3, 1, true, iManager.getInnovationNumber(3, 3));
		
		//if every possible connection is already made, there should be no change
		g.linkMutation();
		assertEquals(10, g.getConnectionGenes().size());
		
		g.addNode(5, Node.INPUT);
		g.addConnection(5, 3, 1, true, iManager.getInnovationNumber(5, 3));
		
		//the mutation should create the connection 5 -> 4, as it is the only remaining connection that doesn't violate any rules
		g.linkMutation();
		assertEquals(12, g.getConnectionGenes().size());
		assertNotNull(g.getConnection(iManager.getInnovationNumber(5, 4)));
	}
	
	@Test
	public void testGenomeCloneGenome() {
		InnovationManager iManager = new InnovationManager();
		Genome g1 = new Genome(iManager);
		g1.addNode(1, Node.INPUT);
		g1.addNode(2, Node.OUTPUT);
		g1.addNode(3, Node.HIDDEN);
		g1.addConnection(0, 2, 1, true, iManager.getInnovationNumber(0, 2));
		g1.addConnection(1, 2, 1, true, iManager.getInnovationNumber(1, 2));
		g1.addConnection(3, 2, 1, true, iManager.getInnovationNumber(3, 2));
		g1.addConnection(0, 3, 1, true, iManager.getInnovationNumber(0, 3));
		
		g1.setFitness(100);
		
		Genome g2 = g1.cloneGenome();
		
		//the new genome should be a copy of the old one
		assertEquals(4, g2.getNodeGenes().size());
		assertEquals(4, g2.getConnectionGenes().size());
		assertEquals(1, g2.getNumberOfInputs());
		assertEquals(1, g2.getNumberOfOutputs());
		assertNotNull(g2.getNode(0));
		assertNotNull(g2.getNode(1));
		assertNotNull(g2.getNode(2));
		assertNotNull(g2.getNode(3));
		assertNotNull(g2.getConnection(iManager.getInnovationNumber(0, 2)));
		assertNotNull(g2.getConnection(iManager.getInnovationNumber(1, 2)));
		assertNotNull(g2.getConnection(iManager.getInnovationNumber(3, 2)));
		assertNotNull(g2.getConnection(iManager.getInnovationNumber(0, 3)));

		//the new genome should be editable
		assertFalse(g2.isFitnessMeasured());
	}
	
	@Test
	public void testGenomeWriteInputs() {
		InnovationManager iManager = new InnovationManager();
		Genome g = new Genome(iManager);
		g.addNode(1, Node.INPUT);
		g.addNode(2, Node.INPUT);
		g.addNode(3, Node.OUTPUT);
		g.addNode(4, Node.HIDDEN);
		
		HashMap<Integer, Double> inputs = new HashMap<Integer, Double>();
		inputs.put(1, 2.3);
		inputs.put(2, 4.5);
		g.writeInputs(inputs);
		
		assertEquals(2.3, g.getNode(1).getValue());
		assertEquals(4.5, g.getNode(2).getValue());
		
		inputs.remove(2);
		
		//write should fail if the number of inputs is wrong
		Executable testBlock = () -> { g.writeInputs(inputs); };		
	    assertThrows(RuntimeException.class, testBlock);
	    
	    //write should fail if the keys don't all correspond to input nodes
	    inputs.put(3, 8.7);
	    assertThrows(RuntimeException.class, testBlock);
	    
	    inputs.remove(3);
	    inputs.put(4, 4.1);
	    assertThrows(RuntimeException.class, testBlock);
	    
	    //we shouldn't be able to set the bias node
	    inputs.remove(4);
	    inputs.put(0, 6.9);
	    assertThrows(RuntimeException.class, testBlock);
	}
	
	@Test
	public void testGenomeReadOutputs() {
		InnovationManager iManager = new InnovationManager();
		Genome g = new Genome(iManager);
		g.addNode(2, Node.INPUT);
		g.addNode(3, Node.OUTPUT);
		g.addNode(4, Node.OUTPUT);
		g.addNode(5, Node.OUTPUT);
		
		HashMap<Integer, Double> outputs = g.readOutputs();
		assertEquals(3, outputs.keySet().size());
		assertEquals(0.0, outputs.get(3), 0.001);
		assertEquals(0.0, outputs.get(4), 0.001);
		assertEquals(0.0, outputs.get(5), 0.001);
	}
	
	@Test
	public void testGenomeRun() {
		InnovationManager iManager = new InnovationManager();
		Genome g = new Genome(iManager);
		g.addNode(1, Node.INPUT);
		g.addNode(2, Node.OUTPUT);
		g.addNode(3, Node.HIDDEN);
		g.addConnection(0, 2, 1, true, iManager.getInnovationNumber(0, 2));
		g.addConnection(1, 2, 2, true, iManager.getInnovationNumber(1, 2));
		g.addConnection(3, 2, 3, true, iManager.getInnovationNumber(3, 2));
		g.addConnection(0, 3, 1, true, iManager.getInnovationNumber(0, 3));
		
		HashMap<Integer, Double> inputs = new HashMap<Integer, Double>();
		inputs.put(1, -0.55);
		g.writeInputs(inputs);
		
		g.run();
		
		assertEquals(1.0, g.getNode(0).getValue(), 0.000000000001);
		assertEquals(-0.55, g.getNode(1).getValue(), 0.000000000001);
		assertEquals(0.3798935676569098, g.getNode(2).getValue(), 0.000000000001);
		assertEquals(0.9926084586557181, g.getNode(3).getValue(), 0.000000000001);

		g.run();

		assertEquals(1.0, g.getNode(0).getValue(), 0.000000000001);
		assertEquals(-0.55, g.getNode(1).getValue(), 0.000000000001);
		assertEquals(0.9999992486130665, g.getNode(2).getValue(), 0.000000000001);
		assertEquals(0.9926084586557181, g.getNode(3).getValue(), 0.000000000001);
	}
	
	@Test
	public void testGenomeReset() {
		InnovationManager iManager = new InnovationManager();
		Genome g = new Genome(iManager);
		g.addNode(1, Node.INPUT);
		g.addNode(2, Node.OUTPUT);
		g.addNode(3, Node.HIDDEN);
		
		HashMap<Integer, Double> inputs = new HashMap<Integer, Double>();
		inputs.put(1, 2.3);
		g.writeInputs(inputs);
		
		g.reset();
		
		assertEquals(1.0, g.getNode(0).getValue(), 0.000000000001);
		assertEquals(0.0, g.getNode(1).getValue(), 0.000000000001);
		assertEquals(0.0, g.getNode(2).getValue(), 0.000000000001);
		assertEquals(0.0, g.getNode(3).getValue(), 0.000000000001);
	}
	
	@Test
	public void testGenomeSetFitness() {
		InnovationManager iManager = new InnovationManager();
		Genome g = new Genome(iManager);
		g.setFitness(25);
		
		assertTrue(g.isFitnessMeasured());
		assertEquals(25, g.getFitness());
	}
	
	@Test
	public void testGenomeGetFitness() {
		InnovationManager iManager = new InnovationManager();
		Genome g = new Genome(iManager);
		
		//method should fail if the fitness has not been set
		Executable testBlock = () -> { g.getFitness(); };		
	    assertThrows(RuntimeException.class, testBlock);
	    
	    g.setFitness(80);
		assertEquals(80, g.getFitness());
	}
	
}