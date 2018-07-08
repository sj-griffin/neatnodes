package com.neatnodes.algorithm;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.neatnodes.algorithm.Innovation;
import com.neatnodes.algorithm.InnovationManager;
import com.neatnodes.algorithm.Simulation;
import com.neatnodes.algorithm.Species;
import com.neatnodes.algorithm.StaticFunctions;
import com.neatnodes.genome.Connection;
import com.neatnodes.genome.Genome;
import com.neatnodes.genome.Node;
import com.neatnodes.utils.Configuration;
import com.neatnodes.utils.DataSet;

public class AlgorithmTests {
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
	    assertThrows(RuntimeException.class, testBlock);
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
	    assertThrows(RuntimeException.class, testBlock);
	    
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
	    assertThrows(RuntimeException.class, testBlock);
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
	
	//Simulation tests
	@Test
	public void testSimulationTestFitness() {
		DataSet d = new DataSet("./datasets/XOR.csv");
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

		assertEquals(92.82474446330792, Simulation.testFitness(g, d, 3), 0.00000000000001);
	}
	
	@Test
	public void testSimulationRunFunction() {
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
		assertArrayEquals(expectedOutputs1, Simulation.runFunction(g, inputs1, configuration.depth));
		
		Double[] inputs2 = {1.0, 1.0};
		Double[] expectedOutputs2 = {0.08756708774628402};
		assertArrayEquals(expectedOutputs2, Simulation.runFunction(g, inputs2, configuration.depth));
		
		Double[] inputs3 = {0.0, 1.0};
		Double[] expectedOutputs3 = {0.9849160396696722};
		assertArrayEquals(expectedOutputs3, Simulation.runFunction(g, inputs3, configuration.depth));
		
		Double[] inputs4 = {1.0, 0.0};
		Double[] expectedOutputs4 = {0.9837502384439617};
		assertArrayEquals(expectedOutputs4, Simulation.runFunction(g, inputs4, configuration.depth));
		
		//should fail if the inputs do not match what is defined in the genome
		Double[] inputs5 = {1.0, 0.0, 1.0};
		Executable testBlock = () -> { Simulation.runFunction(g, inputs5, configuration.depth); };		
	    assertThrows(RuntimeException.class, testBlock);
	    
		Double[] inputs6 = {1.0};
		testBlock = () -> { Simulation.runFunction(g, inputs6, configuration.depth); };		
	    assertThrows(RuntimeException.class, testBlock);
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

}
