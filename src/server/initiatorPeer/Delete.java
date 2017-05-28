package server.initiatorPeer;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Set;

import server.Peer;
import utils.SimpleURL;
import utils.StringKey;
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
		Set<Serializable> peersWFile = Peer.chord.retrieve(new StringKey(this.fileID));
		for (Serializable peer : peersWFile) {
			System.out.println(peer.toString());
		}
		try {
			DatagramSocket clientSocket = new DatagramSocket();
			for (Serializable peer : peersWFile) {

				InetAddress IPAddress = InetAddress.getByName(((SimpleURL) peer).getIpAddress());
				byte[] sendData = new String("DELETE" + Utils.Space 
						+ protocolVersion + Utils.Space 
						+ this.fileID + Utils.Space 
						+ Utils.CRLF + Utils.CRLF).getBytes();
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, ((SimpleURL) peer).getPort());
				clientSocket.send(sendPacket);
			}
			clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
