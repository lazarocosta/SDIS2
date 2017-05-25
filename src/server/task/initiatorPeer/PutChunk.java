package server.task.initiatorPeer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import server.main.Peer;
import utils.Utils;

public class PutChunk implements Runnable {

    private String protocolVersion;
    private String fileID;
    private int chunkNo;
    private int replicationDegree;
    private byte[] body;

    public PutChunk(String protocolVersion, String fileID, int chunkNo, int replicationDegree, byte[] body) {
        this.protocolVersion = protocolVersion;
        this.fileID = fileID;
        this.chunkNo = chunkNo;
        this.replicationDegree = replicationDegree;
        this.body = body;
    }

    @Override
    public void run() {
        // TODO
        // Generate String with chunks and send it to the multicast channel
        try {
            //Prepare byte[] msg
            byte[] header = new String("PUTCHUNK" + Utils.Space
                    + this.protocolVersion + Utils.Space
                    + Peer.serverID + Utils.Space
                    + this.fileID + Utils.Space
                    + this.chunkNo + Utils.Space
                    + this.replicationDegree + Utils.Space
                    + Utils.CRLF + Utils.CRLF).getBytes();
            byte[] chunk = new byte[header.length + this.body.length];
            System.arraycopy(header, 0, chunk, 0, header.length);
            System.arraycopy(this.body, 0, chunk, header.length, this.body.length);

            //RD
            int rds[] = Peer.rdMap.get(this.fileID + Utils.FS + this.chunkNo);
            if (rds == null) {
                Peer.rdMap.put(this.fileID + Utils.FS + this.chunkNo, new int[]{this.replicationDegree, 0});
            } else {
                Peer.rdMap.put(this.fileID + Utils.FS + this.chunkNo, new int[]{this.replicationDegree, rds[1]});
            }
            DatagramSocket clientSocket = new DatagramSocket();
            InetAddress IPAddress = InetAddress.getByName(Peer.mdbAddress);
            DatagramPacket sendPacket = new DatagramPacket(chunk, chunk.length, IPAddress, Peer.mdbPort);

            for (int i = 1; i <= 5; i++) {
                clientSocket.send(sendPacket);
                Thread.sleep(400 * i);
                rds = Peer.rdMap.get(this.fileID + Utils.FS + this.chunkNo);
                if (rds != null && rds[0] <= rds[1]) {
                    break;
                }
            }
            clientSocket.close();

        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }


}
