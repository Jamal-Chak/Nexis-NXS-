package com.nexis.app;

import com.nexis.core.Block;
import com.nexis.core.Blockchain;
import com.nexis.core.Transaction;
import com.nexis.crypto.HashUtil;
import com.nexis.crypto.KeyPairUtil;
import com.nexis.crypto.SignatureUtil;
import com.nexis.network.Node;
import com.nexis.wallet.Wallet;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

public class ManualTest {

    public static void main(String[] args) {
        System.out.println("Running Manual Crypto Tests...");

        try {
            testSha256();
            testKeyPair();
            testSignature();
            testTransaction();
            testBlock();
            testBlockchain();
            testNetworking();
            System.out.println("ALL TESTS PASSED");
            System.exit(0); // Force exit to stop server threads
        } catch (Exception e) {
            System.err.println("TEST FAILED: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void testSha256() {
        String input = "Nexis";
        String hash = HashUtil.applySha256(input);
        if (hash == null || hash.length() != 64) {
            throw new RuntimeException("SHA-256 failed. Hash: " + hash);
        }
        System.out.println("[PASS] SHA-256");
    }

    private static void testKeyPair() {
        KeyPair keyPair = KeyPairUtil.generateKeyPair();
        if (keyPair.getPrivate() == null || keyPair.getPublic() == null) {
            throw new RuntimeException("KeyPair generation failed");
        }

        String address = KeyPairUtil.getAddressFromPublicKey(keyPair.getPublic());
        if (address == null || address.length() != 64) {
            throw new RuntimeException("Address generation failed");
        }
        System.out.println("[PASS] KeyPair & Address");
    }

    private static void testSignature() {
        KeyPair keyPair = KeyPairUtil.generateKeyPair();
        String data = "Test Data";

        byte[] signature = SignatureUtil.applyECDSASig(keyPair.getPrivate(), data);
        boolean valid = SignatureUtil.verifyECDSASig(keyPair.getPublic(), data, signature);

        if (!valid) {
            throw new RuntimeException("Signature verification failed");
        }

        boolean invalid = SignatureUtil.verifyECDSASig(keyPair.getPublic(), data + "tamper", signature);
        if (invalid) {
            throw new RuntimeException("Tampered data should not verify");
        }
        System.out.println("[PASS] Signature");
    }

    private static void testTransaction() {
        Wallet walletA = new Wallet();
        Wallet walletB = new Wallet();

        Transaction tx = walletA.sendFunds(walletB.publicKey, 100, 0.1);

        if (!tx.verifySignature()) {
            throw new RuntimeException("Transaction signature failed to verify");
        }

        // Tamper with transaction
        tx.value = 1000;
        if (tx.verifySignature()) {
            throw new RuntimeException("Tampered transaction should not verify");
        }

        System.out.println("[PASS] Transaction");
    }

    private static void testBlock() {
        Wallet walletA = new Wallet();
        Wallet walletB = new Wallet();
        Transaction tx1 = walletA.sendFunds(walletB.publicKey, 10, 0.1);
        Transaction tx2 = walletB.sendFunds(walletA.publicKey, 5, 0.1);

        List<Transaction> txs = new ArrayList<>();
        txs.add(tx1);
        txs.add(tx2);

        Block block = new Block(1, "0", txs);

        if (block.hash == null || block.hash.length() != 64) {
            throw new RuntimeException("Block hash generation failed");
        }

        System.out.println("[PASS] Block");
    }

    private static void testBlockchain() {
        // Clean up previous test data
        java.io.File chainFile = new java.io.File("nexis_chain.json");
        if (chainFile.exists()) {
            chainFile.delete();
        }

        Blockchain nexis = new Blockchain();
        Wallet whale = new Wallet();
        Wallet user = new Wallet();
        Wallet miner = new Wallet();

        // 1. Seed the whale wallet (Manual Block)
        Wallet system = new Wallet();
        Transaction genesisTx = new Transaction(system.publicKey, whale.publicKey, 1000, 0);
        genesisTx.generateSignature(system.privateKey);

        List<Transaction> txs = new ArrayList<>();
        txs.add(genesisTx);
        Block b1 = new Block(1, nexis.getLatestBlock().hash, txs);
        nexis.addBlock(b1);

        if (nexis.getBalance(whale.publicKey) != 1000) {
            throw new RuntimeException(
                    "Balance tracking failed. Expected 1000, got " + nexis.getBalance(whale.publicKey));
        }

        // 2. Whale sends to User
        Transaction tx1 = whale.sendFunds(user.publicKey, 100, 1.0);
        nexis.addTransaction(tx1);

        // Mine block (Miner gets reward)
        nexis.mineMempool(miner.publicKey);

        if (nexis.getBalance(whale.publicKey) != 899) { // 1000 - 100 - 1.0 fee
            throw new RuntimeException("Sender balance failed. Expected 899, got " + nexis.getBalance(whale.publicKey));
        }
        if (nexis.getBalance(user.publicKey) != 100) {
            throw new RuntimeException(
                    "Recipient balance failed. Expected 100, got " + nexis.getBalance(user.publicKey));
        }
        if (nexis.getBalance(miner.publicKey) != 51) { // 50 reward + 1.0 fee
            throw new RuntimeException("Miner reward failed. Expected 51, got " + nexis.getBalance(miner.publicKey));
        }

        // 3. Invalid Transaction (Double Spend)
        try {
            Transaction tx2 = whale.sendFunds(user.publicKey, 2000, 0.1); // Only has 899
            nexis.addTransaction(tx2);
            throw new RuntimeException("Double spend should have failed");
        } catch (RuntimeException e) {
            // Expected
            if (!e.getMessage().contains("Insufficient Funds")) {
                throw e;
            }
        }

        // 4. Validate Chain
        if (!nexis.isChainValid()) {
            throw new RuntimeException("Chain should be valid");
        }

        // 5. Tamper Chain
        nexis.chain.get(1).transactions.get(0).value = 5000; // Whale gave himself more money
        if (nexis.isChainValid()) {
            throw new RuntimeException("Tampered chain should be invalid");
        }

        System.out.println("[PASS] Blockchain & Mining");
    }

    private static void testNetworking() throws InterruptedException {
        System.out.println("Starting Networking Test...");

        // Clean up
        new java.io.File("nexis_chain.json").delete();

        Node node1 = new Node(5000);
        Node node2 = new Node(5001);

        node1.start();
        node2.start();

        Thread.sleep(1000); // Wait for servers to start

        // Connect node2 to node1
        node2.connectToPeer("localhost", 5000);

        Thread.sleep(1000); // Wait for connection

        // Mine a block on Node 1
        Wallet miner = new Wallet();
        node1.mineMempool(miner.publicKey);

        Thread.sleep(3000); // Wait for sync

        // Check if Node 2 has the block
        // Node 1: Genesis + Mined Block = 2 blocks
        // Node 2: Genesis (starts fresh) + Synced Block = 2 blocks
        if (node2.getBlockchain().chain.size() != node1.getBlockchain().chain.size()) {
            throw new RuntimeException("Chain sync failed. Node 1 size: " + node1.getBlockchain().chain.size()
                    + ", Node 2 size: " + node2.getBlockchain().chain.size());
        }

        System.out.println("[PASS] Networking & Sync");
    }
}
