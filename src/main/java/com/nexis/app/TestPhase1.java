package com.nexis.app;

import com.nexis.core.Block;
import com.nexis.core.Blockchain;
import com.nexis.core.Transaction;
import com.nexis.wallet.Wallet;

public class TestPhase1 {
    public static void main(String[] args) {
        System.out.println("--- TESTING PHASE 1: REVENUE MECHANICS ---");

        // 1. Setup
        Blockchain chain = new Blockchain();
        System.out.println("Blockchain initialized. Current height: " + chain.chain.size());
        
        Wallet miner = new Wallet();
        Wallet user1 = new Wallet();
        Wallet user2 = new Wallet();
        
        System.out.println("Miner Address: " + miner.getAddress());
        System.out.println("User1 Address: " + user1.getAddress());
        System.out.println("Treasury Address: " + Blockchain.TREASURY_ADDRESS);
        
        // 2. Fund User1 (Mine a few blocks)
        System.out.println("\n--- MINING TO FUND USER1 ---");
        chain.mineMempool(user1.publicKey); // Block 1
        chain.mineMempool(user1.publicKey); // Block 2
        
        double user1Balance = chain.getBalance(user1.publicKey);
        System.out.println("User1 Balance: " + user1Balance);
        
        // 3. Create Transaction with Fee
        System.out.println("\n--- EXECUTING TRANSACTION WITH FEE ---");
        double sendAmount = 100.0;
        double fee = 5.0;
        Transaction tx = user1.sendFunds(user2.publicKey, sendAmount, fee);
        chain.addTransaction(tx);
        
        // 4. Mine the block with the transaction
        chain.mineMempool(miner.publicKey);
        
        // 5. Verify Balances & Treasury
        Block latestBlock = chain.getLatestBlock();
        System.out.println("\n--- VERIFICATION ---");
        System.out.println("Latest Block Index: " + latestBlock.index);
        System.out.println("Block Total Fees: " + latestBlock.totalFees + " (Expected: " + fee + ")");
        System.out.println("Block Reward: " + latestBlock.blockReward + " (Expected: " + Blockchain.BLOCK_REWARD + ")");
        
        double totalPot = Blockchain.BLOCK_REWARD + fee;
        double expectedTreasury = totalPot * Blockchain.TREASURY_PERCENTAGE;
        double expectedMiner = totalPot - expectedTreasury;
        
        System.out.println("Total Pot: " + totalPot);
        System.out.println("Expected Miner Share: " + expectedMiner);
        System.out.println("Expected Treasury Share: " + expectedTreasury);
        
        double actualMinerBalance = chain.getBalance(miner.publicKey);
        double actualTreasuryBalance = chain.getTreasuryBalance();
        
        System.out.println("Actual Miner Balance: " + actualMinerBalance);
        System.out.println("Actual Treasury Balance: " + actualTreasuryBalance);
        
        if (Math.abs(actualTreasuryBalance - expectedTreasury) < 0.001) {
            System.out.println("[PASS] Treasury Balance is correct.");
        } else {
            System.out.println("[FAIL] Treasury Balance incorrect.");
        }
        
        if (Math.abs(actualMinerBalance - expectedMiner) < 0.001) {
             System.out.println("[PASS] Miner Balance is correct.");
        } else {
             System.out.println("[FAIL] Miner Balance incorrect (Note: Miner might have previous balance if reused).");
        }
    }
}
