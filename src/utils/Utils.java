package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import server.Peer;

public final class Utils {

	public static final String FS = System.getProperty("file.separator");
	public static final String CRLF = "\r\n";
	public static final String Space = " ";

	public static final boolean initFileSystem() {
		File dir = new File(Peer.dataPath);
		dir.mkdirs();

		return true;
	}

	public static long getusedCapacity() {
		long size = 0;
		File dataDir = new File(Peer.dataPath);
		for (File fileDir : dataDir.listFiles()) {
			for (File chunk : fileDir.listFiles()) {
				size += chunk.length();
			}
		}
		return size;
	}

	public String[] getFileIds() {
		File dir = new File(Peer.dataPath);
		String[] fileIds = dir.list();

		return fileIds;
	}

	public static boolean restoreFileFromTmpFolder(String destFile, String tmpFolderPath, int numChunks) {
		File outFile = new File(destFile);
		FileOutputStream fos;
		FileInputStream fis;
		byte[] chunk;
		int bytesRead = 0;
		try {
			fos = new FileOutputStream(outFile, true);
			for (int i = 0; i < numChunks; i++) {
				File chunkFile = new File(tmpFolderPath + Utils.FS + i);
				fis = new FileInputStream(chunkFile);
				chunk = new byte[(int) chunkFile.length()];
				bytesRead = fis.read(chunk, 0, (int) chunkFile.length());
				assert (bytesRead == chunk.length);
				assert (bytesRead == (int) chunkFile.length());
				fos.write(chunk);
				fos.flush();
				chunk = null;
				fis.close();
				fis = null;
				chunkFile.delete();
			}
			new File(tmpFolderPath).delete();
			fos.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
