package com.nexis.app;

import com.nexis.core.Block;
import com.nexis.core.Blockchain;
import com.nexis.core.Transaction;
import com.nexis.wallet.Wallet;
import com.nexis.network.Node;
import com.nexis.core.NetworkConfig;
import com.nexis.core.AccessControlManager;

import java.util.ArrayList;
import java.util.List;

public class TestPhase6 {
    public static void main(String[] args) {
        System.out.println("--- TESTING PHASE 6: VALIDATION & SCALING ---");
        
        // 1. Setup Network (Public Mode for Stress Test)
        NetworkConfig.getInstance().setPrivateNetwork(false);
        Node node = new Node(8000); // Initialize a local node (no networking for this test)
        Blockchain chain = node.getBlockchain();
        
        Wallet miner = new Wallet();
        Wallet whale = new Wallet(); // Big spender
        
        // Miner needs to be approved if we were in private mode, but we are public.
        // Let's whitelist just in case.
        AccessControlManager.addValidator(miner.getPublicKeyString());
        
        // 2. Fund the Whale
        System.out.println("\n[1] Funding Whale Account...");
        Block genesis = chain.chain.get(0);
        // Mining a few blocks to get coins
        for (int i = 0; i < 5; i++) {
            chain.mineMempool(whale.publicKey); 
        }
        System.out.println("Whale Balance: " + chain.getBalance(whale.getAddress()) + " NXS");

        // 3. Stress Test: Generating Transactions
        int txCount = 1000;
        System.out.println("\n[2] Stress Testing: Generating " + txCount + " Transactions...");
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < txCount; i++) {
            // Random small amount, random fee
            double amount = Math.random() * 1.0;
            double fee = 0.01 + (Math.random() * 0.05); // Variable fees
            
            Transaction tx = whale.sendFunds(miner.publicKey, amount, fee); // Send back to miner (circular)
            chain.addTransaction(tx);
            
            if (i % 200 == 0) System.out.print(".");
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        System.out.println("\nGeneration Time: " + duration + "ms");
        System.out.println("Mempool Size: " + chain.mempool.size());
        
        // 4. Mining Under Load
        System.out.println("\n[3] Mining Blocks to Clear Mempool...");
        int initialHeight = chain.chain.size();
        int blocksMined = 0;
        
        long mineStart = System.currentTimeMillis();
        while (!chain.mempool.isEmpty()) {
            chain.mineMempool(miner.publicKey);
            blocksMined++;
            System.out.print("#");
        }
        long mineEnd = System.currentTimeMillis();
        
        System.out.println("\nCleared mempool in " + blocksMined + " blocks.");
        
        // 5. Results & Metrics
        double totalTimeSec = (mineEnd - mineStart) / 1000.0;
        double tps = txCount / totalTimeSec;
        
        System.out.println("\n--- PERFORMANCE METRICS ---");
        System.out.println("Throughput (TPS): " + String.format("%.2f", tps) + " tx/sec");
        System.out.println("Total Transactions: " + txCount);
        System.out.println("Total Blocks Mined: " + blocksMined);
        
        // Verify Revenue
        System.out.println("\n--- EARNINGS VERIFICATION ---");
        Block lastBlock = chain.getLastBlock();
        System.out.println("Last Block Reward: " + lastBlock.blockReward);
        System.out.println("Last Block Fees: " + lastBlock.totalFees);
        
        if (lastBlock.totalFees > 0) {
            System.out.println("[PASS] Fees were correctly collected under load.");
        } else {
            System.out.println("[FAIL] No fees found in the last block.");
        }
        
        System.out.println("\n--- STRESS TEST COMPLETE ---");
        System.exit(0);
    }
}
