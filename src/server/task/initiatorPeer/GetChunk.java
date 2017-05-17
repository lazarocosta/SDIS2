package server.task.initiatorPeer;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

import server.main.Peer;
import utils.Utils;

public class GetChunk implements Runnable {

    private String protocolVersion;
    private String fileID;
    private int chunkNumber;
    private byte[] chunk;

    public GetChunk(String protocolVersion, String fileID, int chunkNumber) {
        this.protocolVersion = protocolVersion;
        this.fileID = fileID;
        this.chunkNumber = chunkNumber;
        this.setChunk(null);
    }

    @Override
    public void run() {
        //GETCHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
        byte[] msg = new String("GETCHUNK" + Utils.Space +
                this.protocolVersion + Utils.Space +
                Peer.serverID + Utils.Space +
                this.fileID + Utils.Space +
                this.chunkNumber + Utils.Space +
                Utils.CRLF + Utils.CRLF).getBytes();
        try {
            //Group and socket creations
            InetAddress mcGroup = InetAddress.getByName(Peer.mcAddress);
            DatagramSocket mcSocket = new DatagramSocket();

            //Send GETCHUNK
            DatagramPacket sendCommand = new DatagramPacket(msg, msg.length, mcGroup, Peer.mcPort);
            mcSocket.send(sendCommand);
            mcSocket.close();

            //Call RECEIVECHUNK
            Thread receivedThread = new Thread(new ReceiveChunk());
            receivedThread.start();
            receivedThread.join(1000);
            if (receivedThread.isAlive()) receivedThread.interrupt();

        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public byte[] getChunk() {
        return chunk;
    }

    public void setChunk(byte[] chunk) {
        this.chunk = chunk;
    }

    class ReceiveChunk implements Runnable {

        @Override
        public void run() {
            try {
                if (protocolVersion.equals("1.0")) {
                    InetAddress mdrGroup = InetAddress.getByName(Peer.mdrAddress);
                    MulticastSocket mdrSocket = new MulticastSocket(Peer.mdrPort);
                    byte[] receiveMsg = new byte[70000];
                    DatagramPacket receiveCommand = new DatagramPacket(receiveMsg, receiveMsg.length);
                    mdrSocket.joinGroup(mdrGroup);
                    mdrSocket.setSoTimeout(1000);

                    while (!Thread.currentThread().isInterrupted()) {
                        mdrSocket.receive(receiveCommand);
                        String receivedCmdString = new String(receiveCommand.getData(), receiveCommand.getOffset(), receiveCommand.getLength());
                        String cmdSplit[] = receivedCmdString.split("\\s+");
                        if (cmdSplit[0].equals("CHUNK") && cmdSplit[3].equals(fileID) && cmdSplit[4].equals(Integer.toString(chunkNumber))) {
                            int bodyIndex = receivedCmdString.indexOf(Utils.CRLF + Utils.CRLF) + 4;
                            if (bodyIndex >= 0) {
                                setChunk(Arrays.copyOfRange(receiveCommand.getData(), bodyIndex, receiveCommand.getLength()));
                                break;
                            }
                        }
                    }
                    mdrSocket.close();
                } else {
                    String[] connection = Utils.getTCPfromSenderID(Peer.serverID);
                    System.out.println("IP: " + connection[0] + " , PORT: " + connection[1]);
                    ServerSocket serverSocket = new ServerSocket(Integer.parseInt(connection[1]));


                    while (!Thread.currentThread().isInterrupted()) {
                        Socket socket = serverSocket.accept();
                        InputStream in = socket.getInputStream();
                        DataInputStream dis = new DataInputStream(in);
                        int length = dis.readInt();
                        if (length > 0) {
                            System.out.println("VOU RECEBER MSG COM: " + length);
                            byte[] receiveMsg = new byte[length];
                            dis.readFully(receiveMsg, 0, receiveMsg.length);
                            String receivedCmdString = new String(receiveMsg, 0, length);
                            String cmdSplit[] = receivedCmdString.split("\\s+");
                            if (cmdSplit[0].equals("CHUNK") && cmdSplit[3].equals(fileID) && cmdSplit[4].equals(Integer.toString(chunkNumber))) {
                                int bodyIndex = receivedCmdString.indexOf(Utils.CRLF + Utils.CRLF) + 4;
                                if (bodyIndex >= 0) {
                                    setChunk(Arrays.copyOfRange(receiveMsg, bodyIndex, length));
                                    break;
                                }
                            }
                        }
                        socket.close();
                    }
                    serverSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
