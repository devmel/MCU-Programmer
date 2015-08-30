package com.devmel.apps.mcuprogrammer.memories;

public abstract class Memory {
	public String name;
	public int startAddr;
	public int size;
	
	public Memory(String name, int startAddr, int size){
		this.name=name;
		this.startAddr=startAddr;
		this.size=size;
	}
	
}
