package server.protocol;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import server.main.Peer;
import server.task.initiatorPeer.Removed;
import utils.Utils;

public class Reclaim {

    public Reclaim(String protocolVersion, long diskSpace) {
        Peer.capacity = diskSpace * 1000;
        if (Peer.capacity != 0) {
            HashMap<String, int[]> sortedRDMap = Utils.sortMostReplicated();
            Iterator it = sortedRDMap.entrySet().iterator();
            while ((Peer.capacity <= Peer.usedCapacity) && (it.hasNext())) {
                HashMap.Entry<String, int[]> pair = (HashMap.Entry) it.next();
                File f = new File(Peer.dataPath + Utils.FS + pair.getKey());
                if (f.exists() && !f.isDirectory()) {
                    Peer.usedCapacity -= f.length();
                    f.delete();
                    String[] keySplit = pair.getKey().split(Utils.FS);
                    new Thread(new Removed(
                            protocolVersion,
                            keySplit[0],
                            Integer.parseInt(keySplit[1])
                    )).start();

                }
                it.remove();
            }
        }
    }

}
