package com.neatnodes.utils;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.neatnodes.algorithm.InnovationManager;
import com.neatnodes.genome.Genome;
import com.neatnodes.genome.Node;
import com.neatnodes.utils.Configuration;
import com.neatnodes.utils.DataSet;
import com.neatnodes.utils.JSONTools;

public class UtilsTests {	
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
		
		DataSet d = new DataSet("./testDataset.csv");

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
		
		d = new DataSet("./testDataset.csv");

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
	    assertThrows(RuntimeException.class, testBlock);
	    
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
	    assertThrows(RuntimeException.class, testBlock);
	    
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
	    assertThrows(RuntimeException.class, testBlock);
	    
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
	    assertThrows(RuntimeException.class, testBlock);
	    
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
	    assertThrows(RuntimeException.class, testBlock);
	    
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
	    assertThrows(RuntimeException.class, testBlock);
	    
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
	    assertThrows(RuntimeException.class, testBlock);
	    
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
	    assertThrows(RuntimeException.class, testBlock);
	    
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
	    assertThrows(RuntimeException.class, testBlock);
	    
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
		
		DataSet d = new DataSet("./testDataset.csv");
		
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
		
		d = new DataSet("./testDataset.csv");
		
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
		
		DataSet d = new DataSet("./testDataset.csv");
		
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
		
		d = new DataSet("./testDataset.csv");
		
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
		
		DataSet d = new DataSet("./testDataset.csv");
		
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
		
		d = new DataSet("./testDataset.csv");
		
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
		assertEquals(1.0, configuration.EWeight);
		assertEquals(1.0, configuration.DWeight);
		assertEquals(0.4, configuration.WWeight);
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
		assertEquals(1.0, configuration.EWeight);
		assertEquals(1.0, configuration.DWeight);
		assertEquals(0.4, configuration.WWeight);
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
					"E_WEIGHT=2.0\r\n" + 
					"D_WEIGHT=3.5\r\n" + 
					"W_WEIGHT=0.8\r\n" + 
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
		assertEquals(2.0, configuration.EWeight);
		assertEquals(3.5, configuration.DWeight);
		assertEquals(0.8, configuration.WWeight);
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
