package server.task.initiatorPeer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import server.main.Peer;
import utils.Utils;

public class Removed implements Runnable {

    String protocolVersion;
    String fileID;
    int chunkNo;

    public Removed(String protocolVersion, String fileID, int chunkNo) {
        this.protocolVersion = protocolVersion;
        this.fileID = fileID;
        this.chunkNo = chunkNo;
    }

    @Override
    public void run() {
        try {
            DatagramSocket clientSocket = new DatagramSocket();
            InetAddress IPAddress = InetAddress.getByName(Peer.mcAddress);
            byte[] sendData = new String(
                    "REMOVED" + Utils.Space +
                            protocolVersion + Utils.Space +
                            Peer.serverID + Utils.Space +
                            this.fileID + Utils.Space +
                            this.chunkNo + Utils.Space +
                            Utils.CRLF + Utils.CRLF).getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, Peer.mcPort);
            clientSocket.send(sendPacket);
            clientSocket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
