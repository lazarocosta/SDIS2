package server.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import server.task.initiatorPeer.PutChunk;
import utils.Utils;

public class RDChecker implements Runnable {
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            if (Peer.checkRD == true) {
                File dir = new File(Peer.dataPath);
                String[] fileIds = dir.list();
                for (String fileID : fileIds) {
                    File fileDir = new File(Peer.dataPath + Utils.FS + fileID);
                    String[] chunks = fileDir.list();
                    for (String chunkNumber : chunks) {
                        int rds[] = Peer.rdMap.get(fileID + Utils.FS + chunkNumber);
                        if (rds[1] < rds[0]) {
                            File f = new File(Peer.dataPath + Utils.FS + fileID + Utils.FS + chunkNumber);
                            FileInputStream in;
                            try {
                                in = new FileInputStream(f);
                                byte chunk[] = new byte[(int) f.length()];
                                in.read(chunk, 0, (int) f.length());
                                new Thread(new PutChunk(
                                        "2.0",
                                        fileID,
                                        Integer.parseInt(chunkNumber),
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
                Peer.checkRD = false;
            }
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
