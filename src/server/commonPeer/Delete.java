package server.commonPeer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import de.uniba.wiai.lspi.chord.console.command.entry.Key;
import server.Peer;
import utils.Utils;

public class Delete implements Runnable {

	@SuppressWarnings("unused")
	private String protocolVersion;
	String fileID;

	public Delete(String protocolVersion, String fileID) {
		this.protocolVersion = protocolVersion;
		this.fileID = fileID;
	}

	@Override
	public void run() {
		File dir = new File(Peer.dataPath + Utils.FS + this.fileID);
		if (dir.isDirectory()) {
			for (File c : dir.listFiles())
				try {
					// Delete files inside directory
					Peer.usedCapacity -= c.length();
					Files.delete(c.toPath());
				} catch (IOException e) {
					e.printStackTrace();
				}
			try {
				// Delete directory when it is empty
				Files.delete(dir.toPath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Peer.chord.remove(new Key(this.fileID), Peer.IPAddress + ":" + Peer.port);
	}
}
