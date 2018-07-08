package com.neatnodes.ui;

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

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.graphstream.ui.swingViewer.DefaultView;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;

import com.neatnodes.genome.Connection;
import com.neatnodes.genome.Genome;
import com.neatnodes.genome.Node;

/**
 * Creates interactive Genome visualisations. Visualisations are able to respond to user mouse input, and also support 
 * live simulations that update the display programmatically. A GenomeRenderer can be used simply to render individual 
 * Genomes, but it also has methods to support generational simulations that display entire generations of Genomes at 
 * once. 
 * GenomeRenederer objects are designed to be run in their own thread and controlled using the command pattern. For 
 * example:
 * <pre>
 * 
 * CommandQueue commandQueue = new CommandQueue();
 * final GenomeRenderer renderer = new GenomeRenderer(c.stylePath, c.renderStyle, commandQueue, true);
 * new Thread(renderer).start();
 * commandQueue.push(() -&gt; renderer.renderGenome(g, 0, 0, "large"));
 * </pre>
 * Calling the methods on a GenomeRenderer directly will have no effect while the thread is running, but it will 
 * respond to method calls added to it's CommandQueue. This allows it to be controlled from another thread that is 
 * running a simulation.
 */
public class GenomeRenderer implements ViewerListener, Runnable {
	Graph graph;
	int genomesRendered; //the number of genomes rendered so far
	
	int generation; //the current generation
	ArrayList<Genome> currentGeneration; //a list of Genomes in the current generation
	
	protected boolean loop = true;
	
	Viewer viewer;
	ViewPanel view;
	ViewerPipe fromViewer;
	
	SpriteManager spriteManager;
	
	double zoomLevel; //used to control zooming
	double lastZoomLevel; //used to track zoom level changes for optimisation purposes
	Point3 panPoint; //used to control panning
	
	CommandQueue commandQueue; //the queue of commands that will be processed by the main loop
	
	/**
	 * Creates a new GenomeRenderer. After creation, you must start it running in it's own thread before it will respond to 
	 * mouse events.
	 * @param stylePath
	 * 		The directory to look for style sheets in.
	 * @param style
	 * 		The name of the style to use to display Genomes. It must correspond to the name of a CSS file in the styles 
	 * 		directory.
	 * @param commandQueue
	 * 		A queue of method calls that can be used to control the GenomeRenderer from another thread while it is running.
	 * @param panMode
	 * 		Controls the mouse behaviour. If true, the mouse will control panning and zooming. This is useful in large 
	 * 		scale simulations with many genomes on screen. If false, the mouse can be used to reposition elements by 
	 * 		dragging them. This is useful in small displays where you want to get a better look at individual genomes, 
	 * 		but is detrimental in large simulations where nodes and sprites need to remain where they are for readability.
	 */
	public GenomeRenderer(String stylePath, String style, CommandQueue commandQueue, boolean panMode) {
		this.genomesRendered = 0;
		this.generation = 1;
		this.currentGeneration = new ArrayList<Genome>();
		this.zoomLevel = 1.0;
		this.panPoint = new Point3(0,0);
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		this.graph = new MultiGraph("GenomeGraph");
		
		if(stylePath.endsWith("/")) {
			stylePath = stylePath.substring(0, stylePath.length() - 1);
		}
		graph.addAttribute("ui.stylesheet", "url('" + stylePath + "/" + style + ".css')");
		//graph.addAttribute("ui.quality");
		graph.addAttribute("ui.antialias");
		
		spriteManager = new SpriteManager(this.graph);
		
		this.viewer = this.graph.display();
		this.view = viewer.getDefaultView();
		
		if(panMode) {
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
					
					
					if(lastZoomLevel <= 0.5 && zoomLevel > 0.5) {
						setSizeClasses("small");
					}
					else if(lastZoomLevel >= 0.5 && zoomLevel < 0.5) {
						setSizeClasses("medium");
					}
					else if(lastZoomLevel <= 0.15 && zoomLevel > 0.15) {
						setSizeClasses("medium");
					}
					else if(lastZoomLevel >= 0.15 && zoomLevel < 0.15) {
						setSizeClasses("large");
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
								
					lastZoomLevel = zoomLevel;
				}
			});
		
		}

		this.fromViewer = viewer.newViewerPipe();
		this.fromViewer.addViewerListener(this);
		this.fromViewer.addSink(graph);

		this.commandQueue = commandQueue;
	}
	
	/**
	 * Called automatically when the view is closed. Stops the thread.
	 */
	public void viewClosed(String id) {
		loop = false;
	}

	/**
	 * Called automatically by GraphStream.
	 */
	public void buttonPushed(String id) {
	}

	/**
	 * Called automatically by GraphStream.
	 */
	public void buttonReleased(String id) {
	}
	
	/**
	 * Called automatically when the thread is started. Continuously runs a loop that processes mouse inputs and 
	 * commands from the command queue. It is also responsible for moving and resizing elements that need to be 
	 * updated on a continuous basis.
	 */
	@Override
	public void run() {
		int loopNum = 0;
		while(loop) {
			this.fromViewer.pump();
			
			Runnable command = commandQueue.pop();
			if(command != null) {
				command.run();
			}
			
			//Reset the bias node of each genome to its original position to stop genomes from drifting out of position. 
			//This way we can position genomes where we like, while still allowing GraphStream's auto-layout feature to 
			//position the nodes within each genome so that they don't all appear on top of each other.
			//If we do this too often the program will hang because this task is relatively CPU intensive, but if we 
			//don't do it often enough we get a jerking effect as the genomes noticeably oscillate back and forth between 
			//positions. Once every million iterations seems to be the sweet spot.
			if((loopNum % 1000000) == 0) {
				for(org.graphstream.graph.Node n : graph.getEachNode()) {
					if(n.hasAttribute("final-x")) {
						n.setAttribute("x", (Double)n.getAttribute("final-x"));
					}
					if(n.hasAttribute("final-y")) {
						n.setAttribute("y", (Double)n.getAttribute("final-y"));
					}
				}
				resizeGenerationMarkers();
			}
			loopNum ++;
		}
	}
	

	/**
	 * For use in generational simulations. Adds a Genome to the current generation. The Genome will not be rendered 
	 * until the next time newGeneration() is called. This allows us to batch Genomes into generations so that we 
	 * can optimise their positioning.
	 * @param genome
	 * 		The Genome to add.
	 */
	public void addGenomeToCurrentGeneration(Genome genome) {
		this.currentGeneration.add(genome);
	}
	

	/**
	 * Takes a Genome and renders it on the graph with its bias node locked to the graph coordinates specified. If null 
	 * is given as a value for either coordinate, that coordinate will be selected automatically.
	 * @param genome
	 * 		The Genome to render.
	 * @param xPos
	 * 		The x position in graph coordinates.
	 * @param yPos
	 * 		The y position in graph coordinates.
	 * @param styleSize
	 * 		Determines the initial size class the genome will use, which will determine its initial style. Valid values 
	 * 		are "small", "medium", or "large".
	 */
	public void renderGenome(Genome genome, Double xPos, Double yPos, String styleSize) {
		HashMap<Integer, Connection> connections = genome.getConnectionGenes();
		HashMap<Integer, Node> nodes = genome.getNodeGenes();
		
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
				throw new RuntimeException("Invalid node type. This indicates a bug.");
			}
			n.addAttribute("ui.label", currentNode.getLabel());
			n.setAttribute("ui.class", n.getAttribute("ui.class") + ", " + styleSize);

			//We explicitly set the position of the first node in each genome in order to visually separate generations.
			//The other nodes are positioned automatically around it by GraphStream's auto-layout feature.
			//Setting the final-x and final-y attributes will ensure that a node will be continuously reset to that 
			//position by the main loop so that it doesn't get moved by the auto-layout.
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
			e.setAttribute("ui.class", e.getAttribute("ui.class") + ", " + styleSize);
			e.addAttribute("ui.label", doubleFormat.format(currentConnection.getWeight()));
		}
		this.genomesRendered ++;
	}
	
	

	/**
	 * For use in generational simulations. Renders all Genomes in the current generation and begins a new empty 
	 * generation. Rendering Genomes as a batch allows for much more readable positioning.
	 */
	public void newGeneration() {
		int size = this.currentGeneration.size();
		double radius = this.generation * 20; //the distance from the centre in graph units that this generation will be displayed at
		double spreadAngle = Math.toRadians(360.0 / size); //the angle in degrees required between each genome
		
		for(int i = 0; i < size; i ++) {
			double angle = spreadAngle * i;
			double x = Math.cos(angle) * radius;
			double y = Math.sin(angle) * radius;
			
			renderGenome(currentGeneration.get(i), x, y, "small");
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
	
	/**
	 * Switches the GraphStream auto-layout feature on or off. In generational simulations, genomes are positioned 
	 * by the GenomeRenderer itself. However, GraphStream's auto-layout is still used to position the individual nodes 
	 * within each genome so that they don't all fall on top of each other. After the positioning is done, 
	 * auto-layout can become detrimental as it will prevent nodes from staying still and will consume resources 
	 * trying to optimise the positioning. It can be useful to turn it off once there are no more genomes to be rendered.
	 * @param on
	 * 		If true, auto-layout will be turned on. If false, auto-layout will be turned off.
	 */
	public void autoLayoutOn(boolean on) {
		System.out.println("Auto layout set: " + on);
		if(on) {
			this.viewer.enableAutoLayout();
		}
		else {
			this.viewer.disableAutoLayout();
		}
	}
	

	/**
	 * Sets the size class of every node and edge on the graph. This will replace any existing classes on 
	 * each node/edge except for the first one.
	 * @param newSize
	 * 		The new size class. Valid values are "small", "medium", or "large".
	 */
	private void setSizeClasses(String newSize) {
		for(org.graphstream.graph.Node node : graph.getEachNode()) {
			String currentValue = ((String)node.getAttribute("ui.class")).split(", ")[0];
			node.setAttribute("ui.class", currentValue + ", " + newSize);
		}
		for(org.graphstream.graph.Edge edge : graph.getEachEdge()) {
			String currentValue = ((String)edge.getAttribute("ui.class")).split(", ")[0];
			edge.setAttribute("ui.class", currentValue + ", " + newSize);
		}
	}
	
	/**
	 * Resize all generation markers so that they change size with the camera position. This should be called 
	 * whenever the camera is moved or zoomed.
	 */
	private void resizeGenerationMarkers() {
		double ratio = this.view.getCamera().getMetrics().ratioPx2Gu; //we use this ratio to convert graph units into pixel units
		for(Sprite s : spriteManager) {
			if(s.getAttribute("ui.class") == "generation_marker") {
				double radius = s.getAttribute("radius");
				s.setAttribute("ui.style", "size: " + (radius * 2 * ratio + 50) + "px;");				
			}
		}
	}
	
}
