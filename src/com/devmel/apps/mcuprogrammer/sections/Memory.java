package com.devmel.apps.mcuprogrammer.sections;

public abstract class Memory {
	public final String name;
	public final int startAddr;
	public final int size;
	protected StatusListener status;
	
	public Memory(String name, int startAddr, int size){
		this.name=name;
		this.startAddr=startAddr;
		this.size=size;
	}
	
	public void setStatusListener(StatusListener status){
		this.status=status;
	}
	
	public interface StatusListener{
		public void update(long done, long total);
	    public void clear();
	}
}
