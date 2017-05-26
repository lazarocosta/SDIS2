package security;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

public class TLS {

    public static SSLContext sslContext = null;

    public static SSLContext createSSLContext(){

        try {
            //configure SSL
            char[] password = "123456".toCharArray();
            KeyStore ks = KeyStore.getInstance("JKS");
            KeyStore ts = KeyStore.getInstance("JKS");

            File keyFile = new File("./certificates/server.keys");
            ks.load(new FileInputStream(keyFile), password);

            File trustStoreFile = new File("./certificates/truststore");
            ts.load(new FileInputStream(trustStoreFile), password);

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, password);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ts);

            SSLContext sslContext = SSLContext.getInstance("TLS");  // create SSL context
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            TLS.sslContext =  sslContext;

            return sslContext;

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
            return null;
        }

    }

    public static ServerSocket createServerSocket(int port){

        if(TLS.sslContext == null){
            createSSLContext();
        }

        try {
            return TLS.sslContext.getServerSocketFactory().createServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static Socket createClientSocket(InetAddress host, int port){

        if(TLS.sslContext == null){
            createSSLContext();
        }

        try {
            return TLS.sslContext.getSocketFactory().createSocket(host, port);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    // For testing
    public static void main(String[] args) {


    }
}
