package com.nexis.core;

import com.nexis.crypto.HashUtil;
import java.util.ArrayList;
import java.util.List;

public class Block {

    public int index;
    public long timestamp;
    public List<Transaction> transactions = new ArrayList<>();
    public String previousHash;
    public String hash;
    public int nonce;
    public String validator; // Address of the validator who produced this block
    public byte[] validatorSignature; // Signature of the validator

    public Block(int index, String previousHash, List<Transaction> transactions) {
        this.index = index;
        this.previousHash = previousHash;
        this.transactions = transactions;
        // Use a fixed timestamp for the genesis block to ensure deterministic hash
        // across nodes
        this.timestamp = (index == 0) ? 1737180000000L : System.currentTimeMillis();
        this.hash = calculateHash();
    }

    public String calculateHash() {
        StringBuilder transactionsData = new StringBuilder();
        for (Transaction tx : transactions) {
            transactionsData.append(tx.transactionId);
        }

        return HashUtil.applySha256(
                Integer.toString(index) +
                        previousHash +
                        Long.toString(timestamp) +
                        transactionsData.toString() +
                        Integer.toString(nonce));
    }

    public void mineBlock(int difficulty) {
        String target = new String(new char[difficulty]).replace('\0', '0');
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
        System.out.println("Block Mined!!! : " + hash);
    }
}
