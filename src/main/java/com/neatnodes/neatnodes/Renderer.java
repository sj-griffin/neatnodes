package com.neatnodes.neatnodes;

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class Renderer extends JFrame implements ActionListener{
	private Genome genome; //the genome to render
	
	private JPanel mContentPane = null;
	private Animator mAnimator = null;
	private JButton mButton = null;

	
	public Renderer(Genome genome){
		this.genome = genome;
	}
	
	
	protected void initApp() {
		setup();
		pack();
		setVisible(true);
		new Thread(mAnimator).start();
	}
	
	private void setup() {
		setTitle("Genome Viewer");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		mButton = new JButton();
		mButton.setText("Step Forward");
		mButton.addActionListener(this);
		
		mAnimator = new Animator(genome);

		mContentPane = (JPanel)getContentPane();
		mContentPane.setLayout(new BorderLayout());
		mContentPane.add(mButton, BorderLayout.SOUTH);
		mContentPane.add(mAnimator, BorderLayout.CENTER);
	}
	
	/* (non-Javadoc)
	 * Responds to a button push by adding another animated sprite to
	 * the sprite animator panel.
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		genome.run();
		mAnimator.update = true;
	}
}
