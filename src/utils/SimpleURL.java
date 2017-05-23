package utils;

import java.io.Serializable;

public class SimpleURL implements Serializable {
	
	private String ipAddress;
	private int port;
	
	public SimpleURL(String ipAddress, int port){
		this.ipAddress = ipAddress;
		this.port = port;
	}

	public String toString(){
		return ipAddress + ":" + port;
	}
	
	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
