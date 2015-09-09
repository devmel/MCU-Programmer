package com.devmel.apps.mcuprogrammer.view;

public interface IStatus {

	public void setStatus(String status);
	public void startProgress(int percentage);
	public void stopProgress();

}
