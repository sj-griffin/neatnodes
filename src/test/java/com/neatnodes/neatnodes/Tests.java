package com.neatnodes.neatnodes;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DataFormatException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class Tests {
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
	    assertThrows(GenomeException.class, testBlock);
	    
		Node hiddenNode = new Node(Node.HIDDEN, 1);
	    testBlock = () -> { hiddenNode.setValue(0.75); };		
	    assertThrows(GenomeException.class, testBlock);
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
	    assertThrows(GenomeException.class, testBlock);
	    testBlock = () -> { g.addNode(4, 5); };
	    assertThrows(GenomeException.class, testBlock);

		//add should fail if the label already exists
		testBlock = () -> { g.addNode(1, Node.HIDDEN); };		
	    assertThrows(GenomeException.class, testBlock);
	    
		//add should fail if the fitness has already been measured
	    g.setFitness(1);
		testBlock = () -> { g.addNode(4, Node.HIDDEN); };		
	    assertThrows(GenomeException.class, testBlock);
	    
	    //add should fail if the node is a second bias node
		testBlock = () -> { g.addNode(4, Node.BIAS); };		
	    assertThrows(GenomeException.class, testBlock);
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
		assertThrows(GenomeException.class, testBlock);
		
		//add should fail if either node doesn't exist in the genome
		testBlock = () -> { g.addConnection(3, 2, 1, true, 4); };
		assertThrows(GenomeException.class, testBlock);
		
		testBlock = () -> { g.addConnection(2, 3, 1, true, 5); };
		assertThrows(GenomeException.class, testBlock);
		
		//add should fail if the fitness has already been measured
	    g.setFitness(1);
		testBlock = () -> { g.addConnection(1, 2, 1, true, 6); };		
	    assertThrows(GenomeException.class, testBlock);
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
	    assertThrows(GenomeException.class, testBlock);
	    
	    //write should fail if the keys don't all correspond to input nodes
	    inputs.put(3, 8.7);
	    assertThrows(GenomeException.class, testBlock);
	    
	    inputs.remove(3);
	    inputs.put(4, 4.1);
	    assertThrows(GenomeException.class, testBlock);
	    
	    //we shouldn't be able to set the bias node
	    inputs.remove(4);
	    inputs.put(0, 6.9);
	    assertThrows(GenomeException.class, testBlock);
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
	    assertThrows(GenomeException.class, testBlock);
	    
	    g.setFitness(80);
		assertEquals(80, g.getFitness());
	}
	
	//Species tests
	@Test
	public void testSpeciesCreate() {
		InnovationManager iManager = new InnovationManager();
		Configuration configuration = new Configuration();
		Genome g1 = new Genome(iManager);
		Species s = new Species(g1, 100, 0, configuration);
		
		assertEquals(100, s.getMaxFitness());
		assertEquals(0, s.getGenerationsWithoutImprovement());
		assertEquals(0, s.getGenomes().size());
		assertFalse(s.isFinalised());
	}
	
	@Test
	public void testSpeciesAddGenome() {
		InnovationManager iManager = new InnovationManager();
		Configuration configuration = new Configuration();
		Genome g1 = new Genome(iManager);
		g1.addNode(1, Node.INPUT);
		g1.addNode(2, Node.OUTPUT);
		g1.addNode(3, Node.HIDDEN);
		g1.addConnection(0, 2, 1, true, iManager.getInnovationNumber(0, 2));
		g1.addConnection(1, 2, 2, true, iManager.getInnovationNumber(1, 2));
		g1.addConnection(3, 2, 3, true, iManager.getInnovationNumber(3, 2));
		g1.addConnection(0, 3, 1, true, iManager.getInnovationNumber(0, 3));
		
		Species s = new Species(g1, 100, 0, configuration);
		
		//if the genomes have a compatability distance less than the compatability threshold (default 1.0), the add should succeed
		Genome g2 = g1.cloneGenome();
		g2.addNode(4, Node.HIDDEN);
		g2.addConnection(4, 3, 1, true, iManager.getInnovationNumber(4, 3));
		
		assertTrue(s.addGenome(g2));
		ArrayList<Genome> genomes = s.getGenomes();
		assertEquals(1, genomes.size());
		assertEquals(g2, genomes.get(0));

		//if the genomes have a compatability distance greater than the threshold, the add should fail
		Genome g3 = g2.cloneGenome();
		g3.addConnection(2, 3, 6, true, iManager.getInnovationNumber(2, 3));
		g3.getConnection(iManager.getInnovationNumber(1, 2)).setWeight(5);
		g3.getConnection(iManager.getInnovationNumber(0, 3)).setWeight(8);
		
		assertFalse(s.addGenome(g3));
		genomes = s.getGenomes();
		assertEquals(1, genomes.size());
		
		//if the species is already finalised, adds should fail
		for (Genome g : genomes) {
			g.setFitness(100);
		}
		s.calculateAverageFitness();
		Genome g4 = g3.cloneGenome();
		
		Executable testBlock = () -> { s.addGenome(g4); };		
	    assertThrows(GenomeException.class, testBlock);
	}
	
	@Test
	public void testSpeciesCalculateAverageFitness(){
		InnovationManager iManager = new InnovationManager();
		Configuration configuration = new Configuration();
		Genome g1 = new Genome(iManager);
		g1.addNode(1, Node.INPUT);
		g1.addNode(2, Node.OUTPUT);
		g1.addNode(3, Node.HIDDEN);
		g1.addConnection(0, 2, 1, true, iManager.getInnovationNumber(0, 2));
		g1.addConnection(1, 2, 2, true, iManager.getInnovationNumber(1, 2));
		g1.addConnection(3, 2, 3, true, iManager.getInnovationNumber(3, 2));
		g1.addConnection(0, 3, 1, true, iManager.getInnovationNumber(0, 3));
		Genome g2 = g1.cloneGenome();
		Genome g3 = g1.cloneGenome();
		Genome g4 = g1.cloneGenome();
		g1.setFitness(20);
		g2.setFitness(157);
		g3.setFitness(302.87);
		g4.setFitness(85.4222);
		Species s = new Species(g1, 302.87, 0, configuration);
		s.addGenome(g1);
		s.addGenome(g2);
		s.addGenome(g3);
		s.addGenome(g4);
		s.calculateAverageFitness();
		
		assertTrue(s.isFinalised());
		assertEquals(141.32305, s.getAverageFitness(), 0.00001);
	}
	
	@Test
	public void testSpeciesGetAverageFitness() {
		InnovationManager iManager = new InnovationManager();
		Configuration configuration = new Configuration();
		Genome g1 = new Genome(iManager);
		g1.addNode(1, Node.INPUT);
		g1.addNode(2, Node.OUTPUT);
		g1.addNode(3, Node.HIDDEN);
		g1.addConnection(0, 2, 1, true, iManager.getInnovationNumber(0, 2));
		g1.addConnection(1, 2, 2, true, iManager.getInnovationNumber(1, 2));
		g1.addConnection(3, 2, 3, true, iManager.getInnovationNumber(3, 2));
		g1.addConnection(0, 3, 1, true, iManager.getInnovationNumber(0, 3));
		g1.setFitness(30);

		Species s = new Species(g1, 30, 0, configuration);
		s.addGenome(g1);
		
		//get should fail if fitness hasn't been calculated yet
		Executable testBlock = () -> { s.getAverageFitness(); };		
	    assertThrows(GenomeException.class, testBlock);
	    
	    //get should succeed once fitness is set
		s.calculateAverageFitness();
		assertEquals(30, s.getAverageFitness());
	}
	
	@Test
	public void testSpeciesCull() {
		InnovationManager iManager = new InnovationManager();
		Configuration configuration = new Configuration();
		Genome g1 = new Genome(iManager);
		g1.addNode(1, Node.INPUT);
		g1.addNode(2, Node.OUTPUT);
		g1.addConnection(1, 2, 1, true, iManager.getInnovationNumber(1, 2));
		g1.setFitness(45);
		
		Species s = new Species(g1, 131, 5, configuration);
		s.addGenome(g1);
		
		Genome g2 = g1.cloneGenome();
		g2.setFitness(17);
		s.addGenome(g2);
		Genome g3 = g1.cloneGenome();
		g3.setFitness(97.54);
		s.addGenome(g3);
		Genome g4 = g1.cloneGenome();
		g4.setFitness(28.123);
		s.addGenome(g4);
		Genome g5 = g1.cloneGenome();
		g5.setFitness(2.9);
		s.addGenome(g5);
		Genome g6 = g1.cloneGenome();
		g6.setFitness(131);
		s.addGenome(g6);
		Genome g7 = g1.cloneGenome();
		g7.setFitness(81.3064);
		s.addGenome(g7);
		Genome g8 = g1.cloneGenome();
		g8.setFitness(32);
		s.addGenome(g8);
		Genome g9 = g1.cloneGenome();
		g9.setFitness(118.23);
		s.addGenome(g9);
		
		//expected behaviour if the existing maxFitness is not exceeded
		ArrayList<Genome> expectedGenomes = new ArrayList<Genome>();
		expectedGenomes.add(g1);
		expectedGenomes.add(g7);
		expectedGenomes.add(g3);
		expectedGenomes.add(g9);
		expectedGenomes.add(g6);
		
		Genome champion = s.cull();
		
		assertEquals(champion, g6);
		assertEquals(131, s.getMaxFitness());
		assertEquals(5, s.getGenerationsWithoutImprovement());
		ArrayList<Genome> resultGenomes = s.getGenomes();
		assertEquals(expectedGenomes.size(), resultGenomes.size());
		for(int i = 0; i < resultGenomes.size(); i ++ ) {
			assertEquals(expectedGenomes.get(i), resultGenomes.get(i));
		}
		
		//expected behaviour if the max fitness is exceeded
		Genome g10 = g1.cloneGenome();
		g10.setFitness(145.56);
		s.addGenome(g10);
		expectedGenomes.add(g10);
		expectedGenomes.remove(0);
		expectedGenomes.remove(0);
		expectedGenomes.remove(0);
		
		champion = s.cull();

		assertEquals(champion, g10);
		assertEquals(145.56, s.getMaxFitness());
		assertEquals(0, s.getGenerationsWithoutImprovement());
		resultGenomes = s.getGenomes();
		assertEquals(expectedGenomes.size(), resultGenomes.size());
		for(int i = 0; i < resultGenomes.size(); i ++ ) {
			assertEquals(expectedGenomes.get(i), resultGenomes.get(i));
		}
	}
	
	//StaticFunctions tests
	@Test
	public void testStaticFunctionsCalculateCompatabilityDistance() {
		InnovationManager iManager = new InnovationManager();
		Configuration configuration = new Configuration();
		Genome g1 = new Genome(iManager);
		g1.addNode(1, Node.INPUT);
		g1.addNode(2, Node.OUTPUT);
		g1.addConnection(0, 2, 1, true, iManager.getInnovationNumber(0, 2));
		g1.addConnection(1, 2, 2, true, iManager.getInnovationNumber(1, 2));
		
		//g2 has an excess gene, and a gene with a weight difference of 3.5. g1 has a disjoint gene.
		Genome g2 = new Genome(iManager);
		g2.addNode(1, Node.INPUT);
		g2.addNode(2, Node.OUTPUT);
		g2.addNode(3, Node.HIDDEN);
		g2.addConnection(0, 2, 4.5, true, iManager.getInnovationNumber(0, 2));
		g2.addConnection(1, 3, 3, true, iManager.getInnovationNumber(1, 3));
		
		double result = StaticFunctions.calculateCompatabilityDistance(g1, g2, configuration);
		
		assertEquals(2.4, result, 0.000000001);
		
		Genome g3 = new Genome(iManager);
		Genome g4 = new Genome(iManager);
		
		//the calculation should throw an exception if either genome is empty
		Executable testBlock = () -> { StaticFunctions.calculateCompatabilityDistance(g3, g4, configuration); };		
	    assertThrows(GenomeException.class, testBlock);
	}
	
	@Test
	public void testStaticFunctionsBreed() {
		InnovationManager iManager = new InnovationManager();
		Configuration configuration = new Configuration();
		Genome g1 = new Genome(iManager);
		g1.addNode(1, Node.INPUT);
		g1.addNode(2, Node.OUTPUT);
		g1.addConnection(0, 2, 1, true, iManager.getInnovationNumber(0, 2));
		g1.addConnection(1, 2, 2, true, iManager.getInnovationNumber(1, 2));
		g1.setFitness(10);
		
		//g2 has an excess gene, a disjoint gene, a matching gene with a weight difference, and a greater fitness
		Genome g2 = new Genome(iManager);
		g2.addNode(1, Node.INPUT);
		g2.addNode(2, Node.OUTPUT);
		g2.addNode(3, Node.HIDDEN);
		g2.addConnection(0, 2, 4.5, true, iManager.getInnovationNumber(0, 2));
		g2.addConnection(1, 3, 3, true, iManager.getInnovationNumber(1, 3));
		g2.setFitness(20);
		
		Genome offspring = StaticFunctions.breed(g1, g2, iManager, configuration);
		
		assertEquals(2, offspring.getConnectionGenes().size());
		assertEquals(4, offspring.getNodeGenes().size());
		
		//connection 0->2 should have been inherited from either g1 or g2
		Connection c  = offspring.getConnection(iManager.getInnovationNumber(0, 2));
		assertNotNull(c);
		
		boolean connectionIsCorrect = false;
		if(c.getWeight() == 1 || c.getWeight() == 4.5){
			connectionIsCorrect = true;
		}
		assertTrue(connectionIsCorrect);
		
		//connection 1->3 should have been inherited from g2, as it is fitter
		c = offspring.getConnection(iManager.getInnovationNumber(1, 3));
		assertNotNull(c);
		assertEquals(3, c.getWeight());
		
		//connection 1->2 should not have been inherited from g1, as it is not fitter
		c = offspring.getConnection(iManager.getInnovationNumber(1, 2));
		assertNull(c);
		
		//verify that we get the same results when g2 is the father instead of g1
		offspring = StaticFunctions.breed(g2, g1, iManager, configuration);
		assertEquals(2, offspring.getConnectionGenes().size());
		assertEquals(4, offspring.getNodeGenes().size());
		
		c  = offspring.getConnection(iManager.getInnovationNumber(0, 2));
		assertNotNull(c);
		
		connectionIsCorrect = false;
		if(c.getWeight() == 1 || c.getWeight() == 4.5){
			connectionIsCorrect = true;
		}
		assertTrue(connectionIsCorrect);
		
		c = offspring.getConnection(iManager.getInnovationNumber(1, 3));
		assertNotNull(c);
		assertEquals(3, c.getWeight());
		
		c = offspring.getConnection(iManager.getInnovationNumber(1, 2));
		assertNull(c);
	}
	
	@Test
	public void testStaticFunctionsSetupInitialSpecies() {
		InnovationManager iManager = new InnovationManager();
		Configuration configuration = new Configuration();
		Species s = StaticFunctions.setupInitialSpecies(3, 2, 5, iManager, configuration);
		assertEquals(5, s.getGenomes().size());
		assertFalse(s.isFinalised());
		assertEquals(0, s.getGenerationsWithoutImprovement());
		assertEquals(0.0, s.getMaxFitness());
		
		Genome g = s.getGenomes().get(4);
		assertEquals(3, g.getNumberOfInputs());
		assertEquals(2, g.getNumberOfOutputs());
		assertEquals(8, g.getConnectionGenes().size());
		
		assertEquals(0, g.getConnection(iManager.getInnovationNumber(0, 4)).getInNode().getLabel());
		assertEquals(4, g.getConnection(iManager.getInnovationNumber(0, 4)).getOutNode().getLabel());

		assertEquals(1, g.getConnection(iManager.getInnovationNumber(1, 4)).getInNode().getLabel());
		assertEquals(4, g.getConnection(iManager.getInnovationNumber(1, 4)).getOutNode().getLabel());
		
		assertEquals(2, g.getConnection(iManager.getInnovationNumber(2, 4)).getInNode().getLabel());
		assertEquals(4, g.getConnection(iManager.getInnovationNumber(2, 4)).getOutNode().getLabel());
		
		assertEquals(3, g.getConnection(iManager.getInnovationNumber(3, 4)).getInNode().getLabel());
		assertEquals(4, g.getConnection(iManager.getInnovationNumber(3, 4)).getOutNode().getLabel());
		
		assertEquals(0, g.getConnection(iManager.getInnovationNumber(0, 5)).getInNode().getLabel());
		assertEquals(5, g.getConnection(iManager.getInnovationNumber(0, 5)).getOutNode().getLabel());
		
		assertEquals(1, g.getConnection(iManager.getInnovationNumber(1, 5)).getInNode().getLabel());
		assertEquals(5, g.getConnection(iManager.getInnovationNumber(1, 5)).getOutNode().getLabel());
		
		assertEquals(2, g.getConnection(iManager.getInnovationNumber(2, 5)).getInNode().getLabel());
		assertEquals(5, g.getConnection(iManager.getInnovationNumber(2, 5)).getOutNode().getLabel());
		
		assertEquals(3, g.getConnection(iManager.getInnovationNumber(3, 5)).getInNode().getLabel());
		assertEquals(5, g.getConnection(iManager.getInnovationNumber(3, 5)).getOutNode().getLabel());
	}
	
	//API tests
	@Test
	public void testAPITestFitness() {
		DataSet d = null;
		try {
			d = new DataSet("./datasets/XOR.csv");
		} catch (DataFormatException e) {
			e.printStackTrace();
			fail();
		}
		InnovationManager iManager = new InnovationManager();
		Genome g = new Genome(iManager);
		g.addNode(1, Node.INPUT);
		g.addNode(2, Node.INPUT);
		g.addNode(3, Node.OUTPUT);
		g.addNode(4, Node.HIDDEN);
		g.addConnection(3, 4, 1.37168818659685, true, iManager.getInnovationNumber(3, 4));
		g.addConnection(4, 4, -1.9866023632803813, true, iManager.getInnovationNumber(4, 4));
		g.addConnection(0, 3, 0.5173581564297121, true, iManager.getInnovationNumber(0, 3));
		g.addConnection(3, 3, -1.6909665002259813, true, iManager.getInnovationNumber(3, 3));
		g.addConnection(1, 3, 0.6210336565818149, true, iManager.getInnovationNumber(1, 3));
		g.addConnection(2, 3, 0.973834515119807, true, iManager.getInnovationNumber(2, 3));
		g.addConnection(0, 4, -0.6742458822719644, true, iManager.getInnovationNumber(0, 4));
		g.addConnection(2, 4, 1.0724675677107962, true, iManager.getInnovationNumber(2, 4));
		g.addConnection(4, 3, -1.1832390685857468, true, iManager.getInnovationNumber(4, 3));
		g.addConnection(1, 4, -1.0264579235753712, true, iManager.getInnovationNumber(1, 4));

		assertEquals(92.82474446330792, API.testFitness(g, d, 3), 0.00000000000001);
	}
	
	@Test
	public void testAPIRunFunction() {
		//test with a genome that has been configured to calculate the XOR function
		InnovationManager iManager = new InnovationManager();
		Configuration configuration = new Configuration();
		Genome g = new Genome(iManager);
		g.addNode(1, Node.INPUT);
		g.addNode(2, Node.INPUT);
		g.addNode(3, Node.OUTPUT);
		g.addNode(4, Node.HIDDEN);
		g.addConnection(3, 4, 1.37168818659685, true, iManager.getInnovationNumber(3, 4));
		g.addConnection(4, 4, -1.9866023632803813, true, iManager.getInnovationNumber(4, 4));
		g.addConnection(0, 3, 0.5173581564297121, true, iManager.getInnovationNumber(0, 3));
		g.addConnection(3, 3, -1.6909665002259813, true, iManager.getInnovationNumber(3, 3));
		g.addConnection(1, 3, 0.6210336565818149, true, iManager.getInnovationNumber(1, 3));
		g.addConnection(2, 3, 0.973834515119807, true, iManager.getInnovationNumber(2, 3));
		g.addConnection(0, 4, -0.6742458822719644, true, iManager.getInnovationNumber(0, 4));
		g.addConnection(2, 4, 1.0724675677107962, true, iManager.getInnovationNumber(2, 4));
		g.addConnection(4, 3, -1.1832390685857468, true, iManager.getInnovationNumber(4, 3));
		g.addConnection(1, 4, -1.0264579235753712, true, iManager.getInnovationNumber(1, 4));
		
		Double[] inputs1 = {0.0, 0.0};
		Double[] expectedOutputs1 = {0.05248796662764476};
		assertArrayEquals(expectedOutputs1, API.runFunction(g, inputs1, configuration.depth));
		
		Double[] inputs2 = {1.0, 1.0};
		Double[] expectedOutputs2 = {0.08756708774628402};
		assertArrayEquals(expectedOutputs2, API.runFunction(g, inputs2, configuration.depth));
		
		Double[] inputs3 = {0.0, 1.0};
		Double[] expectedOutputs3 = {0.9849160396696722};
		assertArrayEquals(expectedOutputs3, API.runFunction(g, inputs3, configuration.depth));
		
		Double[] inputs4 = {1.0, 0.0};
		Double[] expectedOutputs4 = {0.9837502384439617};
		assertArrayEquals(expectedOutputs4, API.runFunction(g, inputs4, configuration.depth));
		
		//should fail if the inputs do not match what is defined in the genome
		Double[] inputs5 = {1.0, 0.0, 1.0};
		Executable testBlock = () -> { API.runFunction(g, inputs5, configuration.depth); };		
	    assertThrows(GenomeException.class, testBlock);
	    
		Double[] inputs6 = {1.0};
		testBlock = () -> { API.runFunction(g, inputs6, configuration.depth); };		
	    assertThrows(GenomeException.class, testBlock);
	}
	
	//Innovation tests
	@Test
	public void testInnovationCreate() {
		Innovation i = new Innovation(15, 1, 2);
		assertEquals(15, i.getInnovationNumber());
	}
	
	@Test
	public void testInnovationIsEquivalent() {
		Innovation i = new Innovation(15, 1, 2);		
		assertFalse(i.isEquivalent(1, 3));
		assertFalse(i.isEquivalent(4, 2));
		assertFalse(i.isEquivalent(2, 1));
		assertTrue(i.isEquivalent(1, 2));
		
	}
	
	//InnovationManager tests
	@Test
	public void testInnovationManagerGetInnovationNumber() {
		InnovationManager iManager = new InnovationManager();
		//the same combination of inputs should always produce the same innovation number as the first time they were entered as long as a new generation hasn't been started
		assertEquals(1, iManager.getInnovationNumber(3, 5));
		assertEquals(1, iManager.getInnovationNumber(3, 5));
		
		assertEquals(2, iManager.getInnovationNumber(8, 2));
		assertEquals(1, iManager.getInnovationNumber(3, 5));
		assertEquals(2, iManager.getInnovationNumber(8, 2));
	}
	
	@Test
	public void testInnovationManagerNewGeneration() {
		InnovationManager iManager = new InnovationManager();
		iManager.getInnovationNumber(3, 5);
		iManager.getInnovationNumber(1, 2);
		iManager.getInnovationNumber(2, 8);
		iManager.getInnovationNumber(6, 6);
		iManager.newGeneration();
		
		//after a new generation has been started, entering inputs that have already been assigned an innovation number should produce a new innovation number
		assertEquals(5, iManager.getInnovationNumber(2, 8));
		
		//the same inputs should then produce the same innovation number for the duration of this generation
		assertEquals(5, iManager.getInnovationNumber(2, 8));
	}
	
	//JSONTools tests
	@Test
	public void testJSONToolsWriteGenomeToFile() {
		InnovationManager iManager = new InnovationManager();
		Genome g = new Genome(iManager);
		g.addNode(1, Node.INPUT);
		g.addNode(2, Node.INPUT);
		g.addNode(3, Node.OUTPUT);
		g.addNode(4, Node.HIDDEN);
		g.addConnection(3, 4, 1.37168818659685, true, iManager.getInnovationNumber(3, 4));
		g.addConnection(4, 4, -1.9866023632803813, true, iManager.getInnovationNumber(4, 4));
		g.addConnection(0, 3, 0.5173581564297121, true, iManager.getInnovationNumber(0, 3));
		g.addConnection(3, 3, -1.6909665002259813, true, iManager.getInnovationNumber(3, 3));
		g.addConnection(1, 3, 0.6210336565818149, true, iManager.getInnovationNumber(1, 3));
		g.addConnection(2, 3, 0.973834515119807, true, iManager.getInnovationNumber(2, 3));
		g.addConnection(0, 4, -0.6742458822719644, true, iManager.getInnovationNumber(0, 4));
		g.addConnection(2, 4, 1.0724675677107962, true, iManager.getInnovationNumber(2, 4));
		g.addConnection(4, 3, -1.1832390685857468, true, iManager.getInnovationNumber(4, 3));
		
		//connections that are disabled should be ignored and not appear in the file
		g.addConnection(1, 4, -1.0264579235753712, false, iManager.getInnovationNumber(1, 4));

		
		JSONTools.writeGenomeToFile(g, "./testGenomeWrite.json", "This is a comment");
		
		String asString = null;
		File f = new File("./testGenomeWrite.json");
		try{
			BufferedReader br = new BufferedReader(new FileReader(f));
			String line;
			StringBuilder sb = new StringBuilder();
			String ls = System.getProperty("line.separator");
			while((line = br.readLine()) != null){
				sb.append(line);
				sb.append(ls);
			}
			
			asString = sb.toString();
			br.close();
		}
		catch(IOException e){
			e.printStackTrace();
			fail();
		}
		
		assertEquals("{\r\n" + 
				"	\"genome\": {\r\n" + 
				"		\"comment\": \"This is a comment\",\r\n" + 
				"		\"nodes\": [\r\n" + 
				"			{\r\n" + 
				"				\"type\": 4,\r\n" + 
				"				\"label\": 0\r\n" + 
				"			},\r\n" + 
				"			{\r\n" + 
				"				\"type\": 1,\r\n" + 
				"				\"label\": 1\r\n" + 
				"			},\r\n" + 
				"			{\r\n" + 
				"				\"type\": 1,\r\n" + 
				"				\"label\": 2\r\n" + 
				"			},\r\n" + 
				"			{\r\n" + 
				"				\"type\": 2,\r\n" + 
				"				\"label\": 3\r\n" + 
				"			},\r\n" + 
				"			{\r\n" + 
				"				\"type\": 3,\r\n" + 
				"				\"label\": 4\r\n" + 
				"			}\r\n" + 
				"		],\r\n" + 
				"		\"connections\": [\r\n" + 
				"			{\r\n" + 
				"				\"weight\": 1.37168818659685,\r\n" + 
				"				\"inNode\": 3,\r\n" + 
				"				\"outNode\": 4\r\n" + 
				"			},\r\n" + 
				"			{\r\n" + 
				"				\"weight\": -1.9866023632803813,\r\n" + 
				"				\"inNode\": 4,\r\n" + 
				"				\"outNode\": 4\r\n" + 
				"			},\r\n" + 
				"			{\r\n" + 
				"				\"weight\": 0.5173581564297121,\r\n" + 
				"				\"inNode\": 0,\r\n" + 
				"				\"outNode\": 3\r\n" + 
				"			},\r\n" + 
				"			{\r\n" + 
				"				\"weight\": -1.6909665002259813,\r\n" + 
				"				\"inNode\": 3,\r\n" + 
				"				\"outNode\": 3\r\n" + 
				"			},\r\n" + 
				"			{\r\n" + 
				"				\"weight\": 0.6210336565818149,\r\n" + 
				"				\"inNode\": 1,\r\n" + 
				"				\"outNode\": 3\r\n" + 
				"			},\r\n" + 
				"			{\r\n" + 
				"				\"weight\": 0.973834515119807,\r\n" + 
				"				\"inNode\": 2,\r\n" + 
				"				\"outNode\": 3\r\n" + 
				"			},\r\n" + 
				"			{\r\n" + 
				"				\"weight\": -0.6742458822719644,\r\n" + 
				"				\"inNode\": 0,\r\n" + 
				"				\"outNode\": 4\r\n" + 
				"			},\r\n" + 
				"			{\r\n" + 
				"				\"weight\": 1.0724675677107962,\r\n" + 
				"				\"inNode\": 2,\r\n" + 
				"				\"outNode\": 4\r\n" + 
				"			},\r\n" + 
				"			{\r\n" + 
				"				\"weight\": -1.1832390685857468,\r\n" + 
				"				\"inNode\": 4,\r\n" + 
				"				\"outNode\": 3\r\n" + 
				"			}\r\n" + 
				"		]\r\n" + 
				"	}\r\n" + 
				"}\r\n" + 
				"", asString);
		
	    f.delete();
	}
	
	@Test
	public void testJSONToolsReadGenomeFromFile() {
		File f = new File("./testGenomeRead.json");
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			bw.write("{\r\n" + 
					"	\"genome\": {\r\n" + 
					"		\"comment\": \"This is a comment\",\r\n" + 
					"		\"nodes\": [\r\n" + 
					"			{\r\n" + 
					"				\"type\": 4,\r\n" + 
					"				\"label\": 0\r\n" + 
					"			},\r\n" + 
					"			{\r\n" + 
					"				\"type\": 1,\r\n" + 
					"				\"label\": 1\r\n" + 
					"			},\r\n" + 
					"			{\r\n" + 
					"				\"type\": 1,\r\n" + 
					"				\"label\": 2\r\n" + 
					"			},\r\n" + 
					"			{\r\n" + 
					"				\"type\": 2,\r\n" + 
					"				\"label\": 3\r\n" + 
					"			},\r\n" + 
					"			{\r\n" + 
					"				\"type\": 3,\r\n" + 
					"				\"label\": 4\r\n" + 
					"			}\r\n" + 
					"		],\r\n" + 
					"		\"connections\": [\r\n" + 
					"			{\r\n" + 
					"				\"weight\": 1.37168818659685,\r\n" + 
					"				\"inNode\": 3,\r\n" + 
					"				\"outNode\": 4\r\n" + 
					"			},\r\n" + 
					"			{\r\n" + 
					"				\"weight\": -1.9866023632803813,\r\n" + 
					"				\"inNode\": 4,\r\n" + 
					"				\"outNode\": 4\r\n" + 
					"			},\r\n" + 
					"			{\r\n" + 
					"				\"weight\": 0.5173581564297121,\r\n" + 
					"				\"inNode\": 0,\r\n" + 
					"				\"outNode\": 3\r\n" + 
					"			},\r\n" + 
					"			{\r\n" + 
					"				\"weight\": -1.6909665002259813,\r\n" + 
					"				\"inNode\": 3,\r\n" + 
					"				\"outNode\": 3\r\n" + 
					"			},\r\n" + 
					"			{\r\n" + 
					"				\"weight\": 0.6210336565818149,\r\n" + 
					"				\"inNode\": 1,\r\n" + 
					"				\"outNode\": 3\r\n" + 
					"			},\r\n" + 
					"			{\r\n" + 
					"				\"weight\": 0.973834515119807,\r\n" + 
					"				\"inNode\": 2,\r\n" + 
					"				\"outNode\": 3\r\n" + 
					"			},\r\n" + 
					"			{\r\n" + 
					"				\"weight\": -0.6742458822719644,\r\n" + 
					"				\"inNode\": 0,\r\n" + 
					"				\"outNode\": 4\r\n" + 
					"			},\r\n" + 
					"			{\r\n" + 
					"				\"weight\": 1.0724675677107962,\r\n" + 
					"				\"inNode\": 2,\r\n" + 
					"				\"outNode\": 4\r\n" + 
					"			},\r\n" + 
					"			{\r\n" + 
					"				\"weight\": -1.1832390685857468,\r\n" + 
					"				\"inNode\": 4,\r\n" + 
					"				\"outNode\": 3\r\n" + 
					"			},\r\n" + 
					"			{\r\n" + 
					"				\"weight\": -1.0264579235753712,\r\n" + 
					"				\"inNode\": 1,\r\n" + 
					"				\"outNode\": 4\r\n" + 
					"			}\r\n" + 
					"		]\r\n" + 
					"	}\r\n" + 
					"}\r\n" + 
					"");
			
			bw.flush();
			bw.close();
		}
		catch(IOException e){
			e.printStackTrace();
			fail();
		}
		
		Genome g = JSONTools.readGenomeFromFile("./testGenomeRead.json");
		assertEquals(5, g.getNodeGenes().size());
		assertEquals(10, g.getConnectionGenes().size());
		assertFalse(g.isFitnessMeasured());
		assertEquals(2, g.getNumberOfInputs());
		assertEquals(1, g.getNumberOfOutputs());
		assertEquals(Node.BIAS, g.getNode(0).getType());
		assertEquals(Node.INPUT, g.getNode(1).getType());
		assertEquals(Node.INPUT, g.getNode(2).getType());
		assertEquals(Node.OUTPUT, g.getNode(3).getType());
		assertEquals(Node.HIDDEN, g.getNode(4).getType());
		
		assertEquals(3, g.getConnection(1).getInNode().getLabel());
		assertEquals(4, g.getConnection(1).getOutNode().getLabel());
		assertEquals(1.37168818659685, g.getConnection(1).getWeight());
		
		assertEquals(4, g.getConnection(2).getInNode().getLabel());
		assertEquals(4, g.getConnection(2).getOutNode().getLabel());
		assertEquals(-1.9866023632803813, g.getConnection(2).getWeight());
		
		assertEquals(0, g.getConnection(3).getInNode().getLabel());
		assertEquals(3, g.getConnection(3).getOutNode().getLabel());
		assertEquals(0.5173581564297121, g.getConnection(3).getWeight());
		
		assertEquals(3, g.getConnection(4).getInNode().getLabel());
		assertEquals(3, g.getConnection(4).getOutNode().getLabel());
		assertEquals(-1.6909665002259813, g.getConnection(4).getWeight());
		
		assertEquals(1, g.getConnection(5).getInNode().getLabel());
		assertEquals(3, g.getConnection(5).getOutNode().getLabel());
		assertEquals(0.6210336565818149, g.getConnection(5).getWeight());
		
		assertEquals(2, g.getConnection(6).getInNode().getLabel());
		assertEquals(3, g.getConnection(6).getOutNode().getLabel());
		assertEquals(0.973834515119807, g.getConnection(6).getWeight());
		
		assertEquals(0, g.getConnection(7).getInNode().getLabel());
		assertEquals(4, g.getConnection(7).getOutNode().getLabel());
		assertEquals(-0.6742458822719644, g.getConnection(7).getWeight());
		
		assertEquals(2, g.getConnection(8).getInNode().getLabel());
		assertEquals(4, g.getConnection(8).getOutNode().getLabel());
		assertEquals(1.0724675677107962, g.getConnection(8).getWeight());
		
		assertEquals(4, g.getConnection(9).getInNode().getLabel());
		assertEquals(3, g.getConnection(9).getOutNode().getLabel());
		assertEquals(-1.1832390685857468, g.getConnection(9).getWeight());
		
		assertEquals(1, g.getConnection(10).getInNode().getLabel());
		assertEquals(4, g.getConnection(10).getOutNode().getLabel());
		assertEquals(-1.0264579235753712, g.getConnection(10).getWeight());
		
	    f.delete();
	}
	
	//DataSet tests
	@Test
	public void testDataSetCreate() {
		//test creating a standard unweighted DataSet
		File f = new File("./testDataset.csv");
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			bw.write("input,input,output\r\n" + 
					"0,0,0\r\n" + 
					"1,1,0\r\n" + 
					"0,1,1\r\n" + 
					"1,0,1\r\n" + 
					"2,3,5\r\n" + 
					"4,4,4\r\n" + 
					"");
			bw.flush();
			bw.close();
		}
		catch(IOException e){
			e.printStackTrace();
			fail();
		}
		
		DataSet d = null;
		try {
			d = new DataSet("./testDataset.csv");
		}
		catch(DataFormatException e) {
			e.printStackTrace();
		}
		
		assertEquals(6, d.getNumberOfEntries());
		assertEquals(2, d.getInputNumber());
		assertEquals(1, d.getOutputNumber());
		assertFalse(d.isWeighted());
		
		//test creating a weighted DataSet
		f = new File("./testDataset.csv");
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			bw.write("input,input,output,weight\r\n" + 
					"0,0,0,2\r\n" + 
					"1,1,0,2\r\n" + 
					"0,1,1,2\r\n" + 
					"1,0,1,0.5\r\n" + 
					"2,3,5,1\r\n" + 
					"4,4,4,3\r\n" + 
					"");
			bw.flush();
			bw.close();
		}
		catch(IOException e){
			e.printStackTrace();
			fail();
		}
		
		d = null;
		try {
			d = new DataSet("./testDataset.csv");
		}
		catch(DataFormatException e) {
			e.printStackTrace();
		}
		
		assertEquals(6, d.getNumberOfEntries());
		assertEquals(2, d.getInputNumber());
		assertEquals(1, d.getOutputNumber());
		assertTrue(d.isWeighted());
		
		//create should fail if the csv file is empty
		f = new File("./testDataset.csv");
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			bw.write("");
			bw.flush();
			bw.close();
		}
		catch(IOException e){
			e.printStackTrace();
			fail();
		}
		
		Executable testBlock = () -> { new DataSet("./testDataset.csv"); };		
	    assertThrows(DataFormatException.class, testBlock);
	    
		//create should fail if there are no inputs
		f = new File("./testDataset.csv");
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			bw.write("output\r\n" + 
					"0\r\n" +  
					"");
			bw.flush();
			bw.close();
		}
		catch(IOException e){
			e.printStackTrace();
			fail();
		}
		
		testBlock = () -> { new DataSet("./testDataset.csv"); };		
	    assertThrows(DataFormatException.class, testBlock);
	    
		//create should fail if there are no outputs
		f = new File("./testDataset.csv");
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			bw.write("input\r\n" + 
					"0\r\n" +  
					"");
			bw.flush();
			bw.close();
		}
		catch(IOException e){
			e.printStackTrace();
			fail();
		}
		
		testBlock = () -> { new DataSet("./testDataset.csv"); };		
	    assertThrows(DataFormatException.class, testBlock);
	    
		//create should fail if all inputs do not occur before all outputs
		f = new File("./testDataset.csv");
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			bw.write("input,output,input\r\n" + 
					"0,0,0\r\n" + 
					"");
			bw.flush();
			bw.close();
		}
		catch(IOException e){
			e.printStackTrace();
			fail();
		}
		
		testBlock = () -> { new DataSet("./testDataset.csv"); };		
	    assertThrows(DataFormatException.class, testBlock);
	    
		f = new File("./testDataset.csv");
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			bw.write("output,input\r\n" + 
					"0,0\r\n" + 
					"");
			bw.flush();
			bw.close();
		}
		catch(IOException e){
			e.printStackTrace();
			fail();
		}
		
		testBlock = () -> { new DataSet("./testDataset.csv"); };		
	    assertThrows(DataFormatException.class, testBlock);
	    
	    //create should fail if a weight column occurs before the last column
		f = new File("./testDataset.csv");
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			bw.write("input,output,weight,output\r\n" + 
					"0,0,1,1\r\n" + 
					"");
			bw.flush();
			bw.close();
		}
		catch(IOException e){
			e.printStackTrace();
			fail();
		}
		
		testBlock = () -> { new DataSet("./testDataset.csv"); };		
	    assertThrows(DataFormatException.class, testBlock);
	    
	    //create should fail if entries do not align with column headers
		f = new File("./testDataset.csv");
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			bw.write("input,input,output\r\n" + 
					"0,0,1,1\r\n" + 
					"");
			bw.flush();
			bw.close();
		}
		catch(IOException e){
			e.printStackTrace();
			fail();
		}
		
		testBlock = () -> { new DataSet("./testDataset.csv"); };		
	    assertThrows(DataFormatException.class, testBlock);
	    
		f = new File("./testDataset.csv");
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			bw.write("input,input,output\r\n" + 
					"0,0\r\n" + 
					"");
			bw.flush();
			bw.close();
		}
		catch(IOException e){
			e.printStackTrace();
			fail();
		}
		
		testBlock = () -> { new DataSet("./testDataset.csv"); };		
	    assertThrows(DataFormatException.class, testBlock);
	    
	    //create should fail if the file contains non-numeric data
		f = new File("./testDataset.csv");
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			bw.write("input,input,output\r\n" + 
					"0,a string,1\r\n" + 
					"");
			bw.flush();
			bw.close();
		}
		catch(IOException e){
			e.printStackTrace();
			fail();
		}
		
		testBlock = () -> { new DataSet("./testDataset.csv"); };		
	    assertThrows(DataFormatException.class, testBlock);
	    
	    f.delete();
	}
	
	@Test
	public void testDataSetGetInputsForRow() {
		//test with an unweighted DataSet
		File f = new File("./testDataset.csv");
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			bw.write("input,input,output\r\n" + 
					"1,1,0\r\n" + 
					"2,3,5\r\n" + 
					"4,4,4\r\n" + 
					"");
			bw.flush();
			bw.close();
		}
		catch(IOException e){
			e.printStackTrace();
			fail();
		}
		
		DataSet d = null;
		try {
			d = new DataSet("./testDataset.csv");
		}
		catch(DataFormatException e) {
			e.printStackTrace();
		}
		
		assertEquals(2, d.getInputsForRow(0).length);
		assertEquals(1, d.getInputsForRow(0)[0], 0.00000000001);
		assertEquals(1, d.getInputsForRow(0)[1], 0.00000000001);
		
		assertEquals(2, d.getInputsForRow(1).length);
		assertEquals(2, d.getInputsForRow(1)[0], 0.00000000001);
		assertEquals(3, d.getInputsForRow(1)[1], 0.00000000001);
		
		assertEquals(2, d.getInputsForRow(2).length);
		assertEquals(4, d.getInputsForRow(2)[0], 0.00000000001);
		assertEquals(4, d.getInputsForRow(2)[1], 0.00000000001);
		
		//function should return null if the requested row doesn't exist
		assertNull(d.getInputsForRow(3));
		
		//test with a weighted DataSet
		f = new File("./testDataset.csv");
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			bw.write("input,input,output,weight\r\n" + 
					"1,1,0,2\r\n" + 
					"2,3,5,2\r\n" + 
					"4,4,4,1\r\n" + 
					"");
			bw.flush();
			bw.close();
		}
		catch(IOException e){
			e.printStackTrace();
			fail();
		}
		
		d = null;
		try {
			d = new DataSet("./testDataset.csv");
		}
		catch(DataFormatException e) {
			e.printStackTrace();
		}
		
		assertEquals(2, d.getInputsForRow(0).length);
		assertEquals(1, d.getInputsForRow(0)[0], 0.00000000001);
		assertEquals(1, d.getInputsForRow(0)[1], 0.00000000001);
		
		assertEquals(2, d.getInputsForRow(1).length);
		assertEquals(2, d.getInputsForRow(1)[0], 0.00000000001);
		assertEquals(3, d.getInputsForRow(1)[1], 0.00000000001);
		
		assertEquals(2, d.getInputsForRow(2).length);
		assertEquals(4, d.getInputsForRow(2)[0], 0.00000000001);
		assertEquals(4, d.getInputsForRow(2)[1], 0.00000000001);
		
		//function should return null if the requested row doesn't exist
		assertNull(d.getInputsForRow(3));
		
		f.delete();
	}
	
	@Test
	public void testDataSetGetOutputsForRow() {
		//test with an unweighted DataSet
		File f = new File("./testDataset.csv");
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			bw.write("input,input,output\r\n" + 
					"1,1,0\r\n" + 
					"2,3,5\r\n" + 
					"4,4,4\r\n" + 
					"");
			bw.flush();
			bw.close();
		}
		catch(IOException e){
			e.printStackTrace();
			fail();
		}
		
		DataSet d = null;
		try {
			d = new DataSet("./testDataset.csv");
		}
		catch(DataFormatException e) {
			e.printStackTrace();
		}
		
		assertEquals(1, d.getOutputsForRow(0).length);
		assertEquals(0, d.getOutputsForRow(0)[0], 0.00000000001);
		
		assertEquals(1, d.getOutputsForRow(1).length);
		assertEquals(5, d.getOutputsForRow(1)[0], 0.00000000001);
		
		assertEquals(1, d.getOutputsForRow(2).length);
		assertEquals(4, d.getOutputsForRow(2)[0], 0.00000000001);
		
		//function should return null if the requested row doesn't exist
		assertNull(d.getInputsForRow(3));
		
		//test with a weighted DataSet
		f = new File("./testDataset.csv");
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			bw.write("input,input,output,weight\r\n" + 
					"1,1,0,2\r\n" + 
					"2,3,5,2\r\n" + 
					"4,4,4,1\r\n" + 
					"");
			bw.flush();
			bw.close();
		}
		catch(IOException e){
			e.printStackTrace();
			fail();
		}
		
		d = null;
		try {
			d = new DataSet("./testDataset.csv");
		}
		catch(DataFormatException e) {
			e.printStackTrace();
		}
		
		assertEquals(1, d.getOutputsForRow(0).length);
		assertEquals(0, d.getOutputsForRow(0)[0], 0.00000000001);
		
		assertEquals(1, d.getOutputsForRow(1).length);
		assertEquals(5, d.getOutputsForRow(1)[0], 0.00000000001);
		
		assertEquals(1, d.getOutputsForRow(2).length);
		assertEquals(4, d.getOutputsForRow(2)[0], 0.00000000001);
		
		//function should return null if the requested row doesn't exist
		assertNull(d.getInputsForRow(3));
		
		f.delete();
	}
	
	@Test
	public void testDataSetGetWeightForRow() {
		File f = new File("./testDataset.csv");
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			bw.write("input,input,output,weight\r\n" + 
					"1,1,0,2\r\n" + 
					"2,3,5,2\r\n" + 
					"4,4,4,1\r\n" + 
					"");
			bw.flush();
			bw.close();
		}
		catch(IOException e){
			e.printStackTrace();
			fail();
		}
		
		DataSet d = null;
		try {
			d = new DataSet("./testDataset.csv");
		}
		catch(DataFormatException e) {
			e.printStackTrace();
		}
		
		assertEquals(2, d.getWeightForRow(0), 0.00000000001);
		assertEquals(2, d.getWeightForRow(1), 0.00000000001);
		assertEquals(1, d.getWeightForRow(2), 0.00000000001);
		
		//function should return null if the requested row doesn't exist
		assertNull(d.getWeightForRow(3));
		
		f = new File("./testDataset.csv");
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			bw.write("input,input,output\r\n" + 
					"1,1,0\r\n" + 
					"2,3,5\r\n" + 
					"4,4,4\r\n" + 
					"");
			bw.flush();
			bw.close();
		}
		catch(IOException e){
			e.printStackTrace();
			fail();
		}
		
		d = null;
		try {
			d = new DataSet("./testDataset.csv");
		}
		catch(DataFormatException e) {
			e.printStackTrace();
		}
		
		//function should return null if the DataSet is unweighted
		assertNull(d.getWeightForRow(0));
		assertNull(d.getWeightForRow(1));
		assertNull(d.getWeightForRow(2));
		
		f.delete();
	}
	
	//Configuration tests
	@Test
	public void testConfigurationCreate() {
		//test default values
		Configuration configuration = new Configuration();
		assertEquals(0.8, configuration.weightMutationChance);
		assertEquals(0.03, configuration.nodeMutationChance);
		assertEquals(0.05, configuration.linkMutationChance);
		assertEquals(0.75, configuration.disableMutationChance);
		assertEquals(1.0, configuration.c1);
		assertEquals(1.0, configuration.c2);
		assertEquals(0.4, configuration.c3);
		assertEquals(1.0, configuration.compatabilityThreshold);
		assertEquals(150, configuration.initialPopulationSize);
		assertEquals(1000, configuration.generations);
		assertEquals(0.75, configuration.crossoverProportion);
		assertEquals(3, configuration.depth);
		
		//test default values when providing an empty configuration file
		File f = new File("./testConfig.txt");
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			bw.write("# empty config file");
			bw.flush();
			bw.close();
		}
		catch(IOException e){
			e.printStackTrace();
			fail();
		}

		configuration = new Configuration("./testConfig.txt");

		assertEquals(0.8, configuration.weightMutationChance);
		assertEquals(0.03, configuration.nodeMutationChance);
		assertEquals(0.05, configuration.linkMutationChance);
		assertEquals(0.75, configuration.disableMutationChance);
		assertEquals(1.0, configuration.c1);
		assertEquals(1.0, configuration.c2);
		assertEquals(0.4, configuration.c3);
		assertEquals(1.0, configuration.compatabilityThreshold);
		assertEquals(150, configuration.initialPopulationSize);
		assertEquals(1000, configuration.generations);
		assertEquals(0.75, configuration.crossoverProportion);
		assertEquals(3, configuration.depth);
		assertEquals("./styles", configuration.stylePath);
		assertEquals("normal", configuration.renderStyle);

		
		//test custom values when providing a configuration file
		f = new File("./testConfig.txt");
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			bw.write("WEIGHT_MUTATION_CHANCE=0.9\r\n" + 
					"NODE_MUTATION_CHANCE=0.02\r\n" + 
					"LINK_MUTATION_CHANCE=0.07\r\n" + 
					"DISABLE_MUTATION_CHANCE=0.15\r\n" + 
					"C1=2.0\r\n" + 
					"C2=3.5\r\n" + 
					"C3=0.8\r\n" + 
					"# test comment\r\n" + 
					"COMPATABILITY_THRESHOLD=2.25\r\n" + 
					"INITIAL_POPULATION_SIZE=33\r\n" + 
					"GENERATIONS=850\r\n" + 
					"CROSSOVER_PROPORTION=2.3\r\n" + 
					"DEPTH=7\r\n" +
					"STYLE_PATH=C:/styles\r\n" + 
					"RENDER_STYLE=glow");
			bw.flush();
			bw.close();
		}
		catch(IOException e){
			e.printStackTrace();
			fail();
		}

		configuration = new Configuration("./testConfig.txt");

		assertEquals(0.9, configuration.weightMutationChance);
		assertEquals(0.02, configuration.nodeMutationChance);
		assertEquals(0.07, configuration.linkMutationChance);
		assertEquals(0.15, configuration.disableMutationChance);
		assertEquals(2.0, configuration.c1);
		assertEquals(3.5, configuration.c2);
		assertEquals(0.8, configuration.c3);
		assertEquals(2.25, configuration.compatabilityThreshold);
		assertEquals(33, configuration.initialPopulationSize);
		assertEquals(850, configuration.generations);
		assertEquals(2.3, configuration.crossoverProportion);
		assertEquals(7, configuration.depth);
		assertEquals("C:/styles", configuration.stylePath);
		assertEquals("glow", configuration.renderStyle);
		
		//create should fail if values are not the expected format
		f = new File("./testConfig.txt");
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			bw.write("WEIGHT_MUTATION_CHANCE=thisisastring\r\n");
			bw.flush();
			bw.close();
		}
		catch(IOException e){
			e.printStackTrace();
			fail();
		}
		
	    Executable testBlock = () -> { new Configuration("./testConfig.txt"); };		
	    assertThrows(RuntimeException.class, testBlock);
	    
	    f.delete();
	}
}
