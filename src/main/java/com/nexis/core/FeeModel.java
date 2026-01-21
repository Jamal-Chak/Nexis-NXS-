package com.nexis.core;

public class FeeModel {

    public static final double BASE_FEE = 0.1;
    public static final double MIN_FEE = 0.01;
    public static final double MAX_BLOCK_SIZE = 1000.0; // Simulated unit size

    // Simply calculate fee based on data size (simulated) and network congestion
    public static double calculateRequiredFee(int txSizeInBytes, double currentBlockFullnessPercent) {
        double congestionMultiplier = 1.0;
        
        if (currentBlockFullnessPercent > 0.8) {
             congestionMultiplier = 5.0; // High congestion
        } else if (currentBlockFullnessPercent > 0.5) {
             congestionMultiplier = 1.5; // Moderate congestion
        }

        // Fee = Base * SizeFactor * Congestion
        double sizeFactor = (double) txSizeInBytes / 256.0; // Assume avg tx is 256 bytes
        double fee = BASE_FEE * sizeFactor * congestionMultiplier;
        
        return Math.max(fee, MIN_FEE);
    }
}
