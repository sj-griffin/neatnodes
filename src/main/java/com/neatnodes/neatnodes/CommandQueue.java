package com.neatnodes.neatnodes;

import java.util.LinkedList;
import java.util.Queue;

public class CommandQueue {
	private Queue<Runnable> commands;
	protected final Object lock = new Object();
	
	
	public CommandQueue() {
		commands = new LinkedList<Runnable>();
	}
	
	public void push(Runnable command) {
		synchronized(lock) {
			commands.add(command);
		}
	}
	
	public Runnable pop() {
		synchronized(lock) {
			return commands.poll();
		}
	}
	
	public int getSize() {
		synchronized(lock) {
			return commands.size();
		}
	}
}
