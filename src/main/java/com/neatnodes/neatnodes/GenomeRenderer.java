package com.neatnodes.neatnodes;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Queue;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.swingViewer.DefaultView;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;

public class GenomeRenderer implements ViewerListener, Runnable {
	
	private Graph graph;
	private int genomesRendered;
	
	//used to arrange genomes into rows and columns for positioning on the canvas
	private int row;
	private int column;
	
	protected boolean loop = true; //required by interface
	
	private Viewer viewer;
	private ViewPanel view;
	private ViewerPipe fromViewer;
	
	private double zoomLevel;
	private Point3 panPoint;
	
	private Queue<Runnable> commandQueue;
	
	public GenomeRenderer(String style, Queue<Runnable> commandQueue) {
		this.genomesRendered = 0;
		this.row = 0;
		this.column = 0;
		this.zoomLevel = 1.0;
		this.panPoint = new Point3(0,0);
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		this.graph = new MultiGraph("GenomeGraph");
		graph.addAttribute("ui.stylesheet", "url('./styles/" + style + ".css')");
		//graph.addAttribute("ui.quality");
		//graph.addAttribute("ui.antialias");
		
		this.viewer = this.graph.display();
		this.view = viewer.getDefaultView();
		
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
				view.endSelectionAt(0, 0); //this disables the selection box that displays by default
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
		while(loop) {
			this.fromViewer.pump();
			
			//remove a command from the queue and run it
			Runnable command = commandQueue.poll();
			if(command != null) {
				command.run();
			}
		}
	}
	
	//takes a genome object and renders it on the graph
	public void renderGenome(Genome g) {
		HashMap<Integer, Connection> connections = g.getConnectionGenes();
		HashMap<Integer, Node> nodes = g.getNodeGenes();
		
		for(int key : nodes.keySet()) {
			Node currentNode = nodes.get(key);
			//we prefix all node names with a number that identifies the genome it belongs to, as all nodes will appear on the same graph
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
				n.addAttribute("x", this.column * 10);
				n.addAttribute("y", this.row * 20);
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
		this.column ++;
	}
	
	public void newGeneration() {
		this.column = 0;
		this.row ++;
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
