package com.neatnodes.neatnodes;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;

public class GenomeRenderer {
	
	//takes a genome object and renders it using GraphStream
	public static void renderGenome(Genome g) {
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

		Graph graph = new MultiGraph("GenomeGraph");
		HashMap<Integer, Connection> connections = g.getConnectionGenes();
		HashMap<Integer, Node> nodes = g.getNodeGenes();
		
		for(int key : nodes.keySet()) {
			Node currentNode = nodes.get(key);
			String label = Integer.toString(currentNode.getLabel());
			org.graphstream.graph.Node n = graph.addNode(label);
			//set the class to use for node styling based on what type of node it is
			switch(currentNode.getType()) {
			case Node.BIAS:
				n.addAttribute("ui.class", "bias_node");
				break;
			case Node.INPUT:
				n.addAttribute("ui.class", "input_node");
				break;
			case Node.OUTPUT:
				n.addAttribute("ui.class", "output_node");
				break;
			case Node.HIDDEN:
				n.addAttribute("ui.class", "hidden_node");
				break;
			default:
				throw new GenomeException();
			}
			n.addAttribute("ui.label", currentNode.getLabel());

		}
		
		NumberFormat doubleFormat = new DecimalFormat("#0.00");
		for(int key : connections.keySet()) {
			Connection currentConnection = connections.get(key);
			String edgeLabel = Integer.toString(currentConnection.getInnovationNumber());
			String inNodeLabel = Integer.toString(currentConnection.getInNode().getLabel());
			String outNodeLabel = Integer.toString(currentConnection.getOutNode().getLabel());
			org.graphstream.graph.Edge e = graph.addEdge(edgeLabel, inNodeLabel, outNodeLabel, true);
			e.addAttribute("ui.label", doubleFormat.format(currentConnection.getWeight()));
		}
		
		graph.addAttribute("ui.stylesheet", "url('./stylesheet.css')");
		graph.addAttribute("ui.quality");
		graph.addAttribute("ui.antialias");
		graph.display();
	}
}
