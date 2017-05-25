package server.protocol;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Set;

import de.uniba.wiai.lspi.chord.console.command.entry.Key;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import server.main.Peer;
import server.task.initiatorPeer.PutChunk;
import utils.SimpleURL;
import utils.Utils;

public class Backup {
    public int MAX_SIZE_CHUNK = 64 * 1000;


	public Backup(String protocolVersion, String fileID, String filePath, int replicationDeg) {


        File f = new File(filePath);
        long fileLength = f.length();
        byte[] chunk = new byte[MAX_SIZE_CHUNK];

		//get File ID
		//String fileID = Utils.getFileID(filePath);
		//String lastFileID = Peer.mdMap.get(filePath);
		//if(lastFileID != null && lastFileID.equals(fileID)){
		//}else{
		//if(lastFileID != null && !lastFileID.equals(fileID)){
		//	new Thread(new Delete(protocolVersion, lastFileID)).start();
		//}
		/*if(protocolVersion.equals("2.0")){
				Peer.deletedFiles.remove(fileID); // remove from deleted files list
				for(String delFileID: Peer.deletedFiles){
					new Thread(new Delete(protocolVersion, delFileID)).start();
				}
			}*/
		//Peer.mdMap.put(filePath,fileID);
		//Utils.writeMD();

		ArrayList<Socket> availableConnections = getAvailablePeers(fileID);

		try {
			InputStream in = new FileInputStream(f);

                //Number of Chunks needed for the file
                int numChunks = (int) (fileLength / MAX_SIZE_CHUNK) + 1;
                //Size of the last chunk
                int lastChunkSize = (int) (fileLength % MAX_SIZE_CHUNK);

			//Create chunks and call PutChunks threads
			for(int i = 0;i < numChunks;i++){
				int numBytesRead = 0;
				//Special case 'Last Chunk'
				if(i == numChunks-1){
					chunk = new byte[lastChunkSize];
					//Only reads if the size of the last Chunk is not 0, special case from multiples of 64000 in the file sizes
					if(lastChunkSize != 0){
						numBytesRead = in.read(chunk, 0, lastChunkSize);
					}
				}else numBytesRead = in.read(chunk, 0, MAX_SIZE_CHUNK);

				//BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				for(Socket s:availableConnections){
					DataOutputStream outToServer = new DataOutputStream(s.getOutputStream());
					//sentence = inFromUser.readLine();
					outToServer.writeBytes(new String("PUTCHUNK" + Utils.Space
		                    + protocolVersion + Utils.Space
		                    + fileID + Utils.Space
		                    + i + Utils.Space
		                    + numBytesRead + Utils.Space
		                    + Utils.CRLF));
					outToServer.write(chunk, 0, numBytesRead);
					outToServer.flush();
					//modifiedSentence = inFromServer.readLine();
					//System.out.println("FROM SERVER: " + modifiedSentence);
				}

                int sizeEncrypted = (numbytesRead / 16 + 1) * 16;// https://stackoverflow.com/questions/3283787/size-of-data-after-aes-cbc-and-aes-ecb-encryption


                byte[] chunkEncrypted = new byte[sizeEncrypted];

                // chunkEncrypted =

				/*Thread putChunkThread = new Thread(new PutChunk(
						protocolVersion,
						fileID,
						i,
						replicationDeg,
						chunk
						));
				putChunkThread.start();*/
				/*try {
					putChunkThread.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
			}

			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private ArrayList<Socket> getAvailablePeers(String fileID) {
		ArrayList<Socket> result = new ArrayList<Socket>();

		try {
			byte[] availableMsg = new String("AVAILABLE?" + Utils.Space
					+ "1.0" + Utils.Space
					+ fileID + Utils.Space
					+ Peer.node.getSimpleURL().toString()
					+ Utils.CRLF + Utils.CRLF).getBytes();

			Set<Serializable> availablePeers = Peer.node.getChord().retrieve(new Key("AVAILABLE"));

			ServerSocket ss = new ServerSocket(Peer.node.getPort());

			Thread t = new Thread(){
				public void run() {
					try {
						while(true){
							Socket tmpConnection = ss.accept();
							result.add(tmpConnection);
							System.out.println("ACEITEI CONEX�O TCP DE:" + tmpConnection.getRemoteSocketAddress());
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
		//RD
		//InetAddress IPAddress = InetAddress.getByName(Peer.mdbAddress);
		//DatagramPacket sendPacket = new DatagramPacket(header, header.length, IPAddress, Peer.mdbPort);

		/*for (int i = 1; i <= 5; i++) {
            Peer.node.udpSocket.send(sendPacket);
            Thread.sleep(400 * i);
            rds = Peer.rdMap.get(this.fileID + Utils.FS + this.chunkNo);
            if (rds != null && rds[0] <= rds[1]) {
                break;
            }
        }*/
	}
}