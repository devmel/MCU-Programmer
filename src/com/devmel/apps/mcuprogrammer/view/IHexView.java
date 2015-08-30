package com.devmel.apps.mcuprogrammer.view;

public interface IHexView {
	
	public void setData(byte[] data, int offset, int size);
	public void setProgramWriteEnabled(boolean enabled);
	public void setProgramEnabled(boolean enabled);
	public void setEnabled(boolean enabled);

}
