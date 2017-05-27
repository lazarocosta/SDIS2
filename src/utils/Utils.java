package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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
		/*try{
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
		}*/
		return false; //DELETE THIS
	}

	public static final boolean loadMD(){
		/*try{
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
		}*/
		return false; //DELETE THIS
	}

	public static final boolean saveRD(){
		/*try{
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
		}*/
		return false; //DELETE THIS
	}

	public static final boolean writeMD(){
		/*try{
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
		}*/
		return false; //DELETE THIS
	}

	public static final boolean initFileSystem(){
		File dir = new File(Peer.dataPath);
		dir.mkdirs();
		/*File rdFile = new File(Peer.rdFile);
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
		}*/

		return true;
	}

	public static LinkedHashMap<String, int[]> sortMostReplicated() {
		/*List<Map.Entry<String, int[]>> list = new ArrayList<Map.Entry<String, int[]>>(Peer.rdMap.entrySet());

		Collections.sort(list, new Comparator<Map.Entry<String, int[]>>() {
			public int compare(Map.Entry<String, int[]> a, Map.Entry<String, int[]> b) {
				return Integer.compare((a.getValue()[1] - a.getValue()[0]),(b.getValue()[1] - b.getValue()[0]));
			}
		});
*/
		LinkedHashMap<String, int[]> result = new LinkedHashMap<String, int[]>();
		/*for (Entry<String, int[]> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}*/

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
	
	public String[] getFileIds(){
		File dir = new File(Peer.dataPath);
        String[] fileIds = dir.list();
		
		return fileIds;
	}

	public static boolean restoreFileFromTmpFolder(String tmpFolderPath){
		return true;
	}
}
