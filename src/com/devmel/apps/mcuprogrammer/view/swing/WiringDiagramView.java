package com.devmel.apps.mcuprogrammer.view.swing;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.net.URL;

public class WiringDiagramView extends JPanel{
	private static final long serialVersionUID = -3845648396577971250L;
	private final WiringCanvas canvas = new WiringCanvas();
	private final String deviceImageName;
	private final String voltage;
	private final BufferedImage bi;
	private String portClass = null;
	private String programmer = null;
	
	public WiringDiagramView(String deviceImageName, String voltage) {
		this.deviceImageName=deviceImageName;
		this.voltage=voltage;
		setLayout(new GridBagLayout());
		bi = new BufferedImage(600, 400, BufferedImage.TYPE_INT_RGB);
		this.setBackground(Color.WHITE);
		add(canvas);
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
	
	public void update(){
		//Try to build image
		URL programmerImage = null;
		URL deviceImage = null;
		try {
			programmerImage = getClass().getResource("/res/"+portClass + "." +programmer+"-"+voltage+".png");
		} catch (Exception e) {
		}
		try {
			deviceImage = getClass().getResource("/res/"+deviceImageName + "." +programmer+".png");
		} catch (Exception e) {
		}
		Graphics2D g2d = bi.createGraphics();
		g2d.setPaint(Color.WHITE);
		g2d.fillRect(0, 0, bi.getWidth(), bi.getHeight());
		if(programmerImage != null){
//			System.out.println(programmerImage);
			ImageIcon prog = new ImageIcon(programmerImage);
			g2d.drawImage(prog.getImage(), 0, 0, 300, 400, null);
		}
		if(deviceImage != null){
//			System.out.println(deviceImage);
			ImageIcon dev = new ImageIcon(deviceImage);
			g2d.drawImage(dev.getImage(), 300, 0, 300, 400, null);
		}
		canvas.repaint();
	}

	private class WiringCanvas extends Canvas{
		private static final long serialVersionUID = 610102417278220095L;
		@Override
		public void paint(Graphics g) {
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
