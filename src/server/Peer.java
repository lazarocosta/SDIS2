package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;

import database.MyConnection;
import database.UsersKeys;
import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.bitlet.weupnp.PortMappingEntry;
import org.xml.sax.SAXException;

import database.Files;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.PropertiesLoader;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;
import security.HybridEncryption;
import server.commonPeer.Listener;
import utils.SimpleURL;
import utils.StringKey;
import utils.Utils;

public class Peer {

    public static Connection connection;
    public static String email = null;
    public static boolean local_connection = false;
    public static DatagramSocket udpSocket;
    public static boolean port_forwarded = false;
    public static String IPAddress = null;
    public static int port = 60000;
    public static GatewayDevice activeGW = new GatewayDevice();
    public static ChordImpl chord = null;//new ChordImpl();
    public static Listener listener;
    public static Thread listenerThread;
    public static SimpleURL simpleURL;
    public static SimpleURL dhtURL;
    public static int id;

    public static String protocolVersion = "1.0";
    public static String path;
    public static String dataPath;
    public static long capacity = 0; //Capacity in bytes
    public static long usedCapacity = 0; //Used space in bytes
    public static HybridEncryption hybridEncryption;

    public static void initialize() {
        path = ".";
        dataPath = path + Utils.FS + "data";
        Utils.initFileSystem();
        usedCapacity = Utils.getusedCapacity();

        System.out.println("Starting services...");

        System.out.println("Services running...");

        System.out.println("Running configurations:");
        System.out.println("Data Path: " + dataPath);
        System.out.println("Max capacity: " + 0);
    }

    public static void startListening() {
        listener = new Listener();
        listenerThread = new Thread(listener);
        listenerThread.start();
    }

    public static void safeClose() {
        listenerThread.interrupt();
        udpSocket.close();
        chord.remove(new StringKey("AVAILABLE"), simpleURL);
        chord.leave();
        if (!local_connection) {
            try {
                activeGW.deletePortMapping(port, "TCP");

                activeGW.deletePortMapping(port, "UDP");
            } catch (IOException | SAXException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }


    public static boolean initializeIPAddressesAndPorts(boolean localConnection) {
        local_connection = localConnection;

        //LOCAL CONNECTION
        if (localConnection) {

            Enumeration<NetworkInterface> n;
            try {
                n = NetworkInterface.getNetworkInterfaces();

                ArrayList<String> choicestmp = new ArrayList<String>();
                for (; n.hasMoreElements(); ) {
                    NetworkInterface e = n.nextElement();

                    Enumeration<InetAddress> a = e.getInetAddresses();
                    for (; a.hasMoreElements(); ) {
                        InetAddress addr = a.nextElement();
                        if (addr instanceof Inet4Address)
                            choicestmp.add(addr.getHostAddress());
                        System.out.println("  " + addr.getHostAddress());
                    }
                }

                // JOptionPane
                String[] choices = new String[choicestmp.size()];
                choicestmp.toArray(choices);
                Object selected = JOptionPane.showInputDialog(
                        null
                        , "Which network interface do you want to use?"         // Message
                        , "Network Interface"                                    // Title
                        , JOptionPane.PLAIN_MESSAGE
                        , null                                                    // Icon
                        , choices // Array of options
                        , choices[0]    // Default option
                );
                if (selected != null) {
                    IPAddress = (String) selected;
                    simpleURL = new SimpleURL(IPAddress, port);
                    dhtURL = new SimpleURL(IPAddress, port + 1);
                    udpSocket = new DatagramSocket(port);
                    return true;
                } else {
                    return false;
                }
            } catch (SocketException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            //PORT FORWARDING
            try {

                System.out.println("Starting weupnp");

                GatewayDiscover gatewayDiscover = new GatewayDiscover();
                System.out.println("Looking for Gateway Devices...");

                Map<InetAddress, GatewayDevice> gateways = gatewayDiscover.discover();

                if (gateways.isEmpty()) {
                    System.out.println("No gateways found");
                    System.out.println("Stopping weupnp");
                    //NO GATEWAY, FIND EXTERNAL IP
                    java.net.URL whatismyip = new java.net.URL("http://checkip.amazonaws.com");
                    BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));

                    Peer.IPAddress = in.readLine();
                    System.out.println(IPAddress);
                    simpleURL = new SimpleURL(IPAddress, port);
                    udpSocket = new DatagramSocket(port);
                    dhtURL = new SimpleURL(IPAddress, port + 1);
                    return true;
                }
                System.out.println(gateways.size() + " gateway(s) found\n");

                int counter = 0;
                for (GatewayDevice gw : gateways.values()) {
                    counter++;
                    System.out.println("Listing gateway details of device #" + counter +
                            "\n\tFriendly name: " + gw.getFriendlyName() +
                            "\n\tPresentation URL: " + gw.getPresentationURL() +
                            "\n\tModel name: " + gw.getModelName() +
                            "\n\tModel number: " + gw.getModelNumber() +
                            "\n\tLocal interface address: " + gw.getLocalAddress().getHostAddress() + "\n");
                }

                // choose the first active gateway for the tests
                Peer.activeGW = gatewayDiscover.getValidGateway();

                Peer.IPAddress = Peer.activeGW.getExternalIPAddress();
                simpleURL = new SimpleURL(IPAddress, port);
                dhtURL = new SimpleURL(IPAddress, port + 1);
                udpSocket = new DatagramSocket(port);

                if (null != Peer.activeGW) {
                    System.out.println("Using gateway: " + Peer.activeGW.getFriendlyName());
                } else {
                    System.out.println("No active gateway device found");
                    System.out.println("Stopping weupnp");
                    return true;
                }


                // testing PortMappingNumberOfEntries
                Integer portMapCount = Peer.activeGW.getPortMappingNumberOfEntries();
                System.out.println("GetPortMappingNumberOfEntries: " + (portMapCount != null ? portMapCount.toString() : "(unsupported)"));

                // testing getGenericPortMappingEntry
                PortMappingEntry portMapping = new PortMappingEntry();
                if (Peer.activeGW.getGenericPortMappingEntry(0, portMapping))
                    System.out.println("Portmapping #0 successfully retrieved (" + portMapping.getPortMappingDescription() + ":" + portMapping.getExternalPort() + ")");
                else
                    System.out.println("Portmapping #0 retrival failed");

                InetAddress localAddress = Peer.activeGW.getLocalAddress();
                System.out.println("Using local address: " + localAddress.getHostAddress());
                String externalIPAddress = Peer.activeGW.getExternalIPAddress();
                System.out.println("External address: " + externalIPAddress);

                System.out.println("Querying device to see if a port mapping already exists for port " + Peer.port);


                if (Peer.activeGW.getSpecificPortMappingEntry(Peer.port, "UDP", portMapping)) {
                    System.out.println("Port " + Peer.port + " is already mapped. Aborting test.");
                    if (Peer.activeGW.getSpecificPortMappingEntry(Peer.port, "TCP", portMapping)) {
                        System.out.println("Port " + Peer.port + " is already mapped. Aborting test.");
                    }
                }

                System.out.println("Mapping free. Sending port mapping request for port " + Peer.port);

                // test static lease duration mapping
                if (Peer.activeGW.addPortMapping(Peer.port, Peer.port, localAddress.getHostAddress(), "UDP", "P2P Cloud (UDP)")) {
                    System.out.println("Mapping UDP SUCCESSFUL.");

                    if (Peer.activeGW.addPortMapping(Peer.port, Peer.port, localAddress.getHostAddress(), "TCP", "P2P Cloud (TCP)")) {
                        System.out.println("Mapping TCP SUCCESSFUL.");
                        port_forwarded = true;
                    }
                }
                return true;
            } catch (IOException | SAXException | ParserConfigurationException e) {
                e.printStackTrace();
                return false;
            }
        }

    }

    /**
     * Joins or create a new chord network
     * Announces it availability by inserting AVALABLE key
     *
     * @param bootstrap URL of existing network, null to create a new network
     */
    public static boolean joinChordNetwork(String bootstrap) {
        //load chord properties if not loaded
        try {
            PropertiesLoader.loadPropertyFile();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        String protocol = URL.KNOWN_PROTOCOLS.get(URL.SOCKET_PROTOCOL);
        URL localURL = null;
        try {
            localURL = new URL(protocol + "://" + dhtURL.toString() + "/");
        } catch (IOException e) {
            return false;
            //throw new RuntimeException (e);
        }
        if (bootstrap != null) {
            URL bootstrapURL = null;
            try {
                bootstrapURL = new URL(protocol + "://" + bootstrap + "/");
            } catch (MalformedURLException e) {
                //throw new RuntimeException (e);
                return false;

            }
            chord = new ChordImpl();
            try {
                chord.join(localURL, bootstrapURL);
                chord.insert(new StringKey("AVAILABLE"), simpleURL);
                return true;
            } catch (ServiceException e) {
                e.printStackTrace();
                try {
                    chord.join(localURL, bootstrapURL);
                    chord.insert(new StringKey("AVAILABLE"), simpleURL);
                    return true;
                } catch (ServiceException e1) {
                    e1.printStackTrace();
                    try {
                        chord.join(localURL, bootstrapURL);
                        chord.insert(new StringKey("AVAILABLE"), simpleURL);
                        return true;
                    } catch (ServiceException e2) {
                        e2.printStackTrace();
                        return false;
                    }
                }
                //throw new RuntimeException("Could not join DHT!", e);
            }
        } else {
            chord = new ChordImpl();
            try {
                chord.create(localURL);
                //chord.insertAsync(new Key("AVAILABLE"), simpleURL);
                return true;
            } catch (ServiceException e) {
                return false;
                //throw new RuntimeException("Could not create DHT!", e);
            }
        }
    }

    /**
     * Removes deleted files (obtained from db) from file system
     */
    public static void updateFileSystem() {
        File dir = new File(dataPath);

        String[] fileIDs = dir.list();
        HashSet<Integer> deletedFiles;
        try {
            deletedFiles = Files.getDeletedFiles(connection);
            if (deletedFiles.size() > 0) {
                for (String fileID : fileIDs) {
                    if (deletedFiles.contains(Integer.parseInt(fileID))) {
                        File fileDir = new File(dataPath + Utils.FS + fileID);
                        for (File c : fileDir.listFiles())
                            try {
                                //Delete files inside directory
                                Peer.usedCapacity -= c.length();
                                java.nio.file.Files.delete(c.toPath());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        try {
                            //Delete directory when it is empty
                            java.nio.file.Files.delete(fileDir.toPath());
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Announce in chord network files with at least one chunk in its system
     */
    public static void insertMyFiles() {
        File dir = new File(dataPath);
        String[] fileIDs = dir.list();
        for (String fileID : fileIDs) {
            System.out.println(fileID);
            chord.insertAsync(new StringKey(fileID), simpleURL);
        }

    }

    /**
     * Opens a hole in NAT to be able to receive udp messages from available peers in chord network
     *
     * @return
     */
    public static void udpHolePunch() {
        Set<Serializable> paulo = Peer.chord.retrieve(new StringKey("AVAILABLE"));
        byte[] b2 = "Hello".getBytes();
        byte[] b1 = new byte[6];
        System.arraycopy(b2, 0, b1, 0, b2.length);

        for (Serializable s : paulo) {
            DatagramPacket packet;
            try {
                packet = new DatagramPacket(b1, b1.length, InetAddress.getByName(((SimpleURL) s).getIpAddress()), ((SimpleURL) s).getPort());
                udpSocket.send(packet);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}

