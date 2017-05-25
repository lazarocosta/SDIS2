package server.main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;

import server.task.commonPeer.*;
import utils.SimpleURL;

public class Listener implements Runnable {

	@Override
	public void run() {
		try {
			// Get GETCHUNK, DELETE or REMOVED command
			byte[] buf = new byte[70000];
			DatagramPacket receivedCmd = new DatagramPacket(buf, buf.length);
			while (!Thread.currentThread().isInterrupted()) {
				Peer.node.getUDPSocket().receive(receivedCmd);
				String cmdSplit[] = new String(receivedCmd.getData(), receivedCmd.getOffset(), receivedCmd.getLength()).split("\\s+");
				if (cmdSplit[1].equals("1.0") || cmdSplit[1].equals(Peer.protocolVersion)) { //Always accept messages with version 1.0 but only accepts with version 2.0 if the running protocolVersion is also 2.0
					System.out.println(cmdSplit[0]);
					/*if (cmdSplit[0].equals("GETCHUNK")) {
						new Thread(new GetChunk(
								cmdSplit[1],
								cmdSplit[2],
								cmdSplit[3],
								Integer.parseInt(cmdSplit[4])
								)).start();
					} else*/ if (cmdSplit[0].equals("DELETE")) {
						System.out.println("RECEBI DELETE!!!!");
						new Thread(new Delete(
								cmdSplit[2],
								cmdSplit[3]
								)).start();
					/*} else if (cmdSplit[0].equals("REMOVED")) {
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
					} else if (cmdSplit[0].equals("PUTCHUNK")) {*/
						/*int bodyIndex = receivedCmdString.indexOf(Utils.CRLF + Utils.CRLF) + 4;
						byte[] body = Arrays.copyOfRange(receivedCmd.getData(), bodyIndex, receivedCmd.getLength());
						new Thread(new PutChunk(
								cmdSplit[1], //Version
								cmdSplit[3], //fileID
								Integer.parseInt(cmdSplit[4]), //chunkNo
								Integer.parseInt(cmdSplit[5]), //RD
								body
								)).start();*/
					} else if (cmdSplit[0].equals("PUTCHUNK?")){
						System.out.println("RECEBI PEDIDO CONEXÃO TCP DE:" + cmdSplit[2]);
						//TODO confirmar se pode mesmo se ligar para receber
						//Passar para outra funcao
						SimpleURL url = new SimpleURL(cmdSplit[3]);
						new Thread(new PutChunk(
								cmdSplit[1], //Version
								cmdSplit[2],
								url.getIpAddress(),
								url.getPort()
								)).start();
					}else if (cmdSplit[0].equals("GETCHUNK?")){
						System.out.println("RECEBI PEDIDO CONEXÃO TCP DE:" + cmdSplit[2]);
						//TODO confirmar se pode mesmo se ligar para receber
						//Passar para outra funcao
						SimpleURL url = new SimpleURL(cmdSplit[3]);
						new Thread(new PutChunk(
								cmdSplit[1], //Version
								cmdSplit[2],
								url.getIpAddress(),
								url.getPort()
								)).start();
						//Socket s = new Socket(InetAddress.getByName(url.getIpAddress()), url.getPort());

					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}