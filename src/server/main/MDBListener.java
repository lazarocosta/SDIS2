package server.main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

import server.task.commonPeer.PutChunk;
import utils.Utils;

public class MDBListener implements Runnable {

	@Override
	public void run() {
		InetAddress mdbGroup;
		try {
			mdbGroup = InetAddress.getByName(Peer.mdbAddress);
			MulticastSocket socket = new MulticastSocket(Peer.mdbPort);
			socket.joinGroup(mdbGroup);
			// Get PUTCHUNK command
			byte[] buf = new byte[70000];
			DatagramPacket receivedCmd = new DatagramPacket(buf, buf.length);
			while (!Thread.currentThread().isInterrupted()) {
				socket.receive(receivedCmd);
				String receivedCmdString = new String(receivedCmd.getData(), receivedCmd.getOffset(), receivedCmd.getLength());
				String cmdSplit[] = receivedCmdString.split("\\s+");
				if(cmdSplit[0].equals("PUTCHUNK")){
					if(cmdSplit[1].equals("1.0") || cmdSplit[1].equals(Peer.protocolVersion)){ //Always accept messages with version 1.0 but only accepts with version 2.0 if the running protocolVersion is also 2.0
						int bodyIndex = receivedCmdString.indexOf(Utils.CRLF+Utils.CRLF)+4;
						byte[] body = Arrays.copyOfRange(receivedCmd.getData(),bodyIndex,receivedCmd.getLength());
						new Thread(new PutChunk(
								cmdSplit[1], //Version
								cmdSplit[3], //fileID
								Integer.parseInt(cmdSplit[4]), //chunkNo
								Integer.parseInt(cmdSplit[5]), //RD
								body
								)).start();
					}
				}
			}
			socket.leaveGroup(mdbGroup);
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
