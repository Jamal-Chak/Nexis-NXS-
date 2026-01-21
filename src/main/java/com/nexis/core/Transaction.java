package com.nexis.core;

import com.nexis.crypto.HashUtil;
import com.nexis.crypto.KeyPairUtil;
import com.nexis.crypto.SignatureUtil;

import java.security.PrivateKey;
import java.security.PublicKey;

public class Transaction {

    public String transactionId; // Hash of the transaction
    public PublicKey sender; // Senders public key
    public PublicKey recipient; // Recipients public key
    public String senderAddress; // Sender address (String)
    public String recipientAddress; // Recipient address (String)
    public double value;
    public double fee;
    public long timeStamp;
    public byte[] signature; // Signature to prevent tampering

    // Constructor with PublicKeys
    public Transaction(PublicKey from, PublicKey to, double value, double fee) {
        this.sender = from;
        this.recipient = to;
        this.senderAddress = (from != null) ? KeyPairUtil.getAddressFromPublicKey(from) : null;
        this.recipientAddress = (to != null) ? KeyPairUtil.getAddressFromPublicKey(to) : null;
        this.value = value;
        this.fee = fee;
        this.timeStamp = System.currentTimeMillis();
        this.transactionId = calculateHash();
    }

    // Constructor with String Recipient Address (for Treasury/Burn)
    public Transaction(PublicKey from, String toAddress, double value, double fee) {
        this.sender = from;
        this.recipient = null; // We might not know the PK
        this.senderAddress = (from != null) ? KeyPairUtil.getAddressFromPublicKey(from) : null;
        this.recipientAddress = toAddress;
        this.value = value;
        this.fee = fee;
        this.timeStamp = System.currentTimeMillis();
        this.transactionId = calculateHash();
    }

    // Calculate the transaction hash (ID)
    public String calculateHash() {
        // Hash: SenderAddr + RecipientAddr + Value + Fee + Timestamp
        return HashUtil.applySha256(
                (senderAddress != null ? senderAddress : "") +
                        (recipientAddress != null ? recipientAddress : "") +
                        Double.toString(value) +
                        Double.toString(fee) +
                        Long.toString(timeStamp));
    }

    // Sign the transaction with the private key
    public void generateSignature(PrivateKey privateKey) {
        if (sender == null)
            return; // Coinbase transactions are not signed by a sender

        String data = senderAddress +
                recipientAddress +
                Double.toString(value) +
                Double.toString(fee) +
                Long.toString(timeStamp);

        this.signature = SignatureUtil.applyECDSASig(privateKey, data);
    }

    // Verify the transaction signature
    public boolean verifySignature() {
        if (sender == null)
            return true; // Coinbase transactions are valid without signature

        String data = senderAddress +
                recipientAddress +
                Double.toString(value) +
                Double.toString(fee) +
                Long.toString(timeStamp);

        return SignatureUtil.verifyECDSASig(sender, data, signature);
    }
}
