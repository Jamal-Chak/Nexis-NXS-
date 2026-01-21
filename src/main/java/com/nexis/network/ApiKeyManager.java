package com.nexis.network;

import java.util.HashMap;
import java.util.Map;

public class ApiKeyManager {

    public enum Tier {
        FREE, PRO, ENTERPRISE
    }

    private static final Map<String, Tier> apiKeys = new HashMap<>();

    static {
        // Pre-seed some keys for testing
        apiKeys.put("NEXIS_PRO_KEY_123", Tier.PRO);
        apiKeys.put("NEXIS_ENT_KEY_999", Tier.ENTERPRISE);
    }

    public static Tier getTier(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            return Tier.FREE;
        }
        return apiKeys.getOrDefault(apiKey, Tier.FREE);
    }

    public static int getRateLimit(Tier tier) {
        switch (tier) {
            case PRO:
                return 1000; // 1000 req/min
            case ENTERPRISE:
                return Integer.MAX_VALUE; // Unlimited
            case FREE:
            default:
                return 60; // 60 req/min
        }
    }
}
