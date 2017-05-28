package security;

import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class HybridEncryption {

    private PublicKey asymmetricKeyPublic;
    private PrivateKey asymmetricKeyPrivate;
    private SecretKeySpec symmetricKey;
    private IvParameterSpec iv;


    public HybridEncryption() {
    }

    public HybridEncryption(KeyPair asymmetricKeyPair, SecretKeySpec symmetricKey, IvParameterSpec iv) {
        this.asymmetricKeyPublic = asymmetricKeyPair.getPublic();
        this.asymmetricKeyPrivate = asymmetricKeyPair.getPrivate();
        this.symmetricKey = symmetricKey;
        this.iv = iv;
    }

    public void generateKeys() {
        try {
            KeyPair keyPair = Encryptor.generateAsymmetricKeys();
            this.asymmetricKeyPublic = keyPair.getPublic();
            this.asymmetricKeyPrivate = keyPair.getPrivate();
            this.symmetricKey = Encryptor.generateSymmetricKey();
            this.iv = new IvParameterSpec(symmetricKey.getEncoded());


            System.out.println("generate keys completed");
        } catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException | UnsupportedEncodingException e) {
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
            this.asymmetricKeyPublic = kf.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
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
        this.iv = new IvParameterSpec(symmetricKey.getEncoded());
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

}
