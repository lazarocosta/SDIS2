package server.protocol;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import server.main.Peer;
import utils.Utils;

public class CheckState {

    public String getState() {
        // TODO Auto-generated method stub
        String state = new String("");

        state += "---------------------------------------------------------------------------\n";
        state += "| BACKED UP FILES                                                         |\n";
        state += "---------------------------------------------------------------------------\n";

        Iterator it = Peer.mdMap.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry<String, String> pair = (HashMap.Entry) it.next();
            String FilePath = pair.getKey();
            String FileId = pair.getValue();
            state += "  Path  : " + FilePath + "\n";
            state += "  ID    : " + FileId + "\n";
            state += "  RD    : " + Peer.rdMap.get(FileId + Utils.FS + 0)[0] + "\n";
            state += "  Chunks:" + "\n";
            state += "    | ID      | RD |" + "\n";
            int chunkNo = 0;
            int[] rds = Peer.rdMap.get(FileId + Utils.FS + chunkNo);
            while (rds != null) {
                state += "    | " + String.format("%" + -8 + "s", chunkNo) +
                        "| " + String.format("%" + -3 + "s", rds[1]) +
                        "|\n";
                chunkNo++;
                rds = Peer.rdMap.get(FileId + Utils.FS + chunkNo);
            }
            state += "---------------------------------------------------------------------------\n";
        }
        state += "| STORED FILES                                                            |\n";
        state += "---------------------------------------------------------------------------\n";

        File dir = new File(Peer.dataPath);
        String[] fileIds = dir.list();
        for (String file : fileIds) {
            state += "  ID    : " + file + "\n";
            state += "  Chunks:" + "\n";
            state += "    | ID      | kB | RD |" + "\n";
            File fileDir = new File(Peer.dataPath + Utils.FS + file);
            String[] chunks = fileDir.list();
            for (String chunkNumber : chunks) {
                File chunk = new File(Peer.dataPath + Utils.FS + file + Utils.FS + chunkNumber);
                state += "    | " + String.format("%" + -8 + "s", chunkNumber) +
                        "| " + String.format("%" + -3 + "s", chunk.length() / 1000) +
                        "| " + String.format("%" + -3 + "s", Peer.rdMap.get(file + Utils.FS + chunkNumber)[1]) +
                        "|\n";
            }
        }
        state += "---------------------------------------------------------------------------\n";
        state += "| PEER STORAGE CAPACITY                                                   |\n";
        state += "---------------------------------------------------------------------------\n";
        state += "  Total capacity: ";
        if (Peer.capacity != 0) {
            state += (double) Peer.capacity / 1000 + "kB\n";
        } else state += "infinite\n";
        state += "  Used capacity : " + (double) Peer.usedCapacity / 1000 + "kB\n";
        state += "---------------------------------------------------------------------------\n";

        return state;
    }

}
