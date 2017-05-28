package utils;

import java.io.Serializable;

public class SimpleURL implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String ipAddress;
	private int port;

	public SimpleURL(String ipAddress, int port) {
		this.ipAddress = ipAddress;
		this.port = port;
	}

	public SimpleURL(String ipAndPort) {
		int colonPos = ipAndPort.indexOf(':');
		this.ipAddress = ipAndPort.substring(0, colonPos);
		this.port = Integer.parseInt(ipAndPort.substring(colonPos + 1));
	}

	public String toString() {
		return ipAddress + ":" + port;
	}
	
	 public int hashCode(){

	        return ipAddress.hashCode() ^ port;
	    }


	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!SimpleURL.class.isAssignableFrom(obj.getClass())) {
			return false;
		}
		final SimpleURL url = (SimpleURL) obj;

		if (ipAddress.equals(url.getIpAddress())) {
			if (port == url.getPort()) {
				return true;
			}
		}
		return false;
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
