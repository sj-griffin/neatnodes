package com.neatnodes.neatnodes;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.graphstream.ui.swingViewer.DefaultView;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.swingViewer.util.GraphMetrics;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;

public class GenomeRenderer implements ViewerListener, Runnable {
	
	private Graph graph;
	private int genomesRendered;
	
	//used to arrange genomes into rows and columns for positioning on the canvas
	private int generation;
	private ArrayList<Genome> currentGeneration;
	
	protected boolean loop = true; //required by interface
	
	private Viewer viewer;
	private ViewPanel view;
	private ViewerPipe fromViewer;
	
	private SpriteManager spriteManager;
	
	private double zoomLevel;
	private Point3 panPoint;
	
	private CommandQueue commandQueue;
	
	public GenomeRenderer(String style, CommandQueue commandQueue) {
		this.genomesRendered = 0;
		this.generation = 1;
		this.currentGeneration = new ArrayList<Genome>();
		this.zoomLevel = 1.0;
		this.panPoint = new Point3(0,0);
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		this.graph = new MultiGraph("GenomeGraph");
		graph.addAttribute("ui.stylesheet", "url('./styles/" + style + ".css')");
		//graph.addAttribute("ui.quality");
		//graph.addAttribute("ui.antialias");
		
		spriteManager = new SpriteManager(this.graph);
		
		this.viewer = this.graph.display();
		this.view = viewer.getDefaultView();
		
		//remove all the default mouse listeners. This disables unwanted features like selection boxes and the ability to drag nodes and sprites
		MouseListener[] mouseListeners = this.view.getMouseListeners();
		for(MouseListener m : mouseListeners){
			this.view.removeMouseListener(m);
		}
		
		MouseMotionListener[] motionListeners = this.view.getMouseMotionListeners();
		for(MouseMotionListener m : motionListeners){
			this.view.removeMouseMotionListener(m);
		}
		
		//listener to perform panning
		this.view.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				
			}

			@Override
			public void mouseExited(MouseEvent e) {
				
			}

			@Override
			public void mousePressed(MouseEvent e) {
				panPoint = view.getCamera().transformPxToGu(e.getX(), e.getY());
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				panPoint = new Point3(0,0);
			}
			
		});
		
		this.view.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent e) {
				Point3 transformedMousePos = view.getCamera().transformPxToGu(e.getX(), e.getY());
                double deltaX = transformedMousePos.x - panPoint.x;
                double deltaY = transformedMousePos.y - panPoint.y;
                Point3 currentViewCentre = view.getCamera().getViewCenter();
                view.getCamera().setViewCenter(currentViewCentre.x - deltaX, currentViewCentre.y - deltaY, 0);
                
				//this is a convoluted way of calling pushView() on the camera so that it will update its metrics to take the recentre we just performed into account
                //if we don't do this it won't be able to transform the coordinates in the next step improperly which causes a jittering effect
                Graphics2D graphics = (Graphics2D)view.getGraphics();
				DefaultView dView = (DefaultView)view;
				dView.render(graphics);
				
                panPoint = view.getCamera().transformPxToGu(e.getX(), e.getY());
			}
			
			@Override
			public void mouseMoved(MouseEvent e) {
			}
			
		});
		
		//listener to perform zooming
		this.view.addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent e) {
				Point2D rawMousePos = e.getPoint();
				Point3 viewCentrePreZoom = view.getCamera().getViewCenter();
				Point3 mousePosPreZoom = view.getCamera().transformPxToGu(rawMousePos.getX(), rawMousePos.getY());

				//perform the zoom
				if (e.getWheelRotation() == -1) {
					zoomLevel = zoomLevel - 0.1 * zoomLevel;
					if (zoomLevel < 0.01) {
						zoomLevel = 0.01;
					}
					view.getCamera().setViewPercent(zoomLevel);
				}
				if (e.getWheelRotation() == 1) {
					zoomLevel = zoomLevel + 0.1 * zoomLevel;
					view.getCamera().setViewPercent(zoomLevel);
				}
				
				//this is a convoluted way of calling pushView() on the camera so that it will update its metrics to take the zoom we just performed into account
				//if we don't do this it won't be able to transform the coordinates properly in the next stanza
				Graphics2D graphics = (Graphics2D)view.getGraphics();
				DefaultView dView = (DefaultView)view;
				dView.render(graphics);
				
				//adjust the camera view centre so that the mouse remains in the same position on the graph after the zoom
				Point3 mousePosPostZoom = view.getCamera().transformPxToGu(rawMousePos.getX(), rawMousePos.getY());
				Point3 delta = new Point3(mousePosPostZoom.x - mousePosPreZoom.x, mousePosPostZoom.y - mousePosPreZoom.y);
				view.getCamera().setViewCenter(viewCentrePreZoom.x - delta.x, viewCentrePreZoom.y - delta.y, 0);				
			}
		});

		// We connect back the viewer to the graph,
		// the graph becomes a sink for the viewer.
		// We also install us as a viewer listener to
		// intercept the graphic events.
		this.fromViewer = viewer.newViewerPipe();
		this.fromViewer.addViewerListener(this);
		this.fromViewer.addSink(graph);

		this.commandQueue = commandQueue;
	}
	
	
	public void viewClosed(String id) {
		loop = false;
	}

	public void buttonPushed(String id) {
		System.out.println("Button pushed on node "+id);
	}

	public void buttonReleased(String id) {
		System.out.println("Button released on node "+id);
	}
	
	@Override
	public void run() {
		// Then we need a loop to do our work and to wait for events.
		// In this loop we will need to call the
		// pump() method before each use of the graph to copy back events
		// that have already occurred in the viewer thread inside
		// our thread.
		int loopNum = 0;
		while(loop) {
			this.fromViewer.pump();
			
			Runnable command = commandQueue.pop();
			if(command != null) {
				command.run();
			}
			
			//reset the bias node of each genome to its original position to stop genomes from drifting out of position while still allowing nodes within each genome to position themselves nicely
			//if you do this too often the program will hang because this task is very CPU intensive, but if you don't do it often enough you get a jerking effect as the genomes noticeably oscillate back and forth between positions
			//once every million iterations seems to be the sweet spot
			if((loopNum % 1000000) == 0) {
				for(org.graphstream.graph.Node n : graph.getEachNode()) {
					if(n.hasAttribute("final-x")) {
						n.setAttribute("x", (Double)n.getAttribute("final-x"));
					}
					if(n.hasAttribute("final-y")) {
						n.setAttribute("y", (Double)n.getAttribute("final-y"));
					}
				}
				
				//resize generation markers as they must change size with the camera position
				double ratio = this.view.getCamera().getMetrics().ratioPx2Gu; //we use this ratio to convert graph units into pixel units
				for(Sprite s : spriteManager) {
					if(s.getAttribute("ui.class") == "generation_marker") {
						double radius = s.getAttribute("radius");
						s.setAttribute("ui.style", "size: " + (radius * 2 * ratio + 50) + "px;");				
					}
				}
			}
			loopNum ++;
		}
	}
	
	//adds a genome object to the current generation
	//the genome will be rendered along with all the others in the current generation when newGeneration() is called
	//we use this model because the renderer needs to know about all the genomes in a generation to position them properly
	public void addGenomeToCurrentGeneration(Genome g) {
		this.currentGeneration.add(g);
	}
	
	//takes a genome object and renders it on the graph with its bias node at the graph coordinates specified
	//if null is given for either coordinate, that coordinate will be selected automatically by GraphStream
	public void renderGenome(Genome g, Double xPos, Double yPos) {
		HashMap<Integer, Connection> connections = g.getConnectionGenes();
		HashMap<Integer, Node> nodes = g.getNodeGenes();
		
		for(int key : nodes.keySet()) {
			Node currentNode = nodes.get(key);
			//we prefix all node names with a number that identifies the genome it belongs to, as GraphStream requires unique node names
			String nodeName = Integer.toString(this.genomesRendered) + "-" + Integer.toString(currentNode.getLabel());
			org.graphstream.graph.Node n = this.graph.addNode(nodeName);
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
			
			//we explicitly set the position of the first node in each genome in order to visually separate generations
			//the other nodes are positioned automatically around it
			if(currentNode.getLabel() == 0) {
				if(xPos != null) {
					n.addAttribute("final-x", xPos);
					n.addAttribute("x", xPos);
				}
				if(yPos != null) {
					n.addAttribute("final-y", yPos);
					n.addAttribute("y", yPos);
				}
			}			
		}
		
		NumberFormat doubleFormat = new DecimalFormat("#0.00");
		for(int key : connections.keySet()) {
			Connection currentConnection = connections.get(key);
			//we prefix all edge names with a number that identifies the genome it belongs to, as all edges will appear on the same graph
			String edgeName = Integer.toString(this.genomesRendered) + "-" + Integer.toString(currentConnection.getInnovationNumber());
			String inNodeName = Integer.toString(this.genomesRendered) + "-" + Integer.toString(currentConnection.getInNode().getLabel());
			String outNodeName = Integer.toString(this.genomesRendered) + "-" + Integer.toString(currentConnection.getOutNode().getLabel());
			org.graphstream.graph.Edge e = this.graph.addEdge(edgeName, inNodeName, outNodeName, true);
			e.addAttribute("ui.label", doubleFormat.format(currentConnection.getWeight()));
		}
		this.genomesRendered ++;
	}
	
	
	//renders all genomes in the current generation before beginning a new one
	public void newGeneration() {
		System.out.println("New generation called");
		int size = this.currentGeneration.size();
		double radius = this.generation * 20; //the distance from the centre in graph units that this generation will be displayed at
		double spreadAngle = Math.toRadians(360.0 / size); //the angle in degrees required between each genome
		
		for(int i = 0; i < size; i ++) {
			double angle = spreadAngle * i;
			double x = Math.cos(angle) * radius;
			double y = Math.sin(angle) * radius;
			//System.out.println(x + "," + y);
			
			renderGenome(currentGeneration.get(i), x, y);
		}
		
		//add sprites
		Sprite label = this.spriteManager.addSprite("label-" + this.generation);
		label.addAttribute("ui.class", "generation_label");
		label.setPosition(0, radius - 10, 0);
		label.addAttribute("ui.label", "Generation " + this.generation);
		
		Sprite marker = this.spriteManager.addSprite("marker-" + this.generation);
		marker.addAttribute("ui.class", "generation_marker");
		marker.setPosition(0, 0, 0);
		
		double ratio = this.view.getCamera().getMetrics().ratioPx2Gu; //we use this ratio to convert graph units into pixel units
		marker.addAttribute("radius", radius);
		marker.addAttribute("ui.style", "size: " + (radius * 2 * ratio + 50) + "px;");
		
		this.currentGeneration = new ArrayList<Genome>();
		this.generation ++;
	}
	
	public void autoLayoutOn(boolean on) {
		System.out.println("Auto layout set: " + on);
		if(on) {
			this.viewer.enableAutoLayout();
		}
		else {
			this.viewer.disableAutoLayout();
		}
	}
	
	
	public static void main(String[] args) {
		//GenomeRenderer renderer = new GenomeRenderer("ultra_minimal");
/*		
 * 		Genome g1 = JSONTools.readGenomeFromFile("C:/genomes/XOR-93.12877329362878-2018-06-23-11-17-09-02.json");
		Genome g2 = JSONTools.readGenomeFromFile("C:/genomes/Addition-80.3555755646322-2018-06-11-14-49-14-256.json");
 * 		renderer.renderGenome(g1);
		renderer.renderGenome(g2);
		for(int i = 0; i < 100; i++) {
			renderer.renderGenome(g1);
		}
		renderer.newGeneration();
		for(int i = 0; i < 100; i++) {
			renderer.renderGenome(g1);
		}*/
	}



}
