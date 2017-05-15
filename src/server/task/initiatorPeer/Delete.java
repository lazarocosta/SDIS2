package server.task.initiatorPeer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import server.main.Peer;
import utils.Utils;

public class Delete implements Runnable {

	String protocolVersion;
	String fileID;

	public Delete(String protocolVersion, String fileID) {
		this.protocolVersion = protocolVersion;
		this.fileID = fileID;
	}

	@Override
	public void run() {
		try {
		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress IPAddress = InetAddress.getByName(Peer.mcAddress);
		byte[] sendData = new String(
				"DELETE" + Utils.Space +
				protocolVersion + Utils.Space +
				Peer.serverID + Utils.Space +
				this.fileID + Utils.Space +
				Utils.CRLF + Utils.CRLF)
				.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, Peer.mcPort);
		clientSocket.send(sendPacket);
		clientSocket.close();
		if(this.protocolVersion.equals("2.0")){
			Peer.deletedFiles.add(this.fileID);
		}
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
}