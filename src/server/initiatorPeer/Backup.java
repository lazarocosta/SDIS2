package server.initiatorPeer;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Set;

import de.uniba.wiai.lspi.chord.console.command.entry.Key;
import server.Peer;
import utils.SimpleURL;
import utils.Utils;

public class Backup {
    public int MAX_SIZE_CHUNK = 64 * 1000;


    public Backup(String protocolVersion, String fileID, String filePath, int replicationDeg) {


        File f = new File(filePath);
        long fileLength = f.length();
        byte[] chunk = new byte[MAX_SIZE_CHUNK];

        ArrayList<Socket> availableConnections = getAvailablePeers(fileID);

        try {
            InputStream in = new FileInputStream(f);

            //Number of Chunks needed for the file
            int numChunks = (int) (fileLength / MAX_SIZE_CHUNK) + 1;
            //Size of the last chunk
            int lastChunkSize = (int) (fileLength % MAX_SIZE_CHUNK);

            //Create chunks and call PutChunks threads
            for (int i = 0; i < numChunks; i++) {
                int numBytesRead = 0;
                //Special case 'Last Chunk'
                if (i == numChunks - 1) {
                    chunk = new byte[lastChunkSize];
                    //Only reads if the size of the last Chunk is not 0, special case from multiples of 64000 in the file sizes
                    if (lastChunkSize != 0) {
                        numBytesRead = in.read(chunk, 0, lastChunkSize);
                    }
                } else numBytesRead = in.read(chunk, 0, MAX_SIZE_CHUNK);

                for (Socket s : availableConnections) {
                    DataOutputStream outToServer = new DataOutputStream(s.getOutputStream());
                    outToServer.writeBytes(new String("PUTCHUNK" + Utils.Space
                            + protocolVersion + Utils.Space
                            + fileID + Utils.Space
                            + i + Utils.Space
                            + numBytesRead + Utils.Space
                            + Utils.CRLF));
                    outToServer.write(chunk, 0, numBytesRead);
                    outToServer.flush();
                }

                //TODO
                //NAO APAGAR, ENCRIPTACAO
                //HybridEncryption encrypted = new HybridEncryption();
                //int sizeEncrypted = (numBytesRead / 16 + 1) * 16;// https://stackoverflow.com/questions/3283787/size-of-data-after-aes-cbc-and-aes-ecb-encryption
                //byte[] chunkEncrypted = new byte[sizeEncrypted];
                //chunkEncrypted = encrypted.encrypt(chunk);
                //encrypted.encryptedSymmetricKey();
                //encrypted.saveKeysFile();
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
			byte[] availableMsg = new String("PUTCHUNK?" + Utils.Space
					+ "1.0" + Utils.Space
					+ fileID + Utils.Space
					+ Peer.simpleURL.toString() + Utils.Space
					+ Utils.CRLF + Utils.CRLF).getBytes();

			Set<Serializable> availablePeers = Peer.chord.retrieve(new Key("AVAILABLE"));

			//TODO criacao de serversocket
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
                for (Serializable peer : availablePeers) {
                    if (!Peer.simpleURL.equals(peer)) {
                        InetAddress IPAddress = InetAddress.getByName(((SimpleURL) peer).getIpAddress());
                        DatagramPacket sendPacket = new DatagramPacket(availableMsg, availableMsg.length, IPAddress, ((SimpleURL) peer).getPort());
                        Peer.udpSocket.send(sendPacket);
                    }
                }
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}