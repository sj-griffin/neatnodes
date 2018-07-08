package com.neatnodes.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import org.json.*;

import com.neatnodes.algorithm.InnovationManager;
import com.neatnodes.genome.Connection;
import com.neatnodes.genome.Genome;
import com.neatnodes.genome.Node;

/**
 * Provides methods for converting between Genome objects and their JSON representation.
 * @author Sam Griffin
 */
public class JSONTools {
	/**
	 * Encode a Genome object as a JSON file. 
	 * @param genome
	 * 		The Genome to encode.
	 * @param filePath
	 * 		Path to a JSON file to create.
	 * @param comment
	 * 		Value for a comment field in the output file that can be used to provide meta information about 
	 * 		the Genome.
	 */
	public static void writeGenomeToFile(Genome genome, String filePath, String comment){	
		File f = new File(filePath);
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			bw.write("{");
			bw.newLine();
			bw.write("\t\"genome\": {");
			bw.newLine();
			bw.write("\t\t\"comment\": \"" + comment + "\",");
			bw.newLine();
			bw.write("\t\t\"nodes\": [");
			bw.newLine();
			boolean firstEntry = true;
			
			for (Map.Entry<Integer, Node> node : genome.getNodeGenes().entrySet()){
				Node n = node.getValue();
				if(!firstEntry){
					bw.write(",");
					bw.newLine();
				}
				
				bw.write("\t\t\t{");
				bw.newLine();
				bw.write("\t\t\t\t\"type\": " + n.getType() + ",");
				bw.newLine();
				bw.write("\t\t\t\t\"label\": " + n.getLabel());
				bw.newLine();
				bw.write("\t\t\t}");
				bw.flush();
				
				firstEntry = false;
			}
			bw.newLine();
			bw.write("\t\t],");
			bw.newLine();
			
			bw.write("\t\t\"connections\": [");
			bw.newLine();
			
			firstEntry = true;
			for (Map.Entry<Integer, Connection> connection : genome.getConnectionGenes().entrySet()){
				Connection c = connection.getValue();
				if(!c.isEnabled()){
					continue; //skip this iteration if the connection is disabled
				}
				
				if(!firstEntry){
					bw.write(",");
					bw.newLine();
				}
				
				bw.write("\t\t\t{");
				bw.newLine();
				bw.write("\t\t\t\t\"weight\": " + c.getWeight() + ",");
				bw.newLine();
				bw.write("\t\t\t\t\"inNode\": " + c.getInNode().getLabel() + ",");
				bw.newLine();
				bw.write("\t\t\t\t\"outNode\": " + c.getOutNode().getLabel());
				bw.newLine();
				bw.write("\t\t\t}");
				bw.flush();
				
				firstEntry = false;
			}
			bw.newLine();
			bw.write("\t\t]");
			bw.newLine();
			
			bw.write("\t}");
			bw.newLine();
			bw.write("}");
			bw.flush();
			
			bw.close();
			
		}
		catch(IOException e){
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Parse a JSON file to produce a new Genome object.
	 * @param filePath
	 * 		Path to the JSON file to read from
	 * @return
	 * 		The new Genome
	 */
	public static Genome readGenomeFromFile(String filePath){
		File f = new File(filePath);
		InnovationManager iManager = new InnovationManager();
		Genome output = new Genome(iManager);
		String asString = null;
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
			System.exit(1);
		}
		
		try{
			JSONObject obj = new JSONObject(asString).getJSONObject("genome");
			
			JSONArray nodes = obj.getJSONArray("nodes");
			for(int i = 0; i < nodes.length(); i ++){
				int type = nodes.getJSONObject(i).getInt("type");
				if(type == Node.BIAS) {
					continue; //we don't create the bias node as it has already been initiated
				}
				int label = nodes.getJSONObject(i).getInt("label");
				output.addNode(label, type);
			}
			
			JSONArray connections = obj.getJSONArray("connections");
			for(int i = 0; i < connections.length(); i ++){
				double weight = connections.getJSONObject(i).getDouble("weight");
				int inNode = connections.getJSONObject(i).getInt("inNode");
				int outNode = connections.getJSONObject(i).getInt("outNode");
				output.addConnection(inNode, outNode, weight, true, iManager.getInnovationNumber(inNode,outNode));;
			}
		}
		catch(JSONException e){
			e.printStackTrace();
			System.exit(1);
		}
		
		return output;
	}
}
