package com.devmel.apps.mcuprogrammer.view.swing;

import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JLabel;

import com.devmel.apps.mcuprogrammer.R;
import com.devmel.apps.mcuprogrammer.view.IStatus;

public class StatusBar extends JPanel implements IStatus{
	private static final long serialVersionUID = -257113780246545239L;
	private final JProgressBar progressBar;
	private final JLabel lblStatus;
	
	public StatusBar() {
		progressBar = new JProgressBar();
		add(progressBar);
		lblStatus = new JLabel(R.bundle.getString("StatusBar.0")); //$NON-NLS-1$
		add(lblStatus);
	}
	
	public void setStatus(String status){
		lblStatus.setText(status);
	}

	public void startProgress(int percentage){
		if (percentage < 0 || percentage > 100){
			progressBar.setVisible(true);
			lblStatus.setVisible(false);
			progressBar.setIndeterminate(true);
		}else{
			progressBar.setIndeterminate(false);
			progressBar.setVisible(true);
			progressBar.setValue(percentage);
			lblStatus.setVisible(true);
			lblStatus.setText(percentage+"%");
		}
	}
	public void stopProgress(){
		progressBar.setVisible(false);
		lblStatus.setVisible(true);
	}
}
