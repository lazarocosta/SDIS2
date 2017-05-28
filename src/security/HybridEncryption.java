package security;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class HybridEncryption {

	private PublicKey asymmetricKeyPublic;
	private PrivateKey asymmetricKeyPrivate;
	private SecretKeySpec symmetricKey;
	private IvParameterSpec iv;
	public static String pathKeyPublic = "KEY_PUBLIC.key";
	public static String pathKeySymmetric = "KEY_SYMMETRIC.key";

	public HybridEncryption() {
	}

	public HybridEncryption(KeyPair asymmetricKeyPair, SecretKeySpec symmetricKey, IvParameterSpec iv) {
		this.asymmetricKeyPublic = asymmetricKeyPair.getPublic();
		this.asymmetricKeyPrivate = asymmetricKeyPair.getPrivate();
		this.iv = iv;
	}

	public void generateKeys() {
		try {
			KeyPair keyPair = Encryptor.generateAsymmetricKeys();
			this.asymmetricKeyPublic = keyPair.getPublic();
			this.asymmetricKeyPrivate = keyPair.getPrivate();
			this.symmetricKey = Encryptor.generateSymmetricKey();
			this.iv = Encryptor.generateIV();
		} catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException
				| UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public byte[] encrypt(byte[] value) {
		return Encryptor.encryptAES(this.symmetricKey, this.iv, value);
	}

	public byte[] decrypt(byte[] encrypted) {
		return Encryptor.decryptAES(this.symmetricKey, this.iv, encrypted);
	}

	public byte[] encryptedSymmetricKey() {
		return Encryptor.encryptRSA(this.asymmetricKeyPublic, symmetricKey.getEncoded());
	}

	public void decryptSymmetricKey(byte[] key) {
		this.symmetricKey = new SecretKeySpec(Encryptor.decryptRSA(asymmetricKeyPrivate, key), "AES");
	}

	public PublicKey getAsymmetricPublicKey() {
		return asymmetricKeyPublic;
	}

	public PrivateKey getAsymmetricPrivateKey() {
		return asymmetricKeyPrivate;
	}

	public SecretKeySpec getSymmetricKey() {
		return symmetricKey;
	}

	public IvParameterSpec getIv() {
		return iv;
	}

	public void saveKeysFile() {

		try {
			ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(pathKeySymmetric));
			outputStream.writeObject(symmetricKey);
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(pathKeyPublic));
			outputStream.writeObject(asymmetricKeyPublic);
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public boolean loadKeysFile() {

		try {
			ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(pathKeySymmetric));

			this.symmetricKey = (SecretKeySpec) inputStream.readObject();

			inputStream.close();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}

		try {
			ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(pathKeyPublic));

			this.asymmetricKeyPublic = (PublicKey) inputStream.readObject();

			inputStream.close();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}

		/*
		 * try { ObjectInputStream inputStream = new ObjectInputStream(new
		 * FileInputStream(pathKeyPublic));
		 * 
		 * byte[] dataRead = new byte[1000];
		 * System.out.println(inputStream.read()); //
		 * this.decryptSymmetricKey(dataRead);
		 * 
		 * } catch (IOException e) { e.printStackTrace(); return false; }
		 * 
		 * try { ObjectInputStream inputStream = new ObjectInputStream(new
		 * FileInputStream(pathKeySymmetric));
		 * 
		 * 
		 * System.out.println(inputStream.read());
		 * 
		 * } catch (IOException e) { e.printStackTrace(); return false; }
		 */
		return true;
	}

	// FOR TESTING

	public static void main(String[] args) {

		HybridEncryption a = new HybridEncryption();
		a.generateKeys();
		System.out.println("Symmetric Antes--  " + a.getSymmetricKey());
		System.out.println("Assimetric Antes--  " + a.getAsymmetricPublicKey());
		a.saveKeysFile();

		System.out.println();
		System.out.println();

		HybridEncryption b = new HybridEncryption();
		b.loadKeysFile();
		System.out.println("Symettic depois    " + b.getSymmetricKey());
		System.out.println("Assimetric depois   " + b.getAsymmetricPublicKey());
	}
}
