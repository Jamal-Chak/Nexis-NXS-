package com.nexis.core;

public class NetworkConfig {
    
    // Singleton instance
    private static final NetworkConfig INSTANCE = new NetworkConfig();

    // Configuration Fields
    private boolean isPrivateNetwork = false;
    private double minTransactionFee = 0.01;
    private int targetBlockTimeSeconds = 10;

    private NetworkConfig() {
        // Private constructor
    }

    public static NetworkConfig getInstance() {
        return INSTANCE;
    }

    public boolean isPrivateNetwork() {
        return isPrivateNetwork;
    }

    public void setPrivateNetwork(boolean privateNetwork) {
        isPrivateNetwork = privateNetwork;
    }

    public double getMinTransactionFee() {
        return minTransactionFee;
    }

    public void setMinTransactionFee(double minTransactionFee) {
        this.minTransactionFee = minTransactionFee;
    }
}
