package server.task.commonPeer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import de.uniba.wiai.lspi.chord.console.command.entry.Key;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import server.main.Peer;
import utils.Utils;

public class Delete implements Runnable {

    String senderID;
    String fileID;

    public Delete(String senderID, String fileID) {
        this.senderID = senderID;
        this.fileID = fileID;
    }

    @Override
    public void run() {
        File dir = new File(Peer.dataPath + Utils.FS + this.fileID);
        if (dir.isDirectory()) {
            for (File c : dir.listFiles())
                try {
                    //Delete files inside directory
                    Peer.usedCapacity -= c.length();
                    Files.delete(c.toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            try {
                //Delete directory when it is empty
                Files.delete(dir.toPath());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
			Peer.chord.remove(new Key(this.fileID), Peer.IPAddress+":"+Peer.port);
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        int chunkNo = 0;
        int[] rds = Peer.rdMap.get(this.fileID + Utils.FS + chunkNo);
        while (rds != null) {
            Peer.rdMap.remove(this.fileID + Utils.FS + chunkNo);
            Peer.rdDetailedMap.remove(this.fileID + Utils.FS + chunkNo);
            chunkNo++;
            rds = Peer.rdMap.get(this.fileID + Utils.FS + chunkNo);
        }
        Peer.saveRD = true;
    }
}
