package security;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.io.UnsupportedEncodingException;
import java.security.*;
import java.util.Arrays;

public class Encryptor {

    public static int AES_KEY_SIZE = 16;
    public static int RSA_KEY_SIZE = 2048;

    public static byte[] encryptAES(SecretKeySpec skeySpec, IvParameterSpec iv, byte[] value) {
        try {
            /*
                - CBC (Cipher Block Chaining) makes one ciphertext block validity depend on all the previous ciphertext blocks.
                - Padding is a technique used to increase the length of a message prior to encryption so that its actual length is not disclosed.
             */
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            return cipher.doFinal(value);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static byte[] decryptAES(SecretKeySpec skeySpec, IvParameterSpec iv, byte[] encrypted) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            return cipher.doFinal(encrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static byte[] encryptRSA(PublicKey publicKey, byte[] value) {

        Cipher cipher;
        byte[] encrypted = null;

        try {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            encrypted = cipher.doFinal(value);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        return encrypted;
    }

    public static byte[] decryptRSA(PrivateKey privateKey, byte[] encrypted) {
        Cipher cipher;
        byte[] decrypted = null;

        try {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            decrypted = cipher.doFinal(encrypted);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        return decrypted;
    }

    public static KeyPair generateAsymmetricKeys() throws NoSuchAlgorithmException, NoSuchProviderException {

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(RSA_KEY_SIZE);

        return keyGen.generateKeyPair();
    }


    public static SecretKeySpec generateSymmetricKey() throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException {

        SecureRandom rnd = new SecureRandom();
        byte[] key = new byte[AES_KEY_SIZE];
        rnd.nextBytes(key);
        return new SecretKeySpec(key, "AES");
    }

    public static IvParameterSpec generateIV(){
        SecureRandom randomSecureRandom;
        IvParameterSpec ivParams = null;

        try {
            randomSecureRandom = SecureRandom.getInstance("SHA1PRNG");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            byte[] iv = new byte[cipher.getBlockSize()];
            randomSecureRandom.nextBytes(iv);

            ivParams = new IvParameterSpec(iv);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }

        return ivParams;
    }

    // FOR TESTING
    public static void main(String[] args) {

        SecretKeySpec secretKeySpec;
        KeyPair keyPair;
        IvParameterSpec initVector;

        try {
            initVector = generateIV();
            secretKeySpec = generateSymmetricKey();

            System.out.println("Secret symmetric key:\n\t" + Arrays.toString(secretKeySpec.getEncoded()));

            keyPair = generateAsymmetricKeys();

            System.out.println("Asymmetric key pair:");
            System.out.println("\tPublic key: " + Arrays.toString(keyPair.getPublic().getEncoded()));
            System.out.println("\tPrivate key: " + Arrays.toString(keyPair.getPrivate().getEncoded()));

            byte[] encrypted = encryptAES(secretKeySpec, initVector, "HelloWorld".getBytes());

            System.out.println(new String(decryptAES(secretKeySpec, initVector, encrypted)));

        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException e) {
            e.printStackTrace();
        }

    }
}