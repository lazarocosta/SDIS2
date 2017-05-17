package security;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import java.util.Arrays;

public class Encryptor {
    /**
     * @param key The key should be secret. For us it will be a string made by fileId + username.
     * @param initVector The initVector should be constant and is expected to be public.
     * @param value The value to encrypt.
     * @return The string encrypted.
     */
    public static String encrypt(String key, String initVector, String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            /*
                - We choose AES (Advance Encryption Standard) instead of RSA, because AES can only be broken by brute-force, while RSA can be broken with modulus calculations.
                - CBC (Cipher Block Chaining) makes one ciphertext block validity depend on all the previous ciphertext blocks.
                - Padding is a technique used to increase the length of a message prior to encryption so that its actual length is not disclosed.
             */
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());
            System.out.println("encrypted string: "
                    + Base64.encodeBase64String(encrypted));

            return Base64.encodeBase64String(encrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static String decrypt(String key, String initVector, String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));

            return new String(original);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static byte[] encrypt(String key, String initVector, byte[] value)
    {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            /*
                - We choose AES (Advance Encryption Standard) instead of RSA, because AES can only be broken by brute-force, while RSA can be broken with modulus calculations.
                - CBC (Cipher Block Chaining) makes one ciphertext block validity depend on all the previous ciphertext blocks.
                - Padding is a technique used to increase the length of a message prior to encryption so that its actual length is not disclosed.
             */
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value);
            System.out.println("encrypted byte array: "
                    + Arrays.toString(Base64.encodeBase64(encrypted)));

            return Base64.encodeBase64(encrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static byte[] decrypt(String key, String initVector, byte[] encrypted)
    {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            return cipher.doFinal(Base64.decodeBase64(encrypted));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static void main(String[] args) {
        String key = "Bar12345Bar12345"; // 128 bit key
        String initVector = "RandomInitVector"; // 16 bytes IV

        System.out.println(decrypt(key, initVector,
                encrypt(key, initVector, "Hello World")));

        System.out.println(Arrays.toString(decrypt(key, initVector,
                encrypt(key, initVector, "Hello World".getBytes()))));
    }
}