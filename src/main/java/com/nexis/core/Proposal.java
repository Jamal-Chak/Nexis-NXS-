package com.nexis.core;

import java.util.HashMap;
import java.util.Map;

public class Proposal {
    public String id;
    public String description;
    public String proposer;
    public long endTimestamp;
    public Map<String, Double> votes = new HashMap<>(); // Address -> Vote Weight
    public boolean executed = false;

    public Proposal(String id, String description, String proposer, long durationMillis) {
        this.id = id;
        this.description = description;
        this.proposer = proposer;
        this.endTimestamp = System.currentTimeMillis() + durationMillis;
    }

    public double getTotalVotes() {
        return votes.values().stream().mapToDouble(Double::doubleValue).sum();
    }
}
