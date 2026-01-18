package com.nexis.crypto;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

public class SignatureUtil {

    /**
     * Signs data using the Private Key.
     * 
     * @param privateKey The private key to sign with
     * @param data       The data string to sign
     * @return Base64 encoded signature
     */
    public static byte[] applyECDSASig(PrivateKey privateKey, String data) {
        try {
            Signature dsa = Signature.getInstance("SHA256withECDSA");
            dsa.initSign(privateKey);
            dsa.update(data.getBytes());
            return dsa.sign();
        } catch (Exception e) {
            throw new RuntimeException("Error signing data", e);
        }
    }

    /**
     * Verifies the signature using the Public Key.
     * 
     * @param publicKey The public key to verify against
     * @param data      The original data string
     * @param signature The byte array signature
     * @return true if valid, false otherwise
     */
    public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature) {
        try {
            Signature dsa = Signature.getInstance("SHA256withECDSA");
            dsa.initVerify(publicKey);
            dsa.update(data.getBytes());
            return dsa.verify(signature);
        } catch (Exception e) {
            throw new RuntimeException("Error verifying signature", e);
        }
    }
}
