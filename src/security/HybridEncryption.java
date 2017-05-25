package security;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class HybridEncryption {

    private KeyPair asymmetricKeyPair;
    private SecretKeySpec symmetricKey;
    private IvParameterSpec iv;
    public static String pathKey = "KEYS";

    public HybridEncryption() {
        try {
            this.asymmetricKeyPair = Encryptor.generateAsymmetricKeys();
            this.symmetricKey = Encryptor.generateSymmetricKey();
            this.iv = Encryptor.generateIV();
        } catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public HybridEncryption(KeyPair asymmetricKeyPair, SecretKeySpec symmetricKey, IvParameterSpec iv){
        this.asymmetricKeyPair = asymmetricKeyPair;
        this.symmetricKey = symmetricKey;
        this.iv = iv;
    }

    public byte[] encrypt(byte[] value){
        return Encryptor.encryptAES(this.symmetricKey, this.iv, value);
    }

    public byte[] decrypt(byte[] encrypted){
        return Encryptor.decryptAES(this.symmetricKey, this.iv, encrypted);
    }

    public byte[] encryptedSymmetricKey(){
        return Encryptor.encryptRSA(this.asymmetricKeyPair.getPublic(), symmetricKey.getEncoded());
    }

    public void decryptSymmetricKey(byte[] key){
        this.symmetricKey = new SecretKeySpec(Encryptor.decryptRSA(this.asymmetricKeyPair.getPrivate(), key), "AES");
    }

    public Key getAsymmetricPublicKey(){
        return asymmetricKeyPair.getPublic();
    }

    public Key getAsymmetricPrivateKey(){
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

    public void saveKeysFile(){


    }

    public void loadKeysFile (){

    }

    //FOR TESTING

    public static void main(String[] args) {

    }
}
