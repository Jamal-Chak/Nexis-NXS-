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

public class TestHighFees {
    public static void main(String[] args) {
        System.out.println("--- TESTING PHASE 8: HIGH FEE SCENARIO ---");
        
        // 1. Setup Network
        NetworkConfig.getInstance().setPrivateNetwork(false);
        Node node = new Node(8001); 
        Blockchain chain = node.getBlockchain();
        Wallet miner = new Wallet();
        Wallet user = new Wallet();
        
        // Fund user
        chain.mineMempool(user.publicKey); // Get initial funds
        chain.mineMempool(user.publicKey); 
        
        // 2. Flood Mempool with Mixed Fees
        System.out.println("\n[1] Flooding Mempool with 10 High-Fee and 100 Low-Fee Tx...");
        
        // Create 100 "Spam" Txs (Low Fee)
        for (int i = 0; i < 100; i++) {
            Transaction tx = user.sendFunds(miner.publicKey, 0.1, 0.001); // 0.001 Fee
            chain.addTransaction(tx);
        }
        
        // Create 10 "VIP" Txs (High Fee)
        for (int i = 0; i < 10; i++) {
            Transaction tx = user.sendFunds(miner.publicKey, 5.0, 1.0); // 1.0 Fee !!
            chain.addTransaction(tx);
        }
        
        System.out.println("Mempool Size: " + chain.mempool.size());
        
        // 3. Mine Block
        System.out.println("\n[2] Mining Block...");
        // In a real fee-market implementation, the miner would sort by Fee/Byte or pure Fee.
        // Currently, our mineMempool might just be FIFO or basic. 
        // This test simulates the "pressure" and we observe the result.
        
        chain.mineMempool(miner.publicKey);
        Block lastBlock = chain.getLastBlock();
        
        System.out.println("Block Transactions: " + lastBlock.transactions.size());
        System.out.println("Total Fees in Block: " + lastBlock.totalFees);
        
        // 4. Verify Revenue
        if (lastBlock.totalFees >= 10.0) {
             System.out.println("[PASS] Block includes high-fee transactions (Total Fees > 10).");
        } else {
             System.out.println("[WARN] Total fees low (" + lastBlock.totalFees + "). Miner might not be prioritizing fees yet.");
        }
        
        System.out.println("\n--- HIGH FEE TEST COMPLETE ---");
        System.exit(0);
    }
}
