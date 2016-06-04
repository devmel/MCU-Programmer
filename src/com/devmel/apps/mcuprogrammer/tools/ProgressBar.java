package com.devmel.apps.mcuprogrammer.tools;

import java.io.PrintStream;

/**
  * [100% ##################################################]
 */
public class ProgressBar{
	private final static String format = "\r[%3d%% %s %d/%d]";
	private final PrintStream stream;
    private final char[] bar = new char[50];
    private int currentPosition;
    
    
    public ProgressBar(PrintStream stream) {
    	this.stream=stream;
    	this.clear();
    }

    public void update(long done, long total) {
    	if(stream==null || total<done){
    		return;
    	}
    	double percentDouble = (double)done/(double)total*100.0;
        int percent = (int) Math.floor(percentDouble);
        
        int untilPosition = percent*this.bar.length/100;
        //Fill until current position
		for(;currentPosition<untilPosition;currentPosition++){
	    	this.bar[currentPosition]='#';
		}
    	try{
            stream.printf(format, percent, new String(this.bar), done, total);
    	}catch(Throwable e){
    		stream.print("\r["+percent+"% "+new String(this.bar)+" "+done+"/"+total+"]");
    	}
        if (done == total) {
        	stream.flush();
        	stream.println();
        }
    }
    
    public void clear(){
    	for(int i=0;i<this.bar.length;i++){
    		this.bar[i] = 0x20;
    	}
    	currentPosition=0;
    }
}