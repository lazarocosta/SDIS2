package security;

import java.io.*;
import java.security.*;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

//KeyPair(PublicKey publicKey, PrivateKey privateKey)
public class HybridEncryption {

    private PublicKey asymmetricKeyPublic;
    private PrivateKey asymmetricKeyPrivate;
    private SecretKeySpec symmetricKey;
    private IvParameterSpec iv;
    public static String pathKeyPublic = "KEY_PUBLIC.key";
    public static String pathKeySymmetric = "KEY_SYMMETRIC.key";


    public HybridEncryption() {
        try {
            KeyPair keyPair = Encryptor.generateAsymmetricKeys();
            this.asymmetricKeyPublic = keyPair.getPublic();
            this.asymmetricKeyPrivate = keyPair.getPrivate();
            this.symmetricKey = Encryptor.generateSymmetricKey();
            this.iv = Encryptor.generateIV();
        } catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public HybridEncryption(KeyPair asymmetricKeyPair, SecretKeySpec symmetricKey, IvParameterSpec iv) {
        this.asymmetricKeyPublic = asymmetricKeyPair.getPublic();
        this.asymmetricKeyPrivate = asymmetricKeyPair.getPrivate();
        this.iv = iv;
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

        // Save public key
        ObjectOutputStream keyPublicOb = null;
        try {
            keyPublicOb = new ObjectOutputStream(
                    new FileOutputStream(pathKeyPublic));

            keyPublicOb.writeObject(this.encryptedSymmetricKey());
            keyPublicOb.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Save symmetric Key
        ObjectOutputStream symmetricKeyOb = null;
        try {
            symmetricKeyOb = new ObjectOutputStream(
                    new FileOutputStream(pathKeySymmetric));

            symmetricKeyOb.write(this.encryptedSymmetricKey());
            symmetricKeyOb.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean loadKeysFile() {

        try {
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(pathKeyPublic));

            byte[] dataRead = new byte[100];
            inputStream.read(dataRead);
            this.decryptSymmetricKey(dataRead);

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        try {
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(pathKeySymmetric));

            this.symmetricKey = (SecretKeySpec) inputStream.readObject();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    //FOR TESTING

    public static void main(String[] args) {


    }
}
