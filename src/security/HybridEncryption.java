package security;

import java.io.*;
import java.security.*;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

//KeyPair(PublicKey publicKey, PrivateKey privateKey)
public class HybridEncryption {

    static KeyPair asymmetricKeyPair;
    private SecretKeySpec symmetricKey;
    private IvParameterSpec iv;
    public static String pathKeyPublic = "KEY_PUBLIC.key";
    public static String pathKeyPublicasymetric = "KEY_PUBLIC_ASY.key";


    public HybridEncryption() {
        try {
            this.asymmetricKeyPair = Encryptor.generateAsymmetricKeys();
            this.symmetricKey = Encryptor.generateSymmetricKey();
            this.iv = Encryptor.generateIV();
        } catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public HybridEncryption(KeyPair asymmetricKeyPair, SecretKeySpec symmetricKey, IvParameterSpec iv) {
        this.asymmetricKeyPair = asymmetricKeyPair;
        this.symmetricKey = symmetricKey;
        this.iv = iv;
    }

    public byte[] encrypt(byte[] value) {
        return Encryptor.encryptAES(this.symmetricKey, this.iv, value);
    }

    public byte[] decrypt(byte[] encrypted) {
        return Encryptor.decryptAES(this.symmetricKey, this.iv, encrypted);
    }

    public byte[] encryptedSymmetricKey() {
        return Encryptor.encryptRSA(this.asymmetricKeyPair.getPublic(), symmetricKey.getEncoded());
    }

    public void decryptSymmetricKey(byte[] key) {
        this.symmetricKey = new SecretKeySpec(Encryptor.decryptRSA(this.asymmetricKeyPair.getPrivate(), key), "AES");
    }

    public Key getAsymmetricPublicKey() {
        return asymmetricKeyPair.getPublic();
    }

    public Key getAsymmetricPrivateKey() {
        return asymmetricKeyPair.getPrivate();
    }

    public KeyPair getAsymmetricKeyPair() {
        return asymmetricKeyPair;
    }

    public SecretKeySpec getSymmetricKey() {
        return symmetricKey;
    }

    public IvParameterSpec getIv() {
        return iv;
    }

    public void saveKeysFile() {

        // Save public key
        ObjectOutputStream chavePublicaOS = null;
        try {
            chavePublicaOS = new ObjectOutputStream(
                    new FileOutputStream(pathKeyPublic));

            chavePublicaOS.writeObject(asymmetricKeyPair.getPublic());
            chavePublicaOS.close();

        } catch (IOException e) {
            e.printStackTrace();

        }
    }


    public PublicKey loadKeysFile() {

        PublicKey publicKey = null;


        ObjectInputStream inputStream = null;
        try {
            inputStream = new ObjectInputStream(new FileInputStream(pathKeyPublic));

            publicKey = (PublicKey) inputStream.readObject();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return publicKey;
    }

    //FOR TESTING

    public static void main(String[] args) {

    }
}
