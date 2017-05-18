package server.main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

import server.task.commonPeer.Delete;
import server.task.commonPeer.GetChunk;
import server.task.commonPeer.PutChunk;
import server.task.commonPeer.Removed;
import server.task.commonPeer.Stored;
import utils.Utils;

public class Listener implements Runnable {

	@Override
	public void run() {
		try {
			DatagramSocket socket = new DatagramSocket(Peer.node.getPort());
			// Get GETCHUNK, DELETE or REMOVED command
			byte[] buf = new byte[70000];
			DatagramPacket receivedCmd = new DatagramPacket(buf, buf.length);
			while (!Thread.currentThread().isInterrupted()) {
				socket.receive(receivedCmd);
				String cmdSplit[] = new String(receivedCmd.getData(), receivedCmd.getOffset(), receivedCmd.getLength()).split("\\s+");
				if (cmdSplit[1].equals("1.0") || cmdSplit[1].equals(Peer.protocolVersion)) { //Always accept messages with version 1.0 but only accepts with version 2.0 if the running protocolVersion is also 2.0
					if (cmdSplit[0].equals("GETCHUNK")) {
						new Thread(new GetChunk(
								cmdSplit[1],
								cmdSplit[2],
								cmdSplit[3],
								Integer.parseInt(cmdSplit[4])
								)).start();
					} else if (cmdSplit[0].equals("DELETE")) {
						new Thread(new Delete(
								cmdSplit[2],
								cmdSplit[3]
								)).start();
					} else if (cmdSplit[0].equals("REMOVED")) {
						new Thread(new Removed(
								cmdSplit[1],
								cmdSplit[2],
								cmdSplit[3],
								Integer.parseInt(cmdSplit[4])
								)).start();
					} else if (cmdSplit[0].equals("STORED")) {
						new Thread(new Stored(
								cmdSplit[2],
								cmdSplit[3],
								Integer.parseInt(cmdSplit[4])
								)).start();
					}if (cmdSplit[0].equals("PUTCHUNK")) {
						/*int bodyIndex = receivedCmdString.indexOf(Utils.CRLF + Utils.CRLF) + 4;
						byte[] body = Arrays.copyOfRange(receivedCmd.getData(), bodyIndex, receivedCmd.getLength());
						new Thread(new PutChunk(
								cmdSplit[1], //Version
								cmdSplit[3], //fileID
								Integer.parseInt(cmdSplit[4]), //chunkNo
								Integer.parseInt(cmdSplit[5]), //RD
								body
								)).start();*/
					}
				}
			}
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}