package server.task.commonPeer;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.Random;

import server.main.Peer;
import utils.Utils;

public class GetChunk implements Runnable{

	private String protocolVersion;
	private String senderID;
	private String fileID;
	private int chunkNumber;
	private boolean chunkAlreadySent = false;

	public void setChunkAlreadySent(boolean chunkAlreadySent) {
		this.chunkAlreadySent = chunkAlreadySent;
	}

	public GetChunk(String protocolVersion, String senderID,String fileID,int chunkNumber){
		this.protocolVersion = protocolVersion;
		this.senderID = senderID;
		this.fileID = fileID;
		this.chunkNumber = chunkNumber;
	}

	@Override
	public void run() {
		// TODO
		//Check if you have chunk
		File f = new File(Peer.dataPath + Utils.FS + this.fileID + Utils.FS + this.chunkNumber);
		if (f.exists() && !f.isDirectory()) {
			//SEND CHUNK
			try {
				byte[] header = new String("CHUNK"+Utils.Space
						+ this.protocolVersion + Utils.Space
						+ Peer.serverID + Utils.Space
						+ this.fileID+ Utils.Space
						+ this.chunkNumber + Utils.Space
						+ Utils.CRLF + Utils.CRLF).getBytes();
				//Get body from file
				byte[] chunk = Files.readAllBytes(f.toPath());
				byte[] msg;
				byte[] msgTCP;
				if (this.protocolVersion.equals("1.0")){
					msg = new byte[header.length + chunk.length];
					msgTCP = new byte[0];
					System.arraycopy(header, 0, msg, 0, header.length);
					System.arraycopy(chunk, 0, msg, header.length, chunk.length);
				} else {
					msg = new byte[header.length];
					msgTCP = new byte[header.length + chunk.length];
					System.arraycopy(header, 0, msg, 0, header.length);
					System.arraycopy(header, 0, msgTCP, 0, header.length);
					System.arraycopy(chunk, 0, msgTCP, header.length, chunk.length);
				}

				//Call RECEIVECHUNK
				Thread receivedThread = new Thread(new ReceiveChunk());
				receivedThread.start();
				receivedThread.join((long)(long)new Random().nextInt(400));
				if(receivedThread.isAlive()) receivedThread.interrupt();
				if(!this.chunkAlreadySent){
					InetAddress mdrGroup = InetAddress.getByName(Peer.mdrAddress);
					DatagramSocket mdrSocket = new DatagramSocket();

					DatagramPacket sendChunk = new DatagramPacket(msg,msg.length,mdrGroup,Peer.mdrPort);
					mdrSocket.send(sendChunk);
					mdrSocket.close();
					if (this.protocolVersion.equals("2.0")){
						String[] connection = Utils.getTCPfromSenderID(this.senderID);
						System.out.println(this.senderID);
						System.out.println("IP: " + connection[0] + " , PORT: " + connection[1]);
						Socket socket = new Socket(connection[0],Integer.parseInt(connection[1]));
						OutputStream out = socket.getOutputStream(); 
					    DataOutputStream dos = new DataOutputStream(out);

					    int length = msgTCP.length;
					    dos.writeInt(length);
					    if (length > 0) {
					        dos.write(msgTCP, 0, length);
					    }
						socket.close();
					}
				}


			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	class ReceiveChunk implements Runnable {

		@Override
		public void run() {
			try {
				InetAddress mdrGroup = InetAddress.getByName(Peer.mdrAddress);
				MulticastSocket mdrSocket = new MulticastSocket(Peer.mdrPort);
				byte[] receiveMsg = new byte[70000];
				DatagramPacket receiveCommand = new DatagramPacket(receiveMsg,receiveMsg.length);
				mdrSocket.joinGroup(mdrGroup);
				mdrSocket.setSoTimeout(400);

				while(!Thread.currentThread().isInterrupted()){
					mdrSocket.receive(receiveCommand);
					String receivedCmdString = new String(receiveCommand.getData(), receiveCommand.getOffset(), receiveCommand.getLength());
					String cmdSplit[] = receivedCmdString.split("\\s+");
					if(cmdSplit[0].equals("CHUNK") && cmdSplit[3].equals(fileID) && cmdSplit[4].equals(Integer.toString(chunkNumber))){
						setChunkAlreadySent(true);
						break;
					}
				}
				mdrSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}
	}

}
