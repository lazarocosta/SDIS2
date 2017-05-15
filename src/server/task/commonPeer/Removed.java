package server.task.commonPeer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import server.main.Peer;
import server.task.initiatorPeer.PutChunk;
import utils.Utils;

//RD
public class Removed implements Runnable {

	private String protocolVersion;
	private String senderID;
	private String fileID;
	private int chunkNo;

	public Removed(String protocolVersion, String senderID, String fileID, int chunkNo) {
		this.protocolVersion = protocolVersion;
		this.senderID = senderID;
		this.fileID = fileID;
		this.chunkNo = chunkNo;
	}

	@Override
	public void run() {
		int[] rds = Peer.rdMap.get(this.fileID + Utils.FS + this.chunkNo);
		if(rds != null){
			Peer.rdMap.put(this.fileID + Utils.FS + this.chunkNo, new int[]{rds[0], rds[1]-1});
			ArrayList<String> detailed = Peer.rdDetailedMap.get(this.fileID + Utils.FS + this.chunkNo);
			if(detailed != null && !detailed.contains(this.senderID)){
				detailed.remove(this.senderID);
				Peer.rdDetailedMap.put(this.fileID + Utils.FS + this.chunkNo, detailed);
			}
			if(rds[1]-1 < rds[0]  && !this.senderID.equals(Peer.serverID)){
				File f = new File(Peer.dataPath + Utils.FS + this.fileID + Utils.FS + this.chunkNo);
				if (f.exists() && !f.isDirectory()) {
					InputStream in;
					try {
						in = new FileInputStream(f);
						byte chunk[] = new byte[(int) f.length()];
						in.read(chunk, 0,(int) f.length());
						new Thread(new PutChunk(
								this.protocolVersion,
								this.fileID,
								this.chunkNo,
								rds[0],
								chunk
								)).start();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

}
