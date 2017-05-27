package server.protocol;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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

	public Restore(String protocolVersion, String fileID, String destDir) {
		int chunkNumber = 0;
		//TODO ver se o proprio peer nao tem nenhum chunk
		ArrayList<Socket> availableConnections = getAvailablePeers(fileID);

		for(Socket s:availableConnections){
			DataOutputStream outToServer = new DataOutputStream(s.getOutputStream());
			outToServer.writeBytes(new String("GETCHUNKS" + Utils.Space
					+ protocolVersion + Utils.Space
					+ fileID + Utils.Space
					+ Utils.CRLF));
		}
		
		HashMap<Socket, List<Integer>> availableChunks = new HashMap<Socket, List<Integer>>();
		
		for(Socket s:availableConnections){
			new Thread(){
				public void run() {
					try {
						while(true){
							DataInputStream dis = new DataInputStream(s.getInputStream());
							String header = dis.readLine();
							System.out.println("Received: " + header);
							List<Integer> chunks = Arrays.asList(Arrays.stream(new String(header).split("\\s+")).mapToInt(Integer::parseInt).toArray());
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}.start();
		}
		
		Thread.sleep(5000);

		try {
			InputStream in = new FileInputStream(f);

			//Number of Chunks needed for the file
			int numChunks = (int)(fileLength/64000)+1;
			//Size of the last chunk
			int lastChunkSize = (int)(fileLength%64000);

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
				}else numBytesRead = in.read(chunk, 0, 64000);

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
			byte[] availableMsg = new String("GETCHUNK?" + Utils.Space
					+ "1.0" + Utils.Space
					+ fileID + Utils.Space
					+ Peer.simpleURL.toString() + Utils.Space
					+ Utils.CRLF + Utils.CRLF).getBytes();

			Set<Serializable> availablePeers = Peer.chord.retrieve(new Key(fileID));

			ServerSocket ss = new ServerSocket(Peer.port);

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
					if(!Peer.simpleURL.equals(peer)){
						System.out.println("vou mandar");
						InetAddress IPAddress = InetAddress.getByName(((SimpleURL)peer).getIpAddress());
						DatagramPacket sendPacket = new DatagramPacket(availableMsg, availableMsg.length, IPAddress, ((SimpleURL)peer).getPort());
						Peer.udpSocket.send(sendPacket);
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
//String fileId = Peer.mdMap.get(filePath);
/*OutputStream output = null;

        if (fileId != null) {
            try {
                output = new BufferedOutputStream(new FileOutputStream(filePath));
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            byte[] chunk = null;
            do {
                GetChunk getChunk = new GetChunk(
                        protocolVersion,
                        fileId,
                        chunkNumber);
                Thread getChunkThread = new Thread(getChunk);
                getChunkThread.start();

                try {
                    getChunkThread.join();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    break;
                }
                chunk = getChunk.getChunk();
                if (chunk != null) {
                    try {
                        output.write(chunk, 0, chunk.length);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else {
                    break;
                }

                chunkNumber++;
            } while (chunk.length == 64000);
            try {
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}*/
