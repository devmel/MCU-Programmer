package com.devmel.apps.mcuprogrammer.controller;

import com.devmel.apps.mcuprogrammer.datas.DataArray;
import com.devmel.apps.mcuprogrammer.sections.MemoryHex;
import com.devmel.apps.mcuprogrammer.view.IHexView;

public class HexDataController {
	private final GUIController mainController;
	private final MemoryHex section;
	private final IHexView hexView;
	private boolean inProgress = false;
	
	public HexDataController(DataArray tabdata, MemoryHex section, IHexView hexView, GUIController mainController){
		this.mainController=mainController;
		this.section=section;
		this.hexView=hexView;
		hexView.setData(tabdata.rawdata, section.startAddr, section.size);
	}

	public void showView(){
		if(section.type==2){
			this.hexView.setProgramEnabled(true);
			this.hexView.setProgramWriteEnabled(true);
		}else if(section.type==1){
			this.hexView.setProgramEnabled(true);
			this.hexView.setProgramWriteEnabled(false);
		}else{
			this.hexView.setProgramEnabled(false);
		}
		hexView.setEnabled(true);
	}
	
	public void hideView(){
		hexView.setEnabled(false);
		stopCommunication();
	}
	
	public void readClick(){
		mainController.readMemoryClick(section);
	}
	public void writeClick(){
		mainController.writeMemoryClick(section);
	}
	public void verifyClick(){
		mainController.verifyMemoryClick(section);
	}

	
	public void startCommunication(){
		inProgress = true;
		hexView.setEnabled(false);
	}
	public void stopCommunication(){
		if(inProgress){
			showView();
		}
		inProgress = false;
	}

}
