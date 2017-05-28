package server.commonPeer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;

import server.Peer;
import utils.Utils;

public class Restore implements Runnable {

	private String protocolVersion;
	private String fileID;
	private String ipAddress;
	private int port;

	public Restore(String protocolVersion, String fileID, String ipAddress, int port) {
		this.protocolVersion = protocolVersion;
		this.fileID = fileID;
		this.ipAddress = ipAddress;
		this.port = port;
	}

	@Override
	public void run() {
		File dir = new File(Peer.dataPath + Utils.FS + this.fileID);
		dir.mkdirs();

		Socket s = null;
		try {
			// TODO criacao de socket
			s = new Socket(InetAddress.getByName(this.ipAddress), this.port);
			while (true) {
				DataInputStream dis = new DataInputStream(s.getInputStream());
				DataOutputStream outToServer = new DataOutputStream(s.getOutputStream());

				@SuppressWarnings("deprecation")
				String header = dis.readLine();
				System.out.println("Received: " + header);
				String cmdSplit[] = new String(header).split("\\s+");
				if (cmdSplit[0].equals("GETCHUNKS")) {
					File fileDir = new File(Peer.dataPath + Utils.FS + this.fileID);
					String[] chunks = fileDir.list();
					outToServer.writeBytes(String.join(" ", chunks) + Utils.CRLF);
					outToServer.flush();
				} else if (cmdSplit[0].equals("GETCHUNK")) {
					int chunkNumber = Integer.parseInt(cmdSplit[3]);
					File chunk_file = new File(Peer.dataPath + Utils.FS + this.fileID + Utils.FS + chunkNumber);
					byte[] chunk = Files.readAllBytes(chunk_file.toPath());

					outToServer.writeBytes(new String("CHUNK" + Utils.Space 
							+ protocolVersion + Utils.Space 
							+ fileID + Utils.Space 
							+ chunkNumber + Utils.Space 
							+ chunk.length + Utils.Space 
							+ Utils.CRLF));
					outToServer.write(chunk, 0, chunk.length);
					outToServer.flush();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				s.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
}
