package server.protocol;

import server.main.Peer;
import server.task.initiatorPeer.Delete;
import utils.Utils;

public class DeleteProtocol {
    //private static final int N_DELETES_TRIES = 5;

    public DeleteProtocol(String protocolVersion, String filePath) {
        //for(int i=0; i < N_DELETES_TRIES; i++){
        new Thread(new Delete(protocolVersion, Peer.mdMap.get(filePath))).start();
        //}
        Peer.mdMap.remove(filePath);
        Utils.writeMD();
    }

}
