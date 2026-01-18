package com.nexis.crypto;

import org.junit.jupiter.api.Test;
import java.security.KeyPair;
import static org.junit.jupiter.api.Assertions.*;

public class CryptoTest {

    @Test
    public void testSha256() {
        String input = "Nexis";
        String hash = HashUtil.applySha256(input);
        assertNotNull(hash);
        assertEquals(64, hash.length()); // SHA-256 hex is 64 chars
        // Known hash for "Nexis" could be verified, but length/notNull is good for now
    }

    @Test
    public void testKeyPairGeneration() {
        KeyPair keyPair = KeyPairUtil.generateKeyPair();
        assertNotNull(keyPair.getPrivate());
        assertNotNull(keyPair.getPublic());
    }

    @Test
    public void testSignature() {
        KeyPair keyPair = KeyPairUtil.generateKeyPair();
        String data = "Transaction Data: Alice -> Bob 50 NXS";
        
        // Sign
        byte[] signature = SignatureUtil.applyECDSASig(keyPair.getPrivate(), data);
        assertNotNull(signature);
        
        // Verify
        boolean isValid = SignatureUtil.verifyECDSASig(keyPair.getPublic(), data, signature);
        assertTrue(isValid);
        
        // Verify Tampered
        boolean isTamperedValid = SignatureUtil.verifyECDSASig(keyPair.getPublic(), data + "tamper", signature);
        assertFalse(isTamperedValid);
    }
    
    @Test
    public void testAddressGeneration() {
        KeyPair keyPair = KeyPairUtil.generateKeyPair();
        String address = KeyPairUtil.getAddressFromPublicKey(keyPair.getPublic());
        assertNotNull(address);
        assertEquals(64, address.length()); // SHA-256 hash is 64 chars
    }
}
