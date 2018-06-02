package com.neatnodes.neatnodes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;

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
		Genome g = new Genome();
		assertEquals(0, g.getNumberOfInputs());
		assertEquals(0, g.getNumberOfOutputs());
		assertEquals(false, g.isFitnessMeasured());
		
		assertEquals(0, g.getNodeGenes().size());
		assertEquals(0, g.getConnectionGenes().size());
	}
	
	@Test
	public void testGenomeAddNode(){
		Genome g = new Genome();
		g.addNode(1, Node.INPUT);
		assertEquals(1, g.getNodeGenes().size());
		assertEquals(1, g.getNumberOfInputs());
		assertEquals(0, g.getNumberOfOutputs());
		Node n = g.getNode(1);
		assertEquals(Node.INPUT, n.getType());
		assertEquals(1, n.getLabel());
		
		g.addNode(2, Node.BIAS);
		assertEquals(2, g.getNodeGenes().size());
		assertEquals(2, g.getNumberOfInputs());
		assertEquals(0, g.getNumberOfOutputs());
		n = g.getNode(2);
		assertEquals(Node.BIAS, n.getType());
		assertEquals(2, n.getLabel());
		
		g.addNode(3, Node.HIDDEN);
		assertEquals(3, g.getNodeGenes().size());
		assertEquals(2, g.getNumberOfInputs());
		assertEquals(0, g.getNumberOfOutputs());
		n = g.getNode(3);
		assertEquals(Node.HIDDEN, n.getType());
		assertEquals(3, n.getLabel());
		
		g.addNode(4, Node.OUTPUT);
		assertEquals(4, g.getNodeGenes().size());
		assertEquals(2, g.getNumberOfInputs());
		assertEquals(1, g.getNumberOfOutputs());
		n = g.getNode(4);
		assertEquals(Node.OUTPUT, n.getType());
		assertEquals(4, n.getLabel());
		
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
	}
	
	@Test
	public void testGenomeAddConnection() {
		Genome g = new Genome();
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
		Genome g = new Genome();
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
		Genome g = new Genome();
		g.addNode(1, Node.INPUT);
		g.addNode(2, Node.OUTPUT);
		g.addConnection(1, 2, 5, false, GlobalFunctions.getInnovationNumber(1, 2));
		
		g.nodeMutation();
		
		//nothing should happen if the connection is disabled
		assertNull(g.getNode(3));
		assertNull(g.getConnection(GlobalFunctions.getInnovationNumber(1, 3)));
		assertNull(g.getConnection(GlobalFunctions.getInnovationNumber(3, 2)));
		
		g.getConnection(GlobalFunctions.getInnovationNumber(1, 2)).setEnabled(true);
		g.nodeMutation();
		
		//if the connection is enabled, the mutation should occur
		Node newNode = g.getNode(3);
		Connection oldConnection = g.getConnection(GlobalFunctions.getInnovationNumber(1, 2));
		Connection newConnection1 = g.getConnection(GlobalFunctions.getInnovationNumber(1, 3));
		Connection newConnection2 = g.getConnection(GlobalFunctions.getInnovationNumber(3, 2));
		
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
		Genome g = new Genome();
		g.addNode(1, Node.BIAS);
		g.addNode(2, Node.INPUT);
		g.addNode(3, Node.INPUT);
		g.addNode(4, Node.OUTPUT);
		g.addNode(5, Node.HIDDEN);
		g.addConnection(1, 4, 1, true, GlobalFunctions.getInnovationNumber(1, 4));
		g.addConnection(2, 4, 1, true, GlobalFunctions.getInnovationNumber(2, 4));
		g.addConnection(3, 4, 1, true, GlobalFunctions.getInnovationNumber(3, 4));
		g.addConnection(5, 4, 1, true, GlobalFunctions.getInnovationNumber(5, 4));
		g.addConnection(1, 5, 1, true, GlobalFunctions.getInnovationNumber(1, 5));
		g.addConnection(2, 5, 1, true, GlobalFunctions.getInnovationNumber(2, 5));
		g.addConnection(3, 5, 1, true, GlobalFunctions.getInnovationNumber(3, 5));
		g.addConnection(4, 5, 1, true, GlobalFunctions.getInnovationNumber(4, 5));
		g.addConnection(5, 5, 1, true, GlobalFunctions.getInnovationNumber(5, 5));
		g.addConnection(4, 4, 1, true, GlobalFunctions.getInnovationNumber(4, 4));
		
		//if every possible connection is already made, there should be no change
		g.linkMutation();
		assertEquals(10, g.getConnectionGenes().size());
		
		g.addNode(6, Node.INPUT);
		g.addConnection(6, 4, 1, true, GlobalFunctions.getInnovationNumber(6, 4));
		
		//the mutation should create the connection 6 -> 5, as it is the only remaining connection that doesn't violate any rules
		g.linkMutation();
		assertEquals(12, g.getConnectionGenes().size());
		assertNotNull(g.getConnection(GlobalFunctions.getInnovationNumber(6, 5)));
	}
	
	@Test
	public void testGenomeCloneGenome() {
		Genome g1 = new Genome();
		g1.addNode(1, Node.BIAS);
		g1.addNode(2, Node.INPUT);
		g1.addNode(3, Node.OUTPUT);
		g1.addNode(4, Node.HIDDEN);
		g1.addConnection(1, 3, 1, true, GlobalFunctions.getInnovationNumber(1, 3));
		g1.addConnection(2, 3, 1, true, GlobalFunctions.getInnovationNumber(2, 3));
		g1.addConnection(4, 3, 1, true, GlobalFunctions.getInnovationNumber(4, 3));
		g1.addConnection(1, 4, 1, true, GlobalFunctions.getInnovationNumber(1, 4));
		
		g1.setFitness(100);
		
		Genome g2 = g1.cloneGenome();
		
		//the new genome should be a copy of the old one
		assertEquals(4, g2.getNodeGenes().size());
		assertEquals(4, g2.getConnectionGenes().size());
		assertEquals(2, g2.getNumberOfInputs());
		assertEquals(1, g2.getNumberOfOutputs());
		assertNotNull(g2.getNode(1));
		assertNotNull(g2.getNode(2));
		assertNotNull(g2.getNode(3));
		assertNotNull(g2.getNode(4));
		assertNotNull(g2.getConnection(GlobalFunctions.getInnovationNumber(1, 3)));
		assertNotNull(g2.getConnection(GlobalFunctions.getInnovationNumber(2, 3)));
		assertNotNull(g2.getConnection(GlobalFunctions.getInnovationNumber(4, 3)));
		assertNotNull(g2.getConnection(GlobalFunctions.getInnovationNumber(1, 4)));

		//the new genome should be editable
		assertFalse(g2.isFitnessMeasured());
	}
	
	@Test
	public void testGenomeWriteInputs() {
		Genome g = new Genome();
		g.addNode(1, Node.BIAS);
		g.addNode(2, Node.INPUT);
		g.addNode(3, Node.INPUT);
		g.addNode(4, Node.OUTPUT);
		g.addNode(5, Node.HIDDEN);
		
		HashMap<Integer, Double> inputs = new HashMap<Integer, Double>();
		inputs.put(1, 2.3);
		inputs.put(2, 4.5);
		inputs.put(3, 1.8);
		g.writeInputs(inputs);
		
		assertEquals(2.3, g.getNode(1).getValue());
		assertEquals(4.5, g.getNode(2).getValue());
		assertEquals(1.8, g.getNode(3).getValue());
		
		inputs.remove(3);
		
		//write should fail if the number of inputs is wrong
		Executable testBlock = () -> { g.writeInputs(inputs); };		
	    assertThrows(GenomeException.class, testBlock);
	    
	    //write should fail if the keys don't all correspond to bias or input nodes
	    inputs.put(4, 8.7);
	    assertThrows(GenomeException.class, testBlock);
	    
	    inputs.remove(4);
	    inputs.put(5, 4.1);
	    assertThrows(GenomeException.class, testBlock);
	    
	    inputs.remove(5);
	    inputs.put(6, 6.9);
	    assertThrows(GenomeException.class, testBlock);
	}
	
	@Test
	public void testGenomeReadOutputs() {
		Genome g = new Genome();
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
		Genome g = new Genome();
		g.addNode(1, Node.BIAS);
		g.addNode(2, Node.INPUT);
		g.addNode(3, Node.OUTPUT);
		g.addNode(4, Node.HIDDEN);
		g.addConnection(1, 3, 1, true, GlobalFunctions.getInnovationNumber(1, 3));
		g.addConnection(2, 3, 2, true, GlobalFunctions.getInnovationNumber(2, 3));
		g.addConnection(4, 3, 3, true, GlobalFunctions.getInnovationNumber(4, 3));
		g.addConnection(1, 4, 1, true, GlobalFunctions.getInnovationNumber(1, 4));
		
		HashMap<Integer, Double> inputs = new HashMap<Integer, Double>();
		inputs.put(1, 0.5);
		inputs.put(2, 2.3);
		g.writeInputs(inputs);
		
		g.run();
		
		assertEquals(0.5, g.getNode(1).getValue(), 0.000000000001);
		assertEquals(2.3, g.getNode(2).getValue(), 0.000000000001);
		assertEquals(0.9999999999859726, g.getNode(3).getValue(), 0.000000000001);
		assertEquals(0.9205614508160216, g.getNode(4).getValue(), 0.000000000001);

		g.run();

		assertEquals(0.5, g.getNode(1).getValue(), 0.000000000001);
		assertEquals(2.3, g.getNode(2).getValue(), 0.000000000001);
		assertEquals(1.0, g.getNode(3).getValue(), 0.000000000001);
		assertEquals(0.9205614508160216, g.getNode(4).getValue(), 0.000000000001);
	}
	
	@Test
	public void testGenomeReset() {
		Genome g = new Genome();
		g.addNode(1, Node.BIAS);
		g.addNode(2, Node.INPUT);
		g.addNode(3, Node.OUTPUT);
		g.addNode(4, Node.HIDDEN);
		
		HashMap<Integer, Double> inputs = new HashMap<Integer, Double>();
		inputs.put(1, 0.5);
		inputs.put(2, 2.3);
		g.writeInputs(inputs);
		
		g.reset();
		
		assertEquals(0.0, g.getNode(1).getValue(), 0.000000000001);
		assertEquals(0.0, g.getNode(2).getValue(), 0.000000000001);
		assertEquals(0.0, g.getNode(3).getValue(), 0.000000000001);
		assertEquals(0.0, g.getNode(4).getValue(), 0.000000000001);
	}
	
	@Test
	public void testGenomeSetFitness() {
		Genome g = new Genome();
		g.setFitness(25);
		
		assertTrue(g.isFitnessMeasured());
		assertEquals(25, g.getFitness());
	}
	
	@Test
	public void testGenomeGetFitness() {
		Genome g = new Genome();
		
		//method should fail if the fitness has not been set
		Executable testBlock = () -> { g.getFitness(); };		
	    assertThrows(GenomeException.class, testBlock);
	    
	    g.setFitness(80);
		assertEquals(80, g.getFitness());
	}
	
	//Species tests
	@Test
	public void testSpeciesCreate() {
		Genome g1 = new Genome();
		Species s = new Species(g1, 100, 0);
		
		assertEquals(100, s.getMaxFitness());
		assertEquals(0, s.getGenerationsWithoutImprovement());
		assertEquals(0, s.getGenomes().size());
		assertFalse(s.isFinalised());
	}
	
	@Test
	public void testSpeciesAddGenome() {
		Genome g1 = new Genome();
		g1.addNode(1, Node.BIAS);
		g1.addNode(2, Node.INPUT);
		g1.addNode(3, Node.OUTPUT);
		g1.addNode(4, Node.HIDDEN);
		g1.addConnection(1, 3, 1, true, GlobalFunctions.getInnovationNumber(1, 3));
		g1.addConnection(2, 3, 2, true, GlobalFunctions.getInnovationNumber(2, 3));
		g1.addConnection(4, 3, 3, true, GlobalFunctions.getInnovationNumber(4, 3));
		g1.addConnection(1, 4, 1, true, GlobalFunctions.getInnovationNumber(1, 4));
		
		Species s = new Species(g1, 100, 0);
		
		//if the genomes have a compatability distance less than the compatability threshold (default 1.0), the add should succeed
		Genome g2 = g1.cloneGenome();
		g2.addNode(5, Node.HIDDEN);
		g2.addConnection(5, 4, 1, true, GlobalFunctions.getInnovationNumber(5, 4));
		
		assertTrue(s.addGenome(g2));
		ArrayList<Genome> genomes = s.getGenomes();
		assertEquals(1, genomes.size());
		assertEquals(g2, genomes.get(0));

		//if the genomes have a compatability distance greater than the threshold, the add should fail
		Genome g3 = g2.cloneGenome();
		g3.addConnection(3, 4, 6, true, GlobalFunctions.getInnovationNumber(3, 4));
		g3.getConnection(GlobalFunctions.getInnovationNumber(2, 3)).setWeight(5);
		g3.getConnection(GlobalFunctions.getInnovationNumber(1, 4)).setWeight(8);
		
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
		Genome g1 = new Genome();
		g1.addNode(1, Node.BIAS);
		g1.addNode(2, Node.INPUT);
		g1.addNode(3, Node.OUTPUT);
		g1.addNode(4, Node.HIDDEN);
		g1.addConnection(1, 3, 1, true, GlobalFunctions.getInnovationNumber(1, 3));
		g1.addConnection(2, 3, 2, true, GlobalFunctions.getInnovationNumber(2, 3));
		g1.addConnection(4, 3, 3, true, GlobalFunctions.getInnovationNumber(4, 3));
		g1.addConnection(1, 4, 1, true, GlobalFunctions.getInnovationNumber(1, 4));
		Genome g2 = g1.cloneGenome();
		Genome g3 = g1.cloneGenome();
		Genome g4 = g1.cloneGenome();
		g1.setFitness(20);
		g2.setFitness(157);
		g3.setFitness(302.87);
		g4.setFitness(85.4222);
		Species s = new Species(g1, 302.87, 0);
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
		Genome g1 = new Genome();
		g1.addNode(1, Node.BIAS);
		g1.addNode(2, Node.INPUT);
		g1.addNode(3, Node.OUTPUT);
		g1.addNode(4, Node.HIDDEN);
		g1.addConnection(1, 3, 1, true, GlobalFunctions.getInnovationNumber(1, 3));
		g1.addConnection(2, 3, 2, true, GlobalFunctions.getInnovationNumber(2, 3));
		g1.addConnection(4, 3, 3, true, GlobalFunctions.getInnovationNumber(4, 3));
		g1.addConnection(1, 4, 1, true, GlobalFunctions.getInnovationNumber(1, 4));
		g1.setFitness(30);

		Species s = new Species(g1, 30, 0);
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
		Genome g1 = new Genome();
		g1.addNode(1, Node.INPUT);
		g1.addNode(2, Node.OUTPUT);
		g1.addConnection(1, 2, 1, true, GlobalFunctions.getInnovationNumber(1, 2));
		g1.setFitness(45);
		
		Species s = new Species(g1, 131, 5);
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
		
}
