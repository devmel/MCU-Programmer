package com.devmel.apps.mcuprogrammer.view.swing;

import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JLabel;

import com.devmel.apps.mcuprogrammer.lang.Language;

public class StatusBar extends JPanel{
	private static final long serialVersionUID = -257113780246545239L;
	private final JProgressBar progressBar;
	private final JLabel lblStatus;
	
	public StatusBar() {
		progressBar = new JProgressBar();
		add(progressBar);
		progressBar.setIndeterminate(true);
		
		lblStatus = new JLabel(Language.getString("StatusBar.0")); //$NON-NLS-1$
		add(lblStatus);
	}
	
	public void setStatus(String status){
		lblStatus.setText(status);
	}
	public void startProgress(){
		progressBar.setVisible(true);
		lblStatus.setVisible(false);
	}
	public void stopProgress(){
		progressBar.setVisible(false);
		lblStatus.setVisible(true);
	}
}
