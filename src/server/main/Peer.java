package server.main;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;

import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.bitlet.weupnp.PortMappingEntry;
import org.xml.sax.SAXException;

import de.uniba.wiai.lspi.chord.com.CommunicationException;
import de.uniba.wiai.lspi.chord.com.Entry;
import de.uniba.wiai.lspi.chord.com.Node;
import de.uniba.wiai.lspi.chord.com.RefsAndEntries;
import de.uniba.wiai.lspi.chord.com.local.ThreadEndpoint;
import de.uniba.wiai.lspi.chord.com.socket.SocketEndpoint;
import de.uniba.wiai.lspi.chord.console.command.entry.Key;
import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.Chord;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;
import server.protocol.ClientInterface;
import utils.Utils;

public class Peer{

	public static Peer node = null;
	private String email = null;
	private String IPAddress = null;
	private int port = 60000;
	private GatewayDevice activeGW = new GatewayDevice();
	private Chord chord = null;//new ChordImpl();

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

	/**
	 * Main service starter
	 * @param args Server arguments by the following order:
	 * 1 - Protocol Version
	 * 2 - Server ID
	 * 3 - RMI Remote Object name
	 * 4 - MC Address
	 * 5 - MC Port
	 * 6 - MDB Address
	 * 7 - MDB Port
	 * 8 - MDR Address
	 * 9 - MDR Port
	 */
	public static void main(String[] args) {
		if(args.length != 9){
			System.out.println("Usage: Peer <Protocol Version> <Server ID> <RMI Remote Object Name> <MC Address> <MC Port> <MDB Address> <MDB Port> <MDR Address> <MDR Port>");
			return;
		}

		protocolVersion = args[0];
		if(protocolVersion == null){
			System.out.println("Null protocol version");
		}

		serverID = args[1];
		if (serverID == null){
			System.out.println("Server ID wrong!");
			return;
		}

		remoteObject = args[2];
		if (remoteObject == null){
			System.out.println("Remote Object Name wrong!");
			return;
		}

		mcAddress = args[3];
		if (mcAddress == null){
			System.out.println("MC Address wrong!");
			return;
		}

		mcPort = Integer.parseInt(args[4]);
		if (mcPort < 1024){
			System.out.println("MC Port wrong!");
			return;
		}
		mdbAddress = args[5];
		if (mdbAddress == null){
			System.out.println("MDB Address wrong!");
			return;
		}

		mdbPort = Integer.parseInt(args[6]);
		if (mdbPort < 1024){
			System.out.println("MDB Port wrong!");
			return;
		}

		mdrAddress = args[7];
		if (mdrAddress == null){
			System.out.println("MDR Address wrong!");
			return;
		}

		mdrPort = Integer.parseInt(args[8]);
		if (mdrPort < 1024){
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

		if(protocolVersion.equals("2.0")){
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

	public void safeClose(){
		try {
			activeGW.deletePortMapping(port,"TCP");
			activeGW.deletePortMapping(port,"UDP");
			chord.remove(new Key("AVAILABLE"), IPAddress + ":" + port);
			chord.leave();
		} catch (IOException | SAXException | ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public void initializeIPAddressesAndPorts(boolean localConnection) {

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

				// Cria o JOptionPane por showMessageDialog
				String[] choices = new String[choicestmp.size()];
				choicestmp.toArray(choices);
				int selected = JOptionPane.showOptionDialog(
						null
						, "Pergunta?"        // Mensagem
						, "Titulo"               // Titulo
						, JOptionPane.YES_NO_OPTION  
						, JOptionPane.PLAIN_MESSAGE                               
						, null // Icone. Você pode usar uma imagem se quiser, basta carrega-la e passar como referência
						, choices // Array de strings com os valores de cada botão. Veja o exemplo abaixo **
						, choices[0]    // Label do botão Default
						);
				IPAddress = choices[selected];
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
					return;
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

				if (null != Peer.node.getActiveGW()) {
					System.out.println("Using gateway: " + Peer.node.getActiveGW().getFriendlyName());
				} else {
					System.out.println("No active gateway device found");
					System.out.println("Stopping weupnp");
					return;
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

				if (Peer.node.getActiveGW().getSpecificPortMappingEntry(Peer.node.getPort(),"TCP",portMapping)) {
					System.out.println("Port "+Peer.node.getPort()+" is already mapped. Aborting test.");
					return;
				}
				if (Peer.node.getActiveGW().getSpecificPortMappingEntry(Peer.node.getPort(),"UDP",portMapping)) {
					System.out.println("Port "+Peer.node.getPort()+" is already mapped. Aborting test.");
					return;
				}

				System.out.println("Mapping free. Sending port mapping request for port "+Peer.node.getPort());

				// test static lease duration mapping
				if (Peer.node.getActiveGW().addPortMapping(Peer.node.getPort(),Peer.node.getPort(),localAddress.getHostAddress(),"TCP","P2P Cloud (TCP)")) {
					System.out.println("Mapping TCP SUCCESSFUL.");
				}
				if (Peer.node.getActiveGW().addPortMapping(Peer.node.getPort(),Peer.node.getPort(),localAddress.getHostAddress(),"UDP","P2P Cloud (UDP)")) {
					System.out.println("Mapping UDP SUCCESSFUL.");
				}

			} catch (IOException | SAXException | ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public void joinChordNetwork(String bootstrap){
		de.uniba.wiai.lspi.chord.service.PropertiesLoader.loadPropertyFile ();
		String protocol = URL.KNOWN_PROTOCOLS.get(URL.SOCKET_PROTOCOL);
		URL localURL = null;
		try {
			localURL = new URL (protocol + "://"+IPAddress+":"+port+"/");
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
			} catch (ServiceException e) {
				throw new RuntimeException("Could not join DHT!", e);
			}
		} else {
			chord = new ChordImpl();
			try {
				chord.create(localURL);
			} catch (ServiceException e) {
				throw new RuntimeException("Could not create DHT!", e);
			}
		}
		try {
			chord.insert(new Key("AVAILABLE"), IPAddress+":"+port);
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	public void setChord(Chord chord) {
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

}
