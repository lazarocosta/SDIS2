package server.protocol;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import server.main.Peer;
import server.task.initiatorPeer.GetChunk;

public class Restore {

	public Restore(String protocolVersion, String filePath){
		int chunkNumber = 0;
		String fileId = Peer.mdMap.get(filePath);
		OutputStream output = null;

		if(fileId != null){
			try {
				output = new BufferedOutputStream(new FileOutputStream(filePath));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			byte[] chunk = null;
			do{
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
				if(chunk != null){
					try {
						output.write(chunk, 0, chunk.length);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else{
					break;
				}

				chunkNumber++;
			}while(chunk.length == 64000);
			try {
				output.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
