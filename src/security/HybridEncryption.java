package security;

import server.Peer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class HybridEncryption {

    private PublicKey asymmetricKeyPublic;
    private PrivateKey asymmetricKeyPrivate;
    private SecretKeySpec symmetricKey;
    private IvParameterSpec iv;
  //  private static String pathKeyPublic = "KEY_PUBLIC.key";
    //private static String pathKeySymmetric = "KEY_SYMMETRIC.key";


    public HybridEncryption() {
    }

    public HybridEncryption(KeyPair asymmetricKeyPair, SecretKeySpec symmetricKey, IvParameterSpec iv) {
        this.asymmetricKeyPublic = asymmetricKeyPair.getPublic();
        this.asymmetricKeyPrivate = asymmetricKeyPair.getPrivate();
        this.symmetricKey= symmetricKey;
        this.iv = iv;
    }

    public void generateKeys() {
        try {
            KeyPair keyPair = Encryptor.generateAsymmetricKeys();
            this.asymmetricKeyPublic = keyPair.getPublic();
            this.asymmetricKeyPrivate = keyPair.getPrivate();
            this.symmetricKey = Encryptor.generateSymmetricKey();
            this.iv = Encryptor.generateIV();


            System.out.println("generate keys completed");
        } catch (NoSuchAlgorithmException | NoSuchProviderException| NoSuchPaddingException | UnsupportedEncodingException  e) {
            e.printStackTrace();
        }
    }

    public void bytesToAsymmetricKeyPrivate(KeyFactory kf, byte[] privateKeyBytes) {

        try {
            this.asymmetricKeyPrivate = kf.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    public void bytesToAsymmetricKeyPublic(KeyFactory kf, byte[] publicKeyBytes) {

        try {
            this.asymmetricKeyPublic =  kf.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
        } catch (InvalidKeySpecException e) {
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
    /*

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
        System.out.println("Saved the keys to the file successfully");


    }


    public boolean loadKeysFile() {

        try {
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(pathKeySymmetric));

            this.symmetricKey = (SecretKeySpec) inputStream.readObject();

            inputStream .close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(pathKeyPublic));

            this.asymmetricKeyPublic = (PublicKey) inputStream.readObject();

            inputStream .close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("Uploaded the keys to the file successfully");

        return true;
    }
*/
//FOR TESTING

    public static void main(String[] args) {

    /*    HybridEncryption a = new HybridEncryption();
        a.generateKeys();
        System.out.println("Symmetric Antes--  " + a.getSymmetricKey());
        System.out.println("Assimetric Antes--  "  + a.getAsymmetricPublicKey());
        a.saveKeysFile();

        System.out.println();
        System.out.println();

        HybridEncryption b = new HybridEncryption();
        b.loadKeysFile();
        System.out.println("Symettic depois    " + b.getSymmetricKey() );
        System.out.println("Assimetric depois   " + b.getAsymmetricPublicKey());*/
    }
}
