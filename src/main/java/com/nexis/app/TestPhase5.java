package com.nexis.app;

import com.nexis.network.ApiKeyManager;
import com.nexis.network.RateLimiter;

public class TestPhase5 {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("--- TESTING PHASE 5: MONETIZATION ENABLEMENT ---");

        // 1. Test Tier Resolution
        ApiKeyManager.Tier freeTier = ApiKeyManager.getTier(null);
        ApiKeyManager.Tier proTier = ApiKeyManager.getTier("NEXIS_PRO_KEY_123");
        
        System.out.println("Null Key Tier: " + freeTier + " (Expected: FREE)");
        System.out.println("Pro Key Tier: " + proTier + " (Expected: PRO)");
        
        if (freeTier == ApiKeyManager.Tier.FREE && proTier == ApiKeyManager.Tier.PRO) {
            System.out.println("[PASS] Tier resolution correct.");
        } else {
            System.out.println("[FAIL] Tier resolution incorrect.");
        }

        // 2. Test Rate Limiter (Simulated)
        String clientIP = "192.168.1.1";
        int limit = 5; // Low limit for test
        
        System.out.println("\nTesting Rate Limiter (Limit: " + limit + ")...");
        
        for (int i = 1; i <= limit + 2; i++) {
            boolean allowed = RateLimiter.allowRequest(clientIP, limit);
            System.out.println("Request " + i + ": " + (allowed ? "Allowed" : "Blocked"));
            
            if (i <= limit && !allowed) System.out.println("[FAIL] Request should be allowed");
            if (i > limit && allowed) System.out.println("[FAIL] Request should be blocked");
        }
        
        // 3. Test Pro Key (Higher Limit)
        System.out.println("\nTesting Pro Key...");
        String proClient = "NEXIS_PRO_KEY_123";
        int proLimit = 1000;
        if (RateLimiter.allowRequest(proClient, proLimit)) {
             System.out.println("[PASS] Pro key request allowed.");
        } else {
             System.out.println("[FAIL] Pro key request blocked unexpectedly.");
        }
    }
}
