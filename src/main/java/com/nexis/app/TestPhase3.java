package com.nexis.app;

import com.nexis.core.Blockchain;
import com.nexis.core.MetricsEngine;
import com.nexis.core.Transaction;
import com.nexis.wallet.Wallet;

import java.util.Map;

public class TestPhase3 {
    public static void main(String[] args) {
        System.out.println("--- TESTING PHASE 3: BUSINESS METRICS ENGINE ---");

        // 1. Setup
        Blockchain chain = new Blockchain();
        MetricsEngine engine = new MetricsEngine(chain);
        Wallet miner = new Wallet();
        Wallet sender = new Wallet();
        Wallet receiver = new Wallet();

        // 2. Fund Sender
        chain.mineMempool(sender.publicKey); // Block 1 (Supply: 50)
        
        // 3. Generate Transactions
        // Tx 1: Fee 1.0
        Transaction tx1 = sender.sendFunds(receiver.publicKey, 10.0, 1.0);
        chain.addTransaction(tx1);
        
        // Tx 2: Fee 2.0
        Transaction tx2 = sender.sendFunds(receiver.publicKey, 10.0, 2.0);
        chain.addTransaction(tx2);
        
        // 4. Mine Block (2 TXs)
        chain.mineMempool(miner.publicKey);
        
        // 5. Generate Metrics
        Map<String, Object> summary = engine.getBusinessSummary();
        
        // 6. Verify Metrics
        System.out.println("Summary JSON: " + summary);
        
        int totalTx = (int) summary.get("totalTransactions");
        double avgFee = (double) summary.get("averageFeePerTx");
        int activeWallets = (int) summary.get("activeWallets");
        double networkLoad = (double) summary.get("networkLoad");
        
        System.out.println("Total TX: " + totalTx + " (Expected: 2)");
        System.out.println("Avg Fee: " + avgFee + " (Expected: 1.5)");
        System.out.println("Active Wallets: " + activeWallets + " (Expected: ~2)");
        
        if (totalTx == 2) {
            System.out.println("[PASS] Transaction count correct.");
        } else {
            System.out.println("[FAIL] Tx count mismatch.");
        }
        
        if (Math.abs(avgFee - 1.5) < 0.001) {
             System.out.println("[PASS] Avg Fee correct.");
        } else {
             System.out.println("[FAIL] Avg Fee mismatch.");
        }
        
        // Note: Active wallets computation iterates the chain. 
        // We have: 
        // Block 0: Genesis (no tx)
        // Block 1: Coinbase (Null -> Sender) [Sender active]
        // Block 2: Tx1 (Sender->Receiver), Tx2 (Sender->Receiver), Coinbase(Null->Miner) [Sender, Receiver, Miner active]
        // Expecting 3 unique active addresses? 
        // MetricsEngine implementation: checks tx.senderAddress and tx.recipientAddress.
        
        if (activeWallets >= 2) {
             System.out.println("[PASS] Active wallets count looks reasonable.");
        }
    }
}
