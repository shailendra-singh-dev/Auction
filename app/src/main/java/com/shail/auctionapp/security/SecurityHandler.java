package com.shail.auctionapp.security;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SecurityHandler {
    public static final String TAG = "SecurityHandler";
    public static final String PASSWORD = "_credit_cards";

    private Cipher mCipher;
    private SecretKey mSecretKey;
    private IvParameterSpec mIvParameterSpec;

    private static String CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static String CIPHER_ALGORITHM = "AES";

    // Replace me with a 16-byte key, share between Sender and Reciever
    private static byte[] rawSecretKey =
            {
                    0x12, 0x00, 0x22, 0x55, 0x33, 0x78, 0x25, 0x11,
                    0x33, 0x45, 0x00, 0x00, 0x34, 0x00, 0x23, 0x28
            };

    private static String MESSAGEDIGEST_ALGORITHM = "MD5";

    /**
     * Constructor, intiate the object, creates a SecretKeySpec and IvParameterSpec
     *
     * @param passphrase the password phrase on which the cryptography will be depended
     */
    public SecurityHandler(String passphrase) {
        //decodes passd phrase to encrypted byte[]
        byte[] passwordKey = encodeDigest(passphrase);

        try {
            //mCipher instantiation passing transformation parameter
            mCipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        } catch (NoSuchAlgorithmException e) {    //Invalid algorithm in passed transformation parameter
            Log.e(TAG, "No such algorithm " + CIPHER_ALGORITHM, e);
        } catch (NoSuchPaddingException e) {    //Invalid padding in passed transformation parameter
            Log.e(TAG, "No such padding PKCS5", e);
        }

        //Encodes the passed password phrase to a byte[] 
        //that will be stored in class private member of SecretKey type
        mSecretKey = new SecretKeySpec(passwordKey, CIPHER_ALGORITHM);

        //Creates a new IvParameterSpec instance with the bytes 
        //from the specified buffer iv used as initialization vector.
        mIvParameterSpec = new IvParameterSpec(rawSecretKey);
    }

    /**
     * Encrypts the passed byte array
     *
     * @param clearData original byte[] data
     * @return byte[]
     * encrypted bytes
     */
    public byte[] Encrypt(byte[] clearData) {
        byte[] result = null;

        try {    //Cipher initialization in ENCRYPT_MODE
            mCipher.init(Cipher.ENCRYPT_MODE, mSecretKey, mIvParameterSpec);
        } catch (InvalidKeyException e) {
            Log.e(TAG, "Invalid key", e);
        } catch (InvalidAlgorithmParameterException e) {
            Log.e(TAG, "Invalid algorithm " + CIPHER_ALGORITHM, e);
        }

        result = DoWork(clearData);

        return result;
    }

    /**
     * Decrypts the passed byte array
     *
     * @return byte[]
     * encrypted bytes
     */
    public byte[] Decrypt(byte[] data) {
        byte[] result = null;

        try {    //Cipher initialization in DECRYPT_MODE
            mCipher.init(Cipher.DECRYPT_MODE, mSecretKey, mIvParameterSpec);
        } catch (InvalidKeyException e) {
            Log.e(TAG, "Invalid key", e);
        } catch (InvalidAlgorithmParameterException e) {
            Log.e(TAG, "Invalid algorithm " + CIPHER_ALGORITHM, e);
        }

        result = DoWork(data);

        return result;
    }
    /*Performs Cryptology, based on mCipher Cipher mode initialization*/

    /**
     * @param data on which the work will be performed.
     * @return Returns result of Cipher.doFinal() method
     */
    public byte[] DoWork(byte[] data) {
        byte[] result = null;

        try {
            result = mCipher.doFinal(data);
        } catch (IllegalBlockSizeException e) {
            Log.e(TAG, "Illegal block size", e);
        } catch (BadPaddingException e) {
            Log.e(TAG, "Bad padding", e);
        }

        return result;
    }

    /**
     * Performs the final update and then computes and returns the final hash value for this MessageDigest. After the digest is computed the receiver is reset.
     *
     * @return
     */
    private byte[] encodeDigest(String text) {
        byte[] result = null;
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(MESSAGEDIGEST_ALGORITHM);
            result = digest.digest(text.getBytes());
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "No such algorithm " + MESSAGEDIGEST_ALGORITHM, e);
        }

        return result;
    }

    private byte[] readFromLocalFile(String fileName)
    {
        byte[] result = null;

        if (fileName == null)
            return result;

        File file = new File(fileName);
        if (file == null || !file.exists())
            return result;

        int size = (int) file.length();
        result = new byte[size];

        BufferedInputStream buf = null;
        try {
            buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(result, 0, result.length);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (buf != null)
                try {
                    buf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

        return result;
    }

    public String readFromFile(String filename) {
        byte[] fileContent = readFromLocalFile(filename);
        if (fileContent == null)
            return "";

        byte[] decrypted = Decrypt(fileContent);
        if (decrypted == null)
            return "";

        String result;
        try {
            result = new String(decrypted, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            result = "";
        }

        return result;
    }

    public void saveToFile(String filename, String data) throws IOException
    {
        byte[] bytes = Encrypt(data.getBytes());

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(new File(filename));
            out.write(bytes);
        } finally {
            if (out != null)
                out.close();
        }
    }
}