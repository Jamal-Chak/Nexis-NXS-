package com.nexis.consensus;

public class ProofOfWork {

    /**
     * Checks if the hash meets the difficulty requirement.
     * Difficulty 4 means the hash must start with "0000".
     */
    public static boolean isHashValid(String hash, int difficulty) {
        String target = new String(new char[difficulty]).replace('\0', '0');
        return hash.substring(0, difficulty).equals(target);
    }
}
