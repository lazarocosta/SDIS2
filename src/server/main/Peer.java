package server.main;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import org.bitlet.weupnp.GatewayDevice;
import org.xml.sax.SAXException;

import de.uniba.wiai.lspi.chord.service.Chord;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;
import server.protocol.ClientInterface;
import utils.Utils;

public class Peer {

    public static String email = null;
    public static int port = 60000;
    public static GatewayDevice activeGW = new GatewayDevice();
    public static Chord chord = null;//new ChordImpl();

    public static String protocolVersion = "1.0";
    public static String serverID = "1";
    public static String mcAddress = "224.0.0.1";
    public static int mcPort = 9001;
    public static String mdbAddress = "224.0.0.2";
    public static int mdbPort = 9002;
    public static String mdrAddress = "224.0.0.3";
    public static int mdrPort = 9003;
    public static String remoteObject = "peer" + serverID;
    public static String path;
    public static String dataPath;
    public static String rdFile;
    public static String mdFile;
    public static long capacity = 0; //Capacity in bytes
    public static long usedCapacity = 0; //Used space in bytes
    public static boolean saveRD = false;
    public static boolean checkRD = false;
    public static ConcurrentHashMap<String, String> mdMap = new ConcurrentHashMap<String, String>();
    public static ConcurrentHashMap<String, int[]> rdMap = new ConcurrentHashMap<String, int[]>();
    public static ConcurrentHashMap<String, ArrayList<String>> rdDetailedMap = new ConcurrentHashMap<String, ArrayList<String>>();
    public static HashSet<String> deletedFiles = new HashSet<String>();

    /**
     * Main service starter
     *
     * @param args Server arguments by the following order:
     *             1 - Protocol Version
     *             2 - Server ID
     *             3 - RMI Remote Object name
     *             4 - MC Address
     *             5 - MC Port
     *             6 - MDB Address
     *             7 - MDB Port
     *             8 - MDR Address
     *             9 - MDR Port
     */
    public static void main(String[] args) {
        if (args.length != 9) {
            System.out.println("Usage: Peer <Protocol Version> <Server ID> <RMI Remote Object Name> <MC Address> <MC Port> <MDB Address> <MDB Port> <MDR Address> <MDR Port>");
            return;
        }

        protocolVersion = args[0];
        if (protocolVersion == null) {
            System.out.println("Null protocol version");
        }

        serverID = args[1];
        if (serverID == null) {
            System.out.println("Server ID wrong!");
            return;
        }

        remoteObject = args[2];
        if (remoteObject == null) {
            System.out.println("Remote Object Name wrong!");
            return;
        }

        mcAddress = args[3];
        if (mcAddress == null) {
            System.out.println("MC Address wrong!");
            return;
        }

        mcPort = Integer.parseInt(args[4]);
        if (mcPort < 1024) {
            System.out.println("MC Port wrong!");
            return;
        }
        mdbAddress = args[5];
        if (mdbAddress == null) {
            System.out.println("MDB Address wrong!");
            return;
        }

        mdbPort = Integer.parseInt(args[6]);
        if (mdbPort < 1024) {
            System.out.println("MDB Port wrong!");
            return;
        }

        mdrAddress = args[7];
        if (mdrAddress == null) {
            System.out.println("MDR Address wrong!");
            return;
        }

        mdrPort = Integer.parseInt(args[8]);
        if (mdrPort < 1024) {
            System.out.println("MDR Port wrong!");
            return;
        }

        path = "." + Utils.FS + serverID;
        dataPath = path + Utils.FS + "data";
        rdFile = path + Utils.FS + "rd";
        mdFile = path + Utils.FS + "md";

        System.out.println("Loading resources...");
        Utils.initFileSystem();
        Utils.loadRD();
        Utils.loadMD();
        usedCapacity = Utils.getusedCapacity();
        //CheckState cs = new CheckState();
        //System.out.println(cs.getState());

        System.out.println("Starting services...");

        MCListener mcListener = new MCListener();
        MDBListener mdbListener = new MDBListener();
        RDwriter rdWriter = new RDwriter();

        Thread mcThread = new Thread(mcListener);
        Thread mdbThread = new Thread(mdbListener);
        Thread rdWriterThread = new Thread(rdWriter);
        try {
            // Bind the remote object's stub in the registry
            ClientAppListener clientAppListener = new ClientAppListener();

            ClientInterface stub = (ClientInterface) UnicastRemoteObject.exportObject(clientAppListener, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(remoteObject, stub);
        } catch (RemoteException | AlreadyBoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (protocolVersion.equals("2.0")) {
            RDChecker rdChecker = new RDChecker();
            Thread rdCheckerThread = new Thread(rdChecker);
            rdCheckerThread.start();
        }

        mcThread.start();
        mdbThread.start();
        rdWriterThread.start();

        System.out.println("Services running...");

        System.out.println("Running configurations:");
        System.out.println("Server ID: " + serverID);
        System.out.println("MC Multicast Channel: " + mcAddress + ":" + mcPort);
        System.out.println("MDB Multicast Channel: " + mdbAddress + ":" + mdbPort);
        System.out.println("MDR Multicast Channel: " + mdrAddress + ":" + mdrPort);
        System.out.println("Data Path: " + dataPath);
        System.out.println("Max capacity: " + 0);
        System.out.println("Remote Object Name: " + remoteObject);
    }

    public static void safeClose() {
        try {
            activeGW.deletePortMapping(port, "TCP");
            activeGW.deletePortMapping(port, "UDP");
            Peer.chord.leave();
        } catch (IOException | SAXException | ServiceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
