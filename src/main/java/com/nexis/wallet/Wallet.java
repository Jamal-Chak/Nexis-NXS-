package com.nexis.wallet;

import com.nexis.core.Transaction;
import com.nexis.crypto.KeyPairUtil;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

public class Wallet {

    public PrivateKey privateKey;
    public PublicKey publicKey;

    public Wallet() {
        generateKeyPair();
    }

    public void generateKeyPair() {
        KeyPair keyPair = KeyPairUtil.generateKeyPair();
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();
    }

    public Transaction sendFunds(PublicKey recipient, double value, double fee) {
        // In a real scenario, we would check balance here.
        // For now, we just create a transaction and sign it.

        Transaction newTransaction = new Transaction(publicKey, recipient, value, fee);
        newTransaction.generateSignature(privateKey);

        return newTransaction;
    }

    public String getAddress() {
        return KeyPairUtil.getAddressFromPublicKey(this.publicKey);
    }
}
