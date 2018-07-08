package com.neatnodes.ui;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Provides a thread-safe queue of Runnable objects designed to be used for controlling a GenomeRenderer.
 * @author Sam Griffin
 *
 */
public class CommandQueue {
	/**
	 * The queue of commands.
	 */
	protected Queue<Runnable> commands;
	
	/**
	 * A lock that must be acquired before performing any read or write operations on the queue.
	 */
	protected final Object lock = new Object();
	
	/**
	 * Constructor
	 */
	public CommandQueue() {
		commands = new LinkedList<Runnable>();
	}
	
	/**
	 * Add a command to the end of queue.
	 * @param command
	 * 		The command to add.
	 */
	public void push(Runnable command) {
		synchronized(lock) {
			commands.add(command);
		}
	}
	
	/**
	 * Remove a command from the front of the queue.
	 * @return
	 * 		The command at the front of the queue.
	 */
	public Runnable pop() {
		synchronized(lock) {
			return commands.poll();
		}
	}
	
	/**
	 * Get the size of the queue.
	 * @return
	 * 		The number of commands in the queue.
	 */
	public int getSize() {
		synchronized(lock) {
			return commands.size();
		}
	}
}
