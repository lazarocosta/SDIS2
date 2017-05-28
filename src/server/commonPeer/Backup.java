package server.commonPeer;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;

import de.uniba.wiai.lspi.chord.console.command.entry.Key;
import server.Peer;
import utils.Utils;

public class Backup implements Runnable {

	@SuppressWarnings("unused")
	private String protocolVersion;
	private String fileID;
	private String ipAddress;
	private int port;

	public Backup(String protocolVersion, String fileID, String ipAddress, int port) {
		this.protocolVersion = protocolVersion;
		this.fileID = fileID;
		this.ipAddress = ipAddress;
		this.port = port;
	}

	@Override
	public void run() {
		File dir = new File(Peer.dataPath + Utils.FS + this.fileID);
		dir.mkdirs();
		Peer.chord.insert(new Key(this.fileID), Peer.simpleURL.toString());
		try {
			// TODO criacao de socket
			Socket s = new Socket(InetAddress.getByName(this.ipAddress), this.port);
			while (true) {
				DataInputStream dis = new DataInputStream(s.getInputStream());
				@SuppressWarnings("deprecation")
				String header = dis.readLine();
				System.out.println("Received: " + header);
				String cmdSplit[] = new String(header).split("\\s+");
				int size = Integer.parseInt(cmdSplit[4]);
				byte[] body = new byte[size];
				dis.readFully(body);

				try {
					if (Peer.capacity == 0 || Peer.capacity - Peer.usedCapacity > body.length) {
						File f = new File(Peer.dataPath + Utils.FS + this.fileID + Utils.FS + cmdSplit[3]);
						f.createNewFile();
						Files.write(f.toPath(), body);
						Peer.usedCapacity += body.length;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

				System.out.println("Received body with size: " + body.length);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
