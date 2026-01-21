package com.nexis.network;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {

    // Storage: ClientID -> (Counter, ResetTime)
    private static final Map<String, RateLimitEntry> limits = new ConcurrentHashMap<>();
    
    private static class RateLimitEntry {
        int count;
        long resetTime;
        
        RateLimitEntry(long resetTime) {
            this.count = 0;
            this.resetTime = resetTime;
        }
    }

    public static synchronized boolean allowRequest(String clientId, int maxRequestsPerMinute) {
        long now = System.currentTimeMillis();
        
        RateLimitEntry entry = limits.computeIfAbsent(clientId, k -> new RateLimitEntry(now + 60000));
        
        // Reset window if expired
        if (now > entry.resetTime) {
            entry.count = 0;
            entry.resetTime = now + 60000;
        }
        
        if (entry.count < maxRequestsPerMinute) {
            entry.count++;
            return true;
        }
        
        return false;
    }
}
