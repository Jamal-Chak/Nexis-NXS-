package com.nexis.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RevenueTracker {

    private static final String STATS_FILE = "revenue_stats.json";
    
    // Revenue Data
    public double totalFeesAllTime;
    public double totalRewardsAllTime;
    
    // Daily Metrics (Key: "YYYY-MM-DD")
    public Map<String, Double> dailyFees = new HashMap<>();
    public Map<String, Double> dailyRewards = new HashMap<>();
    public Map<String, Integer> dailyTxCount = new HashMap<>();
    
    public int totalTxCountAllTime = 0;
    
    // Validator Cost Simulation (Stored here for simplicity)
    public double estimatedValidatorCostPerBlock = 0.05; // Simulated electricity/hardware cost

    private transient Gson gson; // Don't serialize the Gson instance itself

    public RevenueTracker() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        load();
    }

    public synchronized void recordBlockRevenue(long timestamp, double fees, double reward, int txCount) {
        totalFeesAllTime += fees;
        totalRewardsAllTime += reward;
        totalTxCountAllTime += txCount;
        
        String dateKey = convertTimestampToDate(timestamp);
        
        dailyFees.put(dateKey, dailyFees.getOrDefault(dateKey, 0.0) + fees);
        dailyRewards.put(dateKey, dailyRewards.getOrDefault(dateKey, 0.0) + reward);
        dailyTxCount.put(dateKey, dailyTxCount.getOrDefault(dateKey, 0) + txCount);
        
        save();
    }
    
    private String convertTimestampToDate(long timestamp) {
        // Simple manual conversion to avoid heavy dependencies if regular Java Date/Time is okay
        // Using java.time (Java 8+)
        java.time.Instant instant = java.time.Instant.ofEpochMilli(timestamp);
        java.time.LocalDate date = instant.atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        return date.toString(); // YYYY-MM-DD
    }

    public void save() {
        try (FileWriter writer = new FileWriter(STATS_FILE)) {
            gson.toJson(this, writer);
        } catch (IOException e) {
            System.err.println("Failed to save revenue stats: " + e.getMessage());
        }
    }

    public void load() {
        File file = new File(STATS_FILE);
        if (!file.exists()) {
            return;
        }

        try (FileReader reader = new FileReader(STATS_FILE)) {
            RevenueTracker loaded = gson.fromJson(reader, RevenueTracker.class);
            if (loaded != null) {
                this.totalFeesAllTime = loaded.totalFeesAllTime;
                this.totalRewardsAllTime = loaded.totalRewardsAllTime;
                this.dailyFees = loaded.dailyFees;
                this.dailyRewards = loaded.dailyRewards;
                this.estimatedValidatorCostPerBlock = loaded.estimatedValidatorCostPerBlock;
            }
        } catch (IOException e) {
            System.err.println("Failed to load revenue stats: " + e.getMessage());
        }
    }
    
    // --- Business Metrics Getters ---
    
    public double getNetValidatorProfit() {
        // Total Income - Cost
        // Assume cost is per block. We need total block count. 
        // For accurate calculation we'd need block count passed in, but we can stick to raw revenue for now.
        return (totalFeesAllTime + totalRewardsAllTime);
    }
}
