package com.devmel.apps.mcuprogrammer.view.swing;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import com.devmel.apps.mcuprogrammer.datas.TargetsConfig;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.URL;

public class WiringDiagramView extends JPanel{
	private static final long serialVersionUID = -3845648396577971250L;
	private final WiringCanvas canvas = new WiringCanvas();
	private final BufferedImage bi = new BufferedImage(600, 400, BufferedImage.TYPE_INT_RGB);;
	private final TargetsConfig targetConfig;
	private String voltage;
	private String manufacturer = null;
	private String target = null;
	private String portClass = null;
	private String programmer = null;
	private boolean ready = false;
	private boolean change = false;
	
	public WiringDiagramView(TargetsConfig targetConfig) {
		this.targetConfig=targetConfig;
		setLayout(new GridBagLayout());
		setBackground(Color.WHITE);
		add(canvas);
	}

	public void setVoltage(String voltage){
		if(this.voltage != voltage){
			this.voltage=voltage;
			update();
		}
	}
	public void setManufacturer(String manufacturer){
		if(this.manufacturer != manufacturer){
			this.manufacturer=manufacturer.toLowerCase();
			update();
		}
	}
	public void setTarget(String target){
		if(this.target != target){
			if(target != null){
				this.target=target.toLowerCase().replaceAll("\\p{C}", "").replaceAll(" ", "");
			}else{
				this.target=target;
			}
			update();
		}
	}
	public void setProgrammer(String programmer){
		if(this.programmer != programmer){
			this.programmer=programmer;
			update();
		}
	}
	public void setPortClass(String portClass){
		if(this.portClass != portClass){
			this.portClass=portClass;
			update();
		}
	}
	
	public boolean isChanged(){
		boolean ret = change;
		change = false;
		if(ret)
			canvas.repaint();
		return ret;
	}
	public boolean isReady(){
		return ready;
	}
	
	public void update(){
		ready = false;
		//Try to build image
        Image programmerImage = null;
        Image targetImage = null;
        
		try {
			URL programmerURL = getClass().getResource("/res/"+portClass + "." +programmer+"-"+voltage+"v.png");
			programmerImage = ImageIO.read(programmerURL);
		} catch (Exception e) {
			programmerImage = null;
		}
		try {
			URL targetURL = new URL(targetConfig.getResources(), manufacturer+"."+target+"."+programmer+".png");
			targetImage = ImageIO.read(targetURL);
		} catch (Exception e) {
			targetImage = null;
		}
		Graphics2D g2d = bi.createGraphics();
		g2d.setPaint(Color.WHITE);
		g2d.fillRect(0, 0, bi.getWidth(), bi.getHeight());
		if(programmerImage != null && targetImage != null){
//			System.out.println("programmer = "+programmerImage);
//			System.out.println("target = "+targetImage);
			g2d.drawImage(programmerImage, 0, 0, 300, 400, null);
			g2d.drawImage(targetImage, 300, 0, 300, 400, null);
			change = true;
			ready = true;
		}
	}

	private class WiringCanvas extends Canvas{
		private static final long serialVersionUID = 610102417278220095L;
		
		@Override
		public void paint(Graphics g) {
			super.paint(g);
			g.drawImage(bi, 0, 0, WiringCanvas.this);
		}
		@Override
		public Dimension getPreferredSize() {
			if (isPreferredSizeSet()) {
				return super.getPreferredSize();
			}
			return new Dimension(bi.getWidth(), bi.getHeight());
		}
	}

}
