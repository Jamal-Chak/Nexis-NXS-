package com.nexis.core;

import java.util.HashSet;
import java.util.Set;

public class AccessControlManager {

    private static final Set<String> ALLOWED_VALIDATORS = new HashSet<>();
    private static final Set<String> ADMIN_KEYS = new HashSet<>();

    static {
        // Pre-seed an admin key for testing
        // In reality, this would be loaded from a secure config file
        ADMIN_KEYS.add("NEXIS_ADMIN_KEY_001"); 
    }

    // --- Validator Management ---

    public static void addValidator(String publicKey) {
        ALLOWED_VALIDATORS.add(publicKey);
    }

    public static void removeValidator(String publicKey) {
        ALLOWED_VALIDATORS.remove(publicKey);
    }

    public static boolean isValidatorAllowed(String publicKey) {
        // If network is PUBLIC, everyone is allowed
        if (!NetworkConfig.getInstance().isPrivateNetwork()) {
            return true;
        }
        // If PRIVATE, only allow-listed keys
        return ALLOWED_VALIDATORS.contains(publicKey);
    }

    // --- Admin Management ---

    public static boolean isAdmin(String key) {
        return ADMIN_KEYS.contains(key);
    }
}
