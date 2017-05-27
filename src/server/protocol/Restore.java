package server.protocol;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import de.uniba.wiai.lspi.chord.console.command.entry.Key;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import server.main.Peer;
import server.task.initiatorPeer.GetChunk;
import utils.SimpleURL;
import utils.Utils;

public class Restore {

	private boolean[] receivedChunks;

	public Restore(String protocolVersion, String fileID, int fileSize, String destDir) {

		//cria tmpFolder
		String tmpFolderPath = Peer.dataPath + Utils.FS + fileID + Utils.FS + "tmp";
		File dir = new File(tmpFolderPath);
		dir.mkdirs();

		//TODO ver se o proprio peer nao tem nenhum chunk
		ArrayList<Socket> availableConnections = getAvailablePeers(fileID);

		//Pede numero dos chunks que peers tenham
		for(Socket s:availableConnections){
			try {
			DataOutputStream outToServer = new DataOutputStream(s.getOutputStream());
				outToServer.writeBytes(new String("GETCHUNKS" + Utils.Space
						+ protocolVersion + Utils.Space
						+ fileID + Utils.Space
						+ Utils.CRLF));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		//hashmap com ligação tcp e arraylist de chunks por ligacao
		HashMap<Socket, ArrayList<Integer>> availableChunks = new HashMap<Socket, ArrayList<Integer>>();
		for(Socket s:availableConnections){
			new Thread(){
				public void run() {
					try {
						while(true){
							DataInputStream dis = new DataInputStream(s.getInputStream());
							String header = dis.readLine();
							System.out.println("Received: " + header);
							String[] strChunks = new String(header).split("\\s+");
							ArrayList<Integer> chunks = new ArrayList<Integer>();
							for(String strChunk : strChunks){
								chunks.add(Integer.parseInt(strChunk));
							}
							availableChunks.put(s, chunks);

							//TODO falta fechar somewhere
							new Thread(new ChunkReceiver(tmpFolderPath, s)).start();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}.start();
		}
		//aguarda algum tempo para ja ter peers que responderam
		//ao usar threads garantimos que peers mais lentos a responder também serão usados
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {

			//Number of Chunks needed for the file
			int numChunks = (int)(fileSize/64000)+1;
			//Size of the last chunk
			int lastChunkSize = (int)(fileSize%64000);
			//Arrays of chunks received (default all false)
			receivedChunks = new boolean[numChunks];

			//pedir chunks aos users
			boolean allReceived = true;
			for(Socket s:availableConnections){
				allReceived = true;
				for(boolean b : receivedChunks){
					if(!b){
						allReceived = false;
						break;
					}
				}
				if(!allReceived){
					ArrayList<Integer> chunksOfConnection = availableChunks.get(s);
					if(!chunksOfConnection.isEmpty()){
						while(!chunksOfConnection.isEmpty() && receivedChunks[chunksOfConnection.get(0)]){
							chunksOfConnection.remove(0);
						}

						DataOutputStream outToServer = new DataOutputStream(s.getOutputStream());
						outToServer.writeBytes(new String("GETCHUNK" + Utils.Space
								+ protocolVersion + Utils.Space
								+ fileID + Utils.Space
								+ chunksOfConnection.get(0) + Utils.Space
								+ Utils.CRLF));
						outToServer.flush();
					}
				}else{
					break;
				}
			}
			
			Utils.restoreFileFromTmpFolder(tmpFolderPath);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private ArrayList<Socket> getAvailablePeers(String fileID) {
		ArrayList<Socket> result = new ArrayList<Socket>();

		try {
			byte[] availableMsg = new String("GETCHUNK?" + Utils.Space
					+ "1.0" + Utils.Space
					+ fileID + Utils.Space
					+ Peer.node.getSimpleURL().toString() + Utils.Space
					+ Utils.CRLF + Utils.CRLF).getBytes();

			Set<Serializable> availablePeers = Peer.node.getChord().retrieve(new Key(fileID));

			ServerSocket ss = new ServerSocket(Peer.node.getPort());

			Thread t = new Thread(){
				public void run() {
					try {
						while(true){
							Socket tmpConnection = ss.accept();
							result.add(tmpConnection);
							System.out.println("ACEITEI CONEXAO TCP DE:" + tmpConnection.getRemoteSocketAddress());
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};

			t.start();
			try {
				//DatagramSocket clientSocket = new DatagramSocket();
				for(Serializable peer: availablePeers){
					if(!Peer.node.getSimpleURL().equals(peer)){
						System.out.println("vou mandar");
						InetAddress IPAddress = InetAddress.getByName(((SimpleURL)peer).getIpAddress());
						DatagramPacket sendPacket = new DatagramPacket(availableMsg, availableMsg.length, IPAddress, ((SimpleURL)peer).getPort());
						Peer.node.getUDPSocket().send(sendPacket);
					}
				}
				//clientSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}



			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			t.interrupt();
			ss.close();
		} catch (ServiceException | IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	private class ChunkReceiver implements Runnable{

		private String tmpFolderPath;
		private Socket socket;

		public ChunkReceiver(String tmpFolderPath, Socket socket){
			this.tmpFolderPath = tmpFolderPath;
			this.socket = socket;
		}

		@Override
		public void run() {
			try {
				while(true){
					DataInputStream dis = new DataInputStream(socket.getInputStream());
					String header = dis.readLine();
					System.out.println("Received: " + header);
					String cmdSplit[] = new String(header).split("\\s+");
					int chunkNumber = Integer.parseInt(cmdSplit[3]);
					int size = Integer.parseInt(cmdSplit[4]);
					byte[] body = new byte[size];
					dis.readFully(body);

					try {	File f = new File(tmpFolderPath + Utils.FS + chunkNumber);
							f.createNewFile();
							Files.write(f.toPath(), body);
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					receivedChunks[chunkNumber] = true;

					System.out.println("Received body with size: " + body.length);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}
