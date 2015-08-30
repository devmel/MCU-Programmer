package com.devmel.apps.mcuprogrammer.view.swing;

import javax.swing.JPanel;

import com.devmel.apps.mcuprogrammer.controller.HexDataController;
import com.devmel.apps.mcuprogrammer.lang.Language;
import com.devmel.apps.mcuprogrammer.view.IHexView;
import com.jhe.hexed.JHexEditor;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class HexView extends JPanel implements IHexView{
	private static final long serialVersionUID = -1552571137802330239L;
	private final JPanel commandPanel;
	private JPanel hexeditor;
	private final JButton btnRead;
	private final JButton btnWrite;
	private final JButton btnVerify;
	private HexDataController controller;
	
	public HexView() {
		setLayout(new BorderLayout(0, 0));
		
	    this.addComponentListener ( new ComponentAdapter (){
	        public void componentShown ( ComponentEvent e ){
	        	if(controller!=null){
	        		controller.showView();
	        	}
	        }

	        public void componentHidden ( ComponentEvent e ){
	        	if(controller!=null){
	        		controller.hideView();
	        	}
	        }
	    });

		
		commandPanel = new JPanel();
		commandPanel.setBackground(Color.WHITE);
		add(commandPanel, BorderLayout.NORTH);
		
		btnRead = new JButton(Language.getString("HexView.0")); //$NON-NLS-1$
		btnRead.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
	        	if(controller!=null){
	        		controller.readClick();
	        	}
			}
		});
		commandPanel.add(btnRead);
		
		btnWrite = new JButton(Language.getString("HexView.1")); //$NON-NLS-1$
		commandPanel.add(btnWrite);
		btnWrite.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
	        	if(controller!=null){
	        		controller.writeClick();
	        	}
			}
		});
		
		btnVerify = new JButton(Language.getString("HexView.2")); //$NON-NLS-1$
		commandPanel.add(btnVerify);
		btnVerify.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
	        	if(controller!=null){
	        		controller.verifyClick();
	        	}
			}
		});
		
	}
	
	public void setController(HexDataController controller) {
		this.controller = controller;
	}

	
	public void setData(byte[] data, int offset, int size){
		if(hexeditor!=null){
			remove(hexeditor);
		}
		hexeditor = new JHexEditor(data,offset,size);
		add(hexeditor, BorderLayout.CENTER);
	}
	public void setEnabled(boolean enabled){
		btnRead.setEnabled(enabled);
		btnWrite.setEnabled(enabled);
		btnVerify.setEnabled(enabled);
		if(hexeditor!=null){
			hexeditor.setEnabled(enabled);
		}
	}
	
	public void setProgramWriteEnabled(boolean enabled) {
		btnWrite.setVisible(enabled);
	}

	public void setProgramEnabled(boolean enabled) {
		commandPanel.setVisible(enabled);
	}
}
