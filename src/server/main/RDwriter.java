package server.main;

import utils.Utils;

public class RDwriter implements Runnable {
    @Override
    public void run()
    {
        while(!Thread.currentThread().isInterrupted()) {
           if(Peer.saveRD == true){
             Utils.saveRD();
             Peer.saveRD = false;
           }
           try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        }
    }

}
