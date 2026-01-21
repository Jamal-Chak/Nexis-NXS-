package com.nexis.app;

import com.nexis.core.Blockchain;
import com.nexis.core.FeeModel;
import com.nexis.core.Transaction;
import com.nexis.wallet.Wallet;

public class TestPhase2 {
    public static void main(String[] args) {
        System.out.println("--- TESTING PHASE 2: REVENUE TRACKING ---");

        // 1. Setup
        Blockchain chain = new Blockchain();
        Wallet miner = new Wallet();
        Wallet sender = new Wallet();
        Wallet receiver = new Wallet();

        // 2. Fund Sender
        chain.mineMempool(sender.publicKey); // Block 1 (Reward to sender)
        
        // 3. Generate Traffic (Fees)
        double totalFeesGenerated = 0;
        
        // Tx 1: Small fee
        double fee1 = FeeModel.calculateRequiredFee(256, 0.1); 
        Transaction tx1 = sender.sendFunds(receiver.publicKey, 10.0, fee1);
        chain.addTransaction(tx1);
        totalFeesGenerated += fee1;
        
        // Tx 2: Congestion fee simulation
        double fee2 = FeeModel.calculateRequiredFee(256, 0.9); // High congestion
        Transaction tx2 = sender.sendFunds(receiver.publicKey, 10.0, fee2);
        chain.addTransaction(tx2);
        totalFeesGenerated += fee2;
        
        System.out.println("Generated Fee 1: " + fee1);
        System.out.println("Generated Fee 2: " + fee2 + " (Congestion Multiplier)");
        
        // 4. Mine Block
        chain.mineMempool(miner.publicKey);
        
        // 5. Verify Revenue Tracker
        double trackedFees = chain.revenueTracker.totalFeesAllTime;
        double trackedRewards = chain.revenueTracker.totalRewardsAllTime;
        
        System.out.println("Tracked Total Fees: " + trackedFees);
        System.out.println("Expected Total Fees: " + totalFeesGenerated);
        
        if (Math.abs(trackedFees - totalFeesGenerated) < 0.001) {
            System.out.println("[PASS] RevenueTracker recorded correct fees.");
        } else {
            System.out.println("[FAIL] RevenueTracker fee mismatch.");
        }
        
        if (trackedRewards >= Blockchain.BLOCK_REWARD) {
            System.out.println("[PASS] RevenueTracker recorded rewards.");
        }
        
        // 6. Verify Persistence
        chain.revenueTracker.save();
        System.out.println("Saved revenue_stats.json. Check file existence.");
        
        // 7. Verify Daily Stats
        if (!chain.revenueTracker.dailyFees.isEmpty()) {
             System.out.println("[PASS] Daily stats populated.");
        } else {
             System.out.println("[FAIL] Daily stats empty.");
        }
    }
}
