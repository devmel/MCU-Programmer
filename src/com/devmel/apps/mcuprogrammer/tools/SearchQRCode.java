package com.devmel.apps.mcuprogrammer.tools;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.List;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

public class SearchQRCode {
	private final Webcam webcam;
	private Search search;
	private Result result = null;
	private boolean stop = false;
	private Listener listener = null;
	
	public SearchQRCode(){
		Dimension size = WebcamResolution.QVGA.getSize();
		List<Webcam> webcams = Webcam.getWebcams();
		if(webcams != null && webcams.size() > 0){
			webcam = webcams.get(0);
			webcam.setViewSize(size);
		}else{
			throw new RuntimeException("No webcam found...");
		}
	}
	
	public void close(){
		stop = true;
		if(search != null){
			search.interrupt();
			while(search.isAlive());
			search = null;
			result = null;
		}
	}
	
	public boolean isScanning(){
		if(search !=null)
			return search.isAlive();
		return false;
	}
	
	public boolean newScan(){
		close();
		stop = false;
		if(webcam!=null){
			search = new Search();
			return search.isAlive();
		}
		return false;
	}
	
	public WebcamPanel getPanel(){
		return new WebcamPanel(webcam);
	}
	
	public String waitResult(int timeoutMs){
		int i = 0;
		while (isScanning() && getResult()==null && i < timeoutMs){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			i += 100;
		}
		return getResult();
	}

	public String getResult(){
		if(result != null){
			return result.getText();
		}
		return null;
	}
	
	public void setListener(Listener listener){
		this.listener=listener;
	}
	
	public interface Listener{
		public void onFound(String result);
		public void onStart(SearchQRCode search);
		public void onStop(SearchQRCode search);
	}
	
	private class Search extends Thread{
		private Search(){
			try {
				this.start();
			}catch(Exception e){
			}
		}
		
		@Override
		public void run() {
			try{
				webcam.open();
				if(listener!=null){
					listener.onStart(SearchQRCode.this);
				}
				while (webcam != null && result == null && stop == false){
					BufferedImage image = null;
					if (webcam.isOpen()) {
		
						if ((image = webcam.getImage()) == null) {
							continue;
						}
		
						LuminanceSource source = new BufferedImageLuminanceSource(image);
						BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
		
						try {
							result = new MultiFormatReader().decode(bitmap);
						} catch (NotFoundException e) {
							// fall thru, it means there is no QR code in image
						}
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						stop = true;
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				if(webcam != null)
					webcam.close();
			}
			if(result!=null && listener!=null){
				listener.onFound(SearchQRCode.this.getResult());
			}
			if(listener!=null){
				listener.onStop(SearchQRCode.this);
			}
		}
	}
}
