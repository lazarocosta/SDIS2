package server.main;

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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;

import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.bitlet.weupnp.PortMappingEntry;
import org.xml.sax.SAXException;

import database.Files;
import de.uniba.wiai.lspi.chord.console.command.entry.Key;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.Chord;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;
import utils.SimpleURL;
import utils.Utils;

public class Peer{

	public static Peer node = null;
	public static Connection connection;
	private String email = null;
	private boolean local_connection = false;
	private DatagramSocket udpSocket;
	private boolean port_forwarded = false;
	private String IPAddress = null;
	private int port = 60000;
	private GatewayDevice activeGW = new GatewayDevice();
	private ChordImpl chord = null;//new ChordImpl();
	private Listener listener;
	private Thread listenerThread;
	private SimpleURL simpleURL;
	private SimpleURL dhtURL;

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
	public static ConcurrentHashMap<String,String> mdMap = new ConcurrentHashMap<String,String>();
	public static ConcurrentHashMap<String,int[]> rdMap = new ConcurrentHashMap<String,int[]>();
	public static ConcurrentHashMap<String,ArrayList<String>> rdDetailedMap = new ConcurrentHashMap<String,ArrayList<String>>();
	public static HashSet<String> deletedFiles = new HashSet<String>();

	public Peer(String email, int port) {
		this.email = email;
		this.setPort(port);
	}


	public void initialize(){		
		path = "." + Utils.FS + serverID;
		dataPath = path + Utils.FS + "data";
		Utils.initFileSystem();
		usedCapacity = Utils.getusedCapacity();

		System.out.println("Starting services...");

		
		/*try {
			// Bind the remote object's stub in the registry
			ClientAppListener clientAppListener = new ClientAppListener();

			ClientInterface stub = (ClientInterface) UnicastRemoteObject.exportObject(clientAppListener, 0);
			Registry registry = LocateRegistry.getRegistry();
			registry.bind(remoteObject, stub);
		} catch (RemoteException | AlreadyBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

		if(protocolVersion.equals("2.0")){
			RDChecker rdChecker = new RDChecker();
			Thread rdCheckerThread = new Thread(rdChecker);
			rdCheckerThread.start();
		}

		

		System.out.println("Services running...");

		System.out.println("Running configurations:");
		System.out.println("Server ID: " + serverID);
		System.out.println("Data Path: " + dataPath);
		System.out.println("Max capacity: " + 0);
		System.out.println("Remote Object Name: " + remoteObject);
	}

	public void startListening(){
		listener = new Listener();
		listenerThread = new Thread(listener);
		listenerThread.start();
	}

	public void safeClose(){
			listenerThread.interrupt();
			udpSocket.close();
			chord.remove(new Key("AVAILABLE"), simpleURL);
			chord.leave();
			if(!local_connection){
				try {
					activeGW.deletePortMapping(port,"TCP");
				
				activeGW.deletePortMapping(port,"UDP");
				} catch (IOException | SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	}


	public boolean initializeIPAddressesAndPorts(boolean localConnection) {
		local_connection = localConnection;

		//LOCAL CONNECTION
		if(localConnection){

			Enumeration<NetworkInterface> n;
			try {
				n = NetworkInterface.getNetworkInterfaces();

				ArrayList<String> choicestmp = new ArrayList<String>();
				for (; n.hasMoreElements();)
				{
					NetworkInterface e = n.nextElement();

					Enumeration<InetAddress> a = e.getInetAddresses();
					for (; a.hasMoreElements();)
					{
						InetAddress addr = a.nextElement();
						if(addr instanceof Inet4Address)
							choicestmp.add(addr.getHostAddress());
						System.out.println("  " + addr.getHostAddress());
					}
				}

				// JOptionPane
				String[] choices = new String[choicestmp.size()];
				choicestmp.toArray(choices);
				Object selected = JOptionPane.showInputDialog(
						null
						, "Which network interface do you want to use?"       	 // Message
						, "Network Interface" 									// Title
						, JOptionPane.PLAIN_MESSAGE                               
						, null 													// Icon
						, choices // Array of options
						, choices[0]    // Default option
						);
				if(selected != null){
					IPAddress = (String) selected;
					simpleURL = new SimpleURL(IPAddress, port);
					dhtURL = new SimpleURL(IPAddress, port + 1);
					udpSocket = new DatagramSocket(port);
					return true;
				}else{
					return false;
				}
			} catch (SocketException e) {
				e.printStackTrace();
				return false;
			}
		}else{
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

					Peer.node.setIPAddress(in.readLine());
					System.out.println(IPAddress);
					simpleURL = new SimpleURL(IPAddress, port);
					udpSocket = new DatagramSocket(port);
					dhtURL = new SimpleURL(IPAddress, port + 1);
					return true;
				}
				System.out.println(gateways.size()+" gateway(s) found\n");

				int counter=0;
				for (GatewayDevice gw: gateways.values()) {
					counter++;
					System.out.println("Listing gateway details of device #" + counter+
							"\n\tFriendly name: " + gw.getFriendlyName()+
							"\n\tPresentation URL: " + gw.getPresentationURL()+
							"\n\tModel name: " + gw.getModelName()+
							"\n\tModel number: " + gw.getModelNumber()+
							"\n\tLocal interface address: " + gw.getLocalAddress().getHostAddress()+"\n");
				}

				// choose the first active gateway for the tests
				Peer.node.setActiveGW(gatewayDiscover.getValidGateway());
				Peer.node.setIPAddress(Peer.node.getActiveGW().getExternalIPAddress());
				simpleURL = new SimpleURL(IPAddress, port);
				dhtURL = new SimpleURL(IPAddress, port + 1);
				udpSocket = new DatagramSocket(port);

				if (null != Peer.node.getActiveGW()) {
					System.out.println("Using gateway: " + Peer.node.getActiveGW().getFriendlyName());
				} else {
					System.out.println("No active gateway device found");
					System.out.println("Stopping weupnp");
					return true;
				}


				// testing PortMappingNumberOfEntries
				Integer portMapCount = Peer.node.getActiveGW().getPortMappingNumberOfEntries();
				System.out.println("GetPortMappingNumberOfEntries: " + (portMapCount!=null?portMapCount.toString():"(unsupported)"));

				// testing getGenericPortMappingEntry
				PortMappingEntry portMapping = new PortMappingEntry();
				if (Peer.node.getActiveGW().getGenericPortMappingEntry(0,portMapping))
					System.out.println("Portmapping #0 successfully retrieved ("+portMapping.getPortMappingDescription()+":"+portMapping.getExternalPort()+")");
				else
					System.out.println("Portmapping #0 retrival failed");

				InetAddress localAddress = Peer.node.getActiveGW().getLocalAddress();
				System.out.println("Using local address: "+ localAddress.getHostAddress());
				String externalIPAddress = Peer.node.getActiveGW().getExternalIPAddress();
				System.out.println("External address: "+ externalIPAddress);

				System.out.println("Querying device to see if a port mapping already exists for port "+ Peer.node.getPort());


				if (Peer.node.getActiveGW().getSpecificPortMappingEntry(Peer.node.getPort(),"UDP",portMapping)) {
					System.out.println("Port "+Peer.node.getPort()+" is already mapped. Aborting test.");
					if (Peer.node.getActiveGW().getSpecificPortMappingEntry(Peer.node.getPort(),"TCP",portMapping)) {
						System.out.println("Port "+Peer.node.getPort()+" is already mapped. Aborting test.");
					}
				}

				System.out.println("Mapping free. Sending port mapping request for port "+Peer.node.getPort());

				// test static lease duration mapping
				if (Peer.node.getActiveGW().addPortMapping(Peer.node.getPort(),Peer.node.getPort(),localAddress.getHostAddress(),"UDP","P2P Cloud (UDP)")) {
					System.out.println("Mapping UDP SUCCESSFUL.");

					if (Peer.node.getActiveGW().addPortMapping(Peer.node.getPort(),Peer.node.getPort(),localAddress.getHostAddress(),"TCP","P2P Cloud (TCP)")) {
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
	 * @param bootstrap URL of existing network, null to create a new network
	 */
	public void joinChordNetwork(String bootstrap){
		de.uniba.wiai.lspi.chord.service.PropertiesLoader.loadPropertyFile ();
		String protocol = URL.KNOWN_PROTOCOLS.get(URL.SOCKET_PROTOCOL);
		URL localURL = null;
		try {
			localURL = new URL (protocol + "://"+dhtURL.toString()+"/");
		} catch (IOException e){
			throw new RuntimeException (e);
		}
		if (bootstrap != null){
			URL bootstrapURL = null;
			try {
				bootstrapURL = new URL (protocol + "://"+bootstrap+"/");
			} catch (MalformedURLException e){
				throw new RuntimeException (e);
			}
			chord = new ChordImpl();
			try {
				chord.join(localURL , bootstrapURL);
				chord.insertAsync(new Key("AVAILABLE"), simpleURL);
			} catch (ServiceException e) {
				throw new RuntimeException("Could not join DHT!", e);
			}
		} else {
			chord = new ChordImpl();
			try {
				chord.create(localURL);
				//chord.insertAsync(new Key("AVAILABLE"), simpleURL);
			} catch (ServiceException e) {
				throw new RuntimeException("Could not create DHT!", e);
			}
		}
	}

	/**
	 * Removes deleted files (obtained from db) from file system
	 */
	public void updateFileSystem(){
		File dir = new File(dataPath);

		String[] fileIDs = dir.list();
		HashSet<Integer> deletedFiles;
		try {
			deletedFiles = Files.getDeletedFiles(connection);
			if(deletedFiles.size() > 0){
				for (String fileID : fileIDs) {
					if(deletedFiles.contains(Integer.parseInt(fileID))){
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
	public void insertMyFiles(){
		File dir = new File(dataPath);
		String[] fileIDs = dir.list();
		for (String fileID : fileIDs) {
			System.out.println(fileID);
			chord.insertAsync(new Key(fileID), simpleURL);
		}

	}

	/**
	 * Opens a hole in NAT to be able to receive udp messages from available peers in chord network
	 * @return
	 */
	public void udpHolePunch(){
		try {
			Set<Serializable> paulo = Peer.node.getChord().retrieve(new Key("AVAILABLE"));
			byte[] b2 = "Hello".getBytes();
			byte[] b1 = new byte[6];
			System.arraycopy(b2, 0, b1, 0, b2.length);

			for(Serializable s : paulo){
				DatagramPacket packet;
				try {
					packet = new DatagramPacket(b1, b1.length, InetAddress.getByName(((SimpleURL)s).getIpAddress()), ((SimpleURL)s).getPort());
					udpSocket.send(packet);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (ServiceException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public GatewayDevice getActiveGW() {
		return activeGW;
	}

	public void setActiveGW(GatewayDevice activeGW) {
		this.activeGW = activeGW;
	}

	public Chord getChord() {
		return chord;
	}

	public void setChord(ChordImpl chord) {
		this.chord = chord;
	}

	public String getEmail() {
		return email;
	}

	public String getIPAddress() {
		return IPAddress;
	}

	public void setIPAddress(String iPAddress) {
		IPAddress = iPAddress;
	}


	public DatagramSocket getUDPSocket() {
		return udpSocket;
	}


	public boolean is_port_forwarded() {
		return port_forwarded;
	}


	public SimpleURL getSimpleURL() {
		return simpleURL;
	}

}
