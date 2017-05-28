package server.commonPeer;

import java.io.IOException;
import java.net.DatagramPacket;

import server.Peer;
import utils.SimpleURL;

public class Listener implements Runnable {

    @Override
    public void run() {
        try {
            byte[] buf = new byte[70000];
            DatagramPacket receivedCmd = new DatagramPacket(buf, buf.length);
            while (!Thread.currentThread().isInterrupted()) {
                Peer.udpSocket.receive(receivedCmd);
                String cmdSplit[] = new String(receivedCmd.getData(), receivedCmd.getOffset(), receivedCmd.getLength()).split("\\s+");
                if (cmdSplit[1].equals("1.0") || cmdSplit[1].equals(Peer.protocolVersion)) { //Always accept messages with version 1.0 but only accepts with version 2.0 if the running protocolVersion is also 2.0
                    System.out.println(cmdSplit[0]);
                    if (cmdSplit[0].equals("DELETE")) {
                        new Thread(new Delete(
                                cmdSplit[1],
                                cmdSplit[3]
                        )).start();
                    } else if (cmdSplit[0].equals("PUTCHUNK?")) {
                        System.out.println("RECEBI PEDIDO CONEXAO TCP DE:" + cmdSplit[2]);
                        //TODO confirmar se pode mesmo se ligar para receber
                        //Passar para outra funcao
                        SimpleURL url = new SimpleURL(cmdSplit[3]);
                        new Thread(new Backup(
                                cmdSplit[1], //Version
                                cmdSplit[2],
                                url.getIpAddress(),
                                url.getPort()
                        )).start();
                    } else if (cmdSplit[0].equals("GETCHUNK?")) {
                        System.out.println("RECEBI PEDIDO CONEXAO TCP DE:" + cmdSplit[2]);
                        //TODO confirmar se pode mesmo se ligar para receber
                        //Passar para outra funcao
                        SimpleURL url = new SimpleURL(cmdSplit[3]);
                        new Thread(new Restore(
                                cmdSplit[1], //Version
                                cmdSplit[2],
                                url.getIpAddress(),
                                url.getPort()
                        )).start();
                    }
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}