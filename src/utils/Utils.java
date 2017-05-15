package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;

import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.bitlet.weupnp.PortMappingEntry;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.registry.RegistryListener;
import org.fourthline.cling.support.igd.PortMappingListener;
import org.fourthline.cling.support.model.PortMapping;
import org.xml.sax.SAXException;

import server.main.Peer;

public final class Utils {

	public static final String FS = System.getProperty("file.separator");
	public static final String CRLF = "\r\n";
	public static final String Space = " ";
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static final String getFileID(String filePath){
		File file = new File(filePath);

		String raw = file.getAbsolutePath() + file.length() + file.lastModified();

		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(raw.getBytes("UTF-8"));
			byte[] hash = md.digest();
			String fileID = bytesToHex(hash);

			return fileID;
		} catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for ( int j = 0; j < bytes.length; j++ ) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	public static final boolean loadRD(){
		try{
			FileInputStream fis = new FileInputStream(Peer.rdFile);
			BufferedReader reader = new BufferedReader(new InputStreamReader(fis));

			String fileID = reader.readLine();
			int[] rds = new int[2];
			while(fileID != null){
				rds[0] = Integer.parseInt(reader.readLine());
				rds[1] = Integer.parseInt(reader.readLine());
				Peer.rdMap.put(fileID, rds);
				fileID = reader.readLine();
			}
			reader.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static final boolean loadMD(){
		try{
			FileInputStream fis = new FileInputStream(Peer.mdFile);
			BufferedReader reader = new BufferedReader(new InputStreamReader(fis));

			String path = reader.readLine();
			String fileID = null;
			while(path != null){
				fileID = reader.readLine();
				Peer.mdMap.put(path, fileID);
				path = reader.readLine();
			}
			reader.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static final boolean saveRD(){
		try{
			PrintWriter writer = new PrintWriter(Peer.rdFile, "UTF-8");
			Iterator it = Peer.rdMap.entrySet().iterator();
			while (it.hasNext()) {
				HashMap.Entry<String, int[]> pair = (HashMap.Entry)it.next();
				writer.println(pair.getKey());
				writer.println(pair.getValue()[0]);
				writer.println(pair.getValue()[1]);
			}
			writer.close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public static final boolean writeMD(){
		try{
			PrintWriter writer = new PrintWriter(Peer.mdFile, "UTF-8");
			Iterator it = Peer.mdMap.entrySet().iterator();
			while (it.hasNext()) {
				HashMap.Entry<String, String> pair = (HashMap.Entry)it.next();
				writer.println(pair.getKey());
				writer.println(pair.getValue());
			}
			writer.close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public static final boolean initFileSystem(){
		File dir = new File(Peer.dataPath);
		dir.mkdirs();
		File rdFile = new File(Peer.rdFile);
		File mdFile = new File(Peer.mdFile);
		try {
			if (!rdFile.exists()) {

				rdFile.createNewFile();

			}
			if (!mdFile.exists()) {
				mdFile.createNewFile();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}

	public static LinkedHashMap<String, int[]> sortMostReplicated() {
		List<Map.Entry<String, int[]>> list = new ArrayList<Map.Entry<String, int[]>>(Peer.rdMap.entrySet());

		Collections.sort(list, new Comparator<Map.Entry<String, int[]>>() {
			public int compare(Map.Entry<String, int[]> a, Map.Entry<String, int[]> b) {
				return Integer.compare((a.getValue()[1] - a.getValue()[0]),(b.getValue()[1] - b.getValue()[0]));
			}
		});

		LinkedHashMap<String, int[]> result = new LinkedHashMap<String, int[]>();
		for (Entry<String, int[]> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}

		return result;
	}

	public static long getusedCapacity(){
		long size = 0;
		File dataDir = new File(Peer.dataPath);
		for (File fileDir : dataDir.listFiles()) {
			for (File chunk : fileDir.listFiles()) {
				size += chunk.length();
			}
		}
		return size;
	}

	public static String[] getTCPfromSenderID(String senderID) {
		return senderID.split("\\.\\.");
	}

	public static void doPortForwarding() {

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
			Peer.activeGW = gatewayDiscover.getValidGateway();

			if (null != Peer.activeGW) {
				System.out.println("Using gateway: " + Peer.activeGW.getFriendlyName());
			} else {
				System.out.println("No active gateway device found");
				System.out.println("Stopping weupnp");
				return;
			}


			// testing PortMappingNumberOfEntries
			Integer portMapCount = Peer.activeGW.getPortMappingNumberOfEntries();
			System.out.println("GetPortMappingNumberOfEntries: " + (portMapCount!=null?portMapCount.toString():"(unsupported)"));

			// testing getGenericPortMappingEntry
			PortMappingEntry portMapping = new PortMappingEntry();
			if (Peer.activeGW.getGenericPortMappingEntry(0,portMapping))
				System.out.println("Portmapping #0 successfully retrieved ("+portMapping.getPortMappingDescription()+":"+portMapping.getExternalPort()+")");
			else
				System.out.println("Portmapping #0 retrival failed");

			InetAddress localAddress = Peer.activeGW.getLocalAddress();
			System.out.println("Using local address: "+ localAddress.getHostAddress());
			String externalIPAddress = Peer.activeGW.getExternalIPAddress();
			System.out.println("External address: "+ externalIPAddress);

			System.out.println("Querying device to see if a port mapping already exists for port "+ Peer.port);

			if (Peer.activeGW.getSpecificPortMappingEntry(Peer.port,"TCP",portMapping)) {
				System.out.println("Port "+Peer.port+" is already mapped. Aborting test.");
				return;
			}
			if (Peer.activeGW.getSpecificPortMappingEntry(Peer.port,"UDP",portMapping)) {
				System.out.println("Port "+Peer.port+" is already mapped. Aborting test.");
				return;
			}

			System.out.println("Mapping free. Sending port mapping request for port "+Peer.port);

			// test static lease duration mapping
			if (Peer.activeGW.addPortMapping(Peer.port,Peer.port,localAddress.getHostAddress(),"TCP","P2P Cloud (TCP)")) {
				System.out.println("Mapping TCP SUCCESSFUL.");
			}
			if (Peer.activeGW.addPortMapping(Peer.port,Peer.port,localAddress.getHostAddress(),"UDP","P2P Cloud (UDP)")) {
				System.out.println("Mapping UDP SUCCESSFUL.");
			}
		} catch (IOException | SAXException | ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
