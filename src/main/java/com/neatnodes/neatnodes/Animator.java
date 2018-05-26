package com.neatnodes.neatnodes;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

/**
 * <p>The Animator class maintains and updates the current
 * state of our animation.</p>
 * 
 * <p>This class extends JPanel, to provide a component upon 
 * which the animation is drawn. It also implements Runnable
 * in order to provide a thread which periodically updates
 * the animation and calls <code>repaint()</code> to persuade the event
 * dispatch thread to paint the next frame.</p>
 *
 */
public class Animator extends JPanel implements Runnable {

	private static final long serialVersionUID = 1L;

	public static final int CANVAS_WIDTH = 800;
	public static final int CANVAS_HEIGHT = 600;
	public static final int FRAME_PAUSE = 500;
	
	private static final Color BLACK = new Color(0,0,0);
	private static final Color RED = new Color(255,0,0);
	private static final Color GREEN = new Color(0,255,0);
	private static final Color BLUE = new Color(0,0,255);
	private static final Color YELLOW = new Color(255,215,0);
	
	private static final int NODE_SIZE = 30;
	
	private DecimalFormat df2 = new DecimalFormat("#.######");
	
	private Genome genome;
	
	public boolean update; //used to notify the animator that the genome has been updated
	
	/**
	 * Constructor, creates a new list of Sprite objects
	 * which starts off empty. 
	 */
	public Animator(Genome genome) {
		setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
		this.genome = genome;
		this.update = true;
	}

	/**
	 * The paintComponent() method which is called from the event
	 * dispatch thread whenever the GUI wants to repaint an
	 * Animator component.
	 */
	public void paintComponent(Graphics pGraphics) {
		pGraphics.clearRect(0, 0, getWidth(), getHeight());
		
		int inputBaseX = getWidth() / 2 - genome.getNumberOfInputs()/2 * (NODE_SIZE * 4);
		int outputBaseX = getWidth() / 2 - genome.getNumberOfInputs()/2 * (NODE_SIZE * 4);
		int hiddenBaseX = 50;
		
		int inputBaseY = getHeight() - 50;
		int outputBaseY = 50;
		int hiddenBaseY = 120;
		
		//maps node labels to the coordinates at which they are drawn
		HashMap<Integer, Coordinates> nodePositions = new HashMap<Integer, Coordinates>();
		
		//go through all nodes in the genome and draw them, saving their positons in the hashmap
		for (Map.Entry<Integer, Node> node : genome.getNodeGenes().entrySet()){
			Node n = node.getValue();
			if(n.getType() == Node.INPUT || n.getType() == Node.BIAS){
				if(n.getType() == Node.INPUT) {
					pGraphics.setColor(GREEN);
				}
				else {
					pGraphics.setColor(YELLOW);
				}
				pGraphics.drawOval(inputBaseX, inputBaseY, NODE_SIZE, NODE_SIZE);
				pGraphics.fillOval(inputBaseX, inputBaseY, NODE_SIZE, NODE_SIZE);
				pGraphics.setColor(BLACK);
				pGraphics.drawString(df2.format(n.getValue()), inputBaseX + 5, inputBaseY + 20);
				
				nodePositions.put(n.getLabel(), new Coordinates(inputBaseX, inputBaseY)); //record the position of the node
				inputBaseX += (NODE_SIZE * 4);
				
			} else if (n.getType() == Node.OUTPUT){
				pGraphics.setColor(RED);
				pGraphics.drawOval(outputBaseX, outputBaseY, NODE_SIZE, NODE_SIZE);
				pGraphics.fillOval(outputBaseX, outputBaseY, NODE_SIZE, NODE_SIZE);
				pGraphics.setColor(BLACK);
				pGraphics.drawString(df2.format(n.getValue()), outputBaseX + 5, outputBaseY + 20);
				
				nodePositions.put(n.getLabel(), new Coordinates(outputBaseX, outputBaseY)); //record the position of the node
				outputBaseX += (NODE_SIZE * 4);
				
			} else if(n.getType() == Node.HIDDEN){
				pGraphics.setColor(BLUE);
				pGraphics.drawOval(hiddenBaseX, hiddenBaseY, NODE_SIZE, NODE_SIZE);
				pGraphics.fillOval(hiddenBaseX, hiddenBaseY, NODE_SIZE, NODE_SIZE);
				pGraphics.setColor(BLACK);
				pGraphics.drawString(df2.format(n.getValue()), hiddenBaseX + 5, hiddenBaseY + 20);
				
				nodePositions.put(n.getLabel(), new Coordinates(hiddenBaseX, hiddenBaseY)); //record the position of the node
				//both x and y are incremented to draw hidden nodes diagonally to make connections less cluttered
				hiddenBaseX += (NODE_SIZE * 4);
				hiddenBaseY += (NODE_SIZE * 4);
			}
		}
		
		//go through all the connections in the genome and draw them
		for (Map.Entry<Integer, Connection> connection : genome.getConnectionGenes().entrySet()){
			//do not render the connection if it is disabled
			if(!connection.getValue().isEnabled()){
				continue;
			}
			
			int node1 = connection.getValue().getInNode().getLabel();
			int node2 = connection.getValue().getOutNode().getLabel();
			
			Coordinates c1 = nodePositions.get(node1);
			Coordinates c2 = nodePositions.get(node2);
			
			int randomOffset = (int)((Math.random() - 0.5) * (NODE_SIZE / 2)); //add a random offset to the line origin so that connections on the same path don't overlap and hide each other
			
			pGraphics.setColor(BLACK);
			pGraphics.drawLine(c1.x + (NODE_SIZE / 2) + randomOffset, c1.y + (NODE_SIZE / 2) + randomOffset, c2.x + (NODE_SIZE / 2), c2.y + (NODE_SIZE / 2));
			
			int midpointX = (c1.x + c2.x) / 2;
			int midpointY = (c1.y + c2.y) / 2;
			pGraphics.drawString(df2.format(connection.getValue().getWeight()), midpointX, midpointY);
			
			//draw an oval on the side of the midpoint that the output node is on to indicate direction
			int quarterPointX = (midpointX + c2.x) / 2;
			int quarterPointY = (midpointY + c2.y) / 2;
			
			pGraphics.drawOval(quarterPointX, quarterPointY, NODE_SIZE / 5, NODE_SIZE / 5);
			pGraphics.fillOval(quarterPointX, quarterPointY, NODE_SIZE / 5, NODE_SIZE / 5);



		}
	}

	/**
	 * The run() method of an Animator thread simply loops
	 * calling repaint() to inform the event dispatch
	 * thread that it needs to paint the next frame.
	 * 
	 * We pause between each frame for FRAME_PAUSE milliseconds,
	 * so decreasing FRAME_PAUSE we can increase the framerate (and
	 * thus the speed) of the animation.
	 */
	public void run() {
		while (true) {
			if(update){
				repaint();
				update = false;
			}
			
			try {
				Thread.sleep(FRAME_PAUSE);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private class Coordinates{
		public int x;
		public int y;
		public Coordinates(int x, int y){
			this.x = x;
			this.y = y;
		}
	}
}
