package com.nexis.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MetricsEngine {

    private Blockchain blockchain;

    public MetricsEngine(Blockchain blockchain) {
        this.blockchain = blockchain;
    }

    public Map<String, Object> getBusinessSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        // 1. Transaction Metrics
        double totalFees = blockchain.revenueTracker.totalFeesAllTime;
        int totalTx = blockchain.revenueTracker.totalTxCountAllTime;
        double avgFee = (totalTx > 0) ? (totalFees / totalTx) : 0.0;
        
        summary.put("totalTransactions", totalTx);
        summary.put("averageFeePerTx", avgFee);
        
        // 2. Network Usage
        // In a real system, we'd query indexed data. Here we iterate (expensive but functional for MVP)
        int activeWallets = calculateActiveWallets();
        summary.put("activeWallets", activeWallets);
        
        // 3. Network Load (Simulated based on mempool vs theoretical max)
        double currentLoad = (double) blockchain.mempool.size() / 1000.0; // Assume 1000 tps max
        summary.put("networkLoad", Math.min(currentLoad, 1.0)); // Cap at 100%
        
        // 4. Validator Economics
        double totalRewards = blockchain.revenueTracker.totalRewardsAllTime;
        double totalRevenue = totalFees + totalRewards;
        summary.put("totalValidatorRevenue", totalRevenue);
        
        return summary;
    }
    
    // Expensive operation: iterates entire chain to find unique addresses
    private int calculateActiveWallets() {
        Set<String> activeAddresses = new HashSet<>();
        for (Block block : blockchain.chain) {
            for (Transaction tx : block.transactions) {
                if (tx.senderAddress != null) activeAddresses.add(tx.senderAddress);
                if (tx.recipientAddress != null) activeAddresses.add(tx.recipientAddress);
            }
        }
        return activeAddresses.size();
    }
}
