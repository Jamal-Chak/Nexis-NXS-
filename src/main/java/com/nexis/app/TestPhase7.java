package com.nexis.app;

import com.nexis.core.AccessControlManager;
import com.nexis.core.NetworkConfig;

public class TestPhase7 {
    public static void main(String[] args) {
        System.out.println("--- TESTING PHASE 7: ENTERPRISE & PRIVATE MODE ---");

        String validatorA = "VALIDATOR_KEY_A_123";
        String validatorB = "VALIDATOR_KEY_B_999";

        // 1. Test Public Mode (Default)
        System.out.println("\n[1] Testing Public Mode...");
        NetworkConfig.getInstance().setPrivateNetwork(false);
        if (AccessControlManager.isValidatorAllowed(validatorA)) {
            System.out.println("[PASS] Public mode allows anyone.");
        } else {
            System.out.println("[FAIL] Public mode blocked a validator.");
        }

        // 2. Test Private Mode
        System.out.println("\n[2] Testing Private Mode...");
        NetworkConfig.getInstance().setPrivateNetwork(true);
        
        // Neither is allowed yet
        if (!AccessControlManager.isValidatorAllowed(validatorA)) {
            System.out.println("[PASS] Private mode blocked unknown validator A.");
        } else {
            System.out.println("[FAIL] Private mode allowed unknown validator A.");
        }

        // 3. Allow-listing Validator A
        System.out.println("\n[3] Adding Validator A to Allow-list...");
        AccessControlManager.addValidator(validatorA);
        
        if (AccessControlManager.isValidatorAllowed(validatorA)) {
            System.out.println("[PASS] Allow-listed Validator A is now allowed.");
        } else {
            System.out.println("[FAIL] Allow-listed Validator A is still blocked.");
        }
        
        if (!AccessControlManager.isValidatorAllowed(validatorB)) {
            System.out.println("[PASS] Non-listed Validator B is still blocked.");
        } else {
            System.out.println("[FAIL] Non-listed Validator B is allowed!");
        }
        
        System.out.println("\n--- TEST COMPLETE ---");
    }
}
