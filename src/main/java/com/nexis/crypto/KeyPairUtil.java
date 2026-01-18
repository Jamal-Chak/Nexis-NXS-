package com.nexis.crypto;

import java.security.*;
import java.security.spec.ECGenParameterSpec;

public class KeyPairUtil {

    /**
     * Generates a new ECDSA KeyPair (Public & Private Key).
     */
    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
            SecureRandom random = SecureRandom.getInstanceStrong();
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
            keyGen.initialize(ecSpec, random);
            return keyGen.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate key pair", e);
        }
    }

    /**
     * Returns the hex representation of the Key (Public or Private).
     */
    public static String getStringFromKey(Key key) {
        return HashUtil.bytesToHex(key.getEncoded());
    }

    /**
     * Generates a Nexis Address from a Public Key.
     * Address = SHA-256(PublicKey)
     */
    public static String getAddressFromPublicKey(PublicKey publicKey) {
        // 1. Get encoded bytes of public key
        // 2. Hash them with SHA-256
        // 3. Return hex string
        String hexKey = getStringFromKey(publicKey);
        return HashUtil.applySha256(hexKey);
    }

    public static PublicKey getPublicKeyFromString(String hex) {
        try {
            byte[] byteKey = HashUtil.hexToBytes(hex);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            return keyFactory.generatePublic(new java.security.spec.X509EncodedKeySpec(byteKey));
        } catch (Exception e) {
            throw new RuntimeException("Failed to reconstruct Public Key", e);
        }
    }

    public static PrivateKey getPrivateKeyFromString(String hex) {
        try {
            byte[] byteKey = HashUtil.hexToBytes(hex);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            return keyFactory.generatePrivate(new java.security.spec.PKCS8EncodedKeySpec(byteKey));
        } catch (Exception e) {
            throw new RuntimeException("Failed to reconstruct Private Key", e);
        }
    }
}
