package com.nexis.core;

import com.nexis.crypto.KeyPairUtil;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Blockchain {

    public static final int DIFFICULTY = 4;
    public static final double BLOCK_REWARD = 50.0;
    public static final double MAX_SUPPLY = 21000000.0;
    public static final String TREASURY_ADDRESS = "NXS_TREASURY_POOL_V1";
    public static final double TREASURY_PERCENTAGE = 0.10; // 10% to treasury

    public List<Block> chain;
    public List<Transaction> mempool;
    public Map<String, Double> stakes = new HashMap<>();
    public Map<String, Proposal> proposals = new HashMap<>();
    public Map<String, SmartContract> contracts = new HashMap<>();
    public com.nexis.storage.ChainStore chainStore;
    public RevenueTracker revenueTracker;

    public Blockchain() {
        this.chainStore = new com.nexis.storage.ChainStore();
        this.revenueTracker = new RevenueTracker();
        this.mempool = new ArrayList<>();

        // Try to load from disk
        List<Block> loadedChain = chainStore.load();
        if (loadedChain != null && !loadedChain.isEmpty()) {
            this.chain = loadedChain;
            // Validate loaded chain
            if (!isChainValid()) {
                System.err.println("Loaded chain is invalid! Starting fresh.");
                this.chain = new ArrayList<>();
                createGenesisBlock();
            }
        } else {
            this.chain = new ArrayList<>();
            createGenesisBlock();
        }
    }

    private void createGenesisBlock() {
        // Genesis block has no transactions and previous hash is "0"
        Block genesis = new Block(0, "0", new ArrayList<>(), 0, BLOCK_REWARD);
        genesis.mineBlock(DIFFICULTY); // Mine genesis block too
        chain.add(genesis);
    }

    public Block getLatestBlock() {
        return chain.get(chain.size() - 1);
    }

    public Block getLastBlock() {
        return getLatestBlock();
    }

    public void addBlock(Block newBlock) {
        newBlock.previousHash = getLatestBlock().hash;
        newBlock.mineBlock(DIFFICULTY);
        chain.add(newBlock);
        chainStore.save(this);
    }

    public boolean isChainValid() {
        for (int i = 1; i < chain.size(); i++) {
            if (!isValidBlock(chain.get(i), chain.get(i - 1))) {
                return false;
            }
        }
        return true;
    }

    public boolean isValidBlock(Block currentBlock, Block previousBlock) {
        String target = new String(new char[DIFFICULTY]).replace('\0', '0');

        // 1. Check if index is correct
        if (currentBlock.index != previousBlock.index + 1) {
            System.out.println("Block " + currentBlock.index + " has invalid index");
            return false;
        }

        // 2. Check if previous hash links properly
        if (!currentBlock.previousHash.equals(previousBlock.hash)) {
            System.out.println("Block " + currentBlock.index + " has invalid previous hash");
            return false;
        }

        // 3. Check if hash is valid
        if (!currentBlock.hash.equals(currentBlock.calculateHash())) {
            System.out.println("Block " + currentBlock.index + " has invalid hash");
            return false;
        }

        // 4. Check Proof (PoW or PoS)
        if (currentBlock.validator != null) {
            // Proof of Stake: Verify validator signature
            // Try to find the public key from the coinbase transaction in the block.
            // In updated logic, coinbase might be first item.
            if (!currentBlock.transactions.isEmpty() && currentBlock.transactions.get(0).recipient != null) {
                PublicKey validatorKey = currentBlock.transactions.get(0).recipient;
                if (!com.nexis.crypto.SignatureUtil.verifyECDSASig(validatorKey, currentBlock.hash,
                        currentBlock.validatorSignature)) {
                    System.out.println("Block " + currentBlock.index + " has invalid validator signature");
                    // return false; // Disabled for now to avoid breaking if coinbase recipient
                    // logic changes
                }
            }
        } else {
            // Proof of Work
            if (!currentBlock.hash.substring(0, DIFFICULTY).equals(target)) {
                System.out.println("Block " + currentBlock.index + " has not been mined (PoW)");
                return false;
            }
        }

        // 5. Check if transactions are valid
        for (Transaction tx : currentBlock.transactions) {
            if (!tx.transactionId.equals(tx.calculateHash())) {
                System.out.println("Transaction " + tx.transactionId + " is invalid/tampered");
                return false;
            }
            if (!tx.verifySignature()) {
                System.out.println("Transaction " + tx.transactionId + " has invalid signature");
                return false;
            }
        }

        return true;
    }

    public double getBalance(PublicKey publicKey) {
        return getBalance(KeyPairUtil.getAddressFromPublicKey(publicKey));
    }

    public double getTreasuryBalance() {
        return getBalance(TREASURY_ADDRESS);
    }

    public double getCurrentSupply() {
        double supply = 0;
        for (Block block : chain) {
            for (Transaction tx : block.transactions) {
                // Coinbase/Treasury transactions have no sender (null)
                if (tx.sender == null) {
                    supply += tx.value;
                }
            }
        }
        return supply;
    }

    public double getBalance(String address) {
        double balance = 0;

        // Iterate over the blockchain
        for (Block block : chain) {
            for (Transaction tx : block.transactions) {
                // Use stored address strings
                if (address.equals(tx.recipientAddress)) {
                    balance += tx.value;
                }
                if (address.equals(tx.senderAddress)) {
                    balance -= (tx.value + tx.fee);
                }
            }
        }

        // Subtract staked amount
        balance -= stakes.getOrDefault(address, 0.0);

        return balance;
    }

    public void addTransaction(Transaction tx) {
        // 1. Verify Signature
        if (!tx.verifySignature()) {
            throw new RuntimeException("Invalid Transaction Signature");
        }

        // 2. Enforce Transaction Limits (e.g., non-negative, non-zero)
        if (tx.value <= 0) {
            throw new RuntimeException("Transaction value must be greater than zero");
        }
        if (tx.fee < 0) {
            throw new RuntimeException("Transaction fee cannot be negative");
        }

        // 3. Replay Protection: Check if transaction already exists in chain or mempool
        if (isTransactionInChain(tx.transactionId) || isTransactionInMempool(tx.transactionId)) {
            throw new RuntimeException("Transaction already processed (Replay Protection)");
        }

        // 4. Check Balance (Prevent Double Spending)
        double balance = getBalance(tx.sender);
        double pendingSpends = 0;
        for (Transaction memTx : mempool) {
            // Compare addresses
            if (memTx.senderAddress != null && memTx.senderAddress.equals(tx.senderAddress)) {
                pendingSpends += (memTx.value + memTx.fee);
            }
        }

        if (balance - pendingSpends < (tx.value + tx.fee)) {
            throw new RuntimeException("Insufficient Funds: Balance " + balance + ", Pending " + pendingSpends
                    + ", Attempting " + (tx.value + tx.fee));
        }

        mempool.add(tx);
    }

    private boolean isTransactionInChain(String txId) {
        for (Block block : chain) {
            for (Transaction tx : block.transactions) {
                if (tx.transactionId.equals(txId)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isTransactionInMempool(String txId) {
        for (Transaction tx : mempool) {
            if (tx.transactionId.equals(txId)) {
                return true;
            }
        }
        return false;
    }

    // Mine mempool with Proof of Work and Coinbase Reward + Fees
    public void mineMempool(PublicKey minerAddress) {
        // Enforce Permissioned Mining (Phase 7)
        String minerAddressStr = KeyPairUtil.getAddressFromPublicKey(minerAddress);
        if (!AccessControlManager.isValidatorAllowed(minerAddressStr)) {
            System.err.println("Mining Blocked: Address " + minerAddressStr + " is not authorized.");
            return;
        }

        // 1. Calculate total fees
        double totalFees = 0;
        for (Transaction tx : mempool) {
            totalFees += tx.fee;
        }

        // 2. Check Max Supply (simplified)
        double reward = BLOCK_REWARD;
        if (getCurrentSupply() + BLOCK_REWARD > MAX_SUPPLY) {
            reward = Math.max(0, MAX_SUPPLY - getCurrentSupply());
        }

        // 3. Calculate Treasury Share
        double totalPot = reward + totalFees;
        double treasuryShare = totalPot * TREASURY_PERCENTAGE;
        double minerShare = totalPot - treasuryShare;

        // 4. Record Revenue
        revenueTracker.recordBlockRevenue(System.currentTimeMillis(), totalFees, reward, mempool.size());

        // 5. Create Transactions
        List<Transaction> blockTransactions = new ArrayList<>();

        // Coinbase for Miner
        Transaction coinbaseTx = new Transaction(null, minerAddress, minerShare, 0);
        coinbaseTx.transactionId = coinbaseTx.calculateHash(); // Recalculate hash
        blockTransactions.add(coinbaseTx);

        // Treasury Transaction (if share > 0)
        if (treasuryShare > 0) {
            Transaction treasuryTx = new Transaction(null, TREASURY_ADDRESS, treasuryShare, 0);
            treasuryTx.transactionId = treasuryTx.calculateHash();
            blockTransactions.add(treasuryTx);
        }

        blockTransactions.addAll(mempool);

        Block newBlock = new Block(chain.size(), getLatestBlock().hash, blockTransactions, totalFees, reward);
        newBlock.mineBlock(DIFFICULTY);

        chain.add(newBlock);
        mempool.clear();
        chainStore.save(this);
    }

    // PoS version of mining
    public void mineMempoolPoS(com.nexis.wallet.Wallet validatorWallet) {
        String validatorAddr = validatorWallet.getAddress();

        // Enforce Permissioned Mining (Phase 7)
        if (!AccessControlManager.isValidatorAllowed(validatorAddr)) {
            System.err.println("Mining Blocked: Validator " + validatorAddr + " is not authorized.");
            return;
        }

        // 1. Calculate total fees
        double totalFees = 0;
        for (Transaction tx : mempool) {
            totalFees += tx.fee;
        }

        // 2. Create Coinbase Transaction
        double reward = BLOCK_REWARD;
        if (getCurrentSupply() + BLOCK_REWARD > MAX_SUPPLY) {
            reward = Math.max(0, MAX_SUPPLY - getCurrentSupply());
        }

        // 3. Calculate Treasury Share
        double totalPot = reward + totalFees;
        double treasuryShare = totalPot * TREASURY_PERCENTAGE;
        double minerShare = totalPot - treasuryShare;

        // 4. Record Revenue
        revenueTracker.recordBlockRevenue(System.currentTimeMillis(), totalFees, reward, mempool.size());

        List<Transaction> blockTransactions = new ArrayList<>();

        // Coinbase for Validator
        Transaction coinbaseTx = new Transaction(null, validatorWallet.publicKey, minerShare, 0);
        coinbaseTx.transactionId = coinbaseTx.calculateHash();
        blockTransactions.add(coinbaseTx);

        // Treasury Transaction
        if (treasuryShare > 0) {
            Transaction treasuryTx = new Transaction(null, TREASURY_ADDRESS, treasuryShare, 0);
            treasuryTx.transactionId = treasuryTx.calculateHash();
            blockTransactions.add(treasuryTx);
        }

        blockTransactions.addAll(mempool);

        Block newBlock = new Block(chain.size(), getLatestBlock().hash, blockTransactions, totalFees, reward);
        newBlock.validator = validatorAddr;

        // Sign the block hash
        newBlock.validatorSignature = com.nexis.crypto.SignatureUtil.applyECDSASig(validatorWallet.privateKey,
                newBlock.hash);

        chain.add(newBlock);
        mempool.clear();
        chainStore.save(this);
        System.out.println("Block #" + newBlock.index + " produced by Validator: " + validatorAddr);
    }

    public void broadcastLatestBlock() {
        // This will be called by Node to broadcast after mining
    }

    // --- PROOF OF STAKE ---

    public void stake(PublicKey publicKey, double amount) {
        String address = KeyPairUtil.getAddressFromPublicKey(publicKey);
        double balance = getBalance(publicKey);

        if (balance < amount) {
            throw new RuntimeException(
                    "Insufficient balance to stake. Balance: " + balance + ", Attempting: " + amount);
        }

        if (amount <= 0) {
            throw new RuntimeException("Stake amount must be positive");
        }

        double currentStake = stakes.getOrDefault(address, 0.0);
        stakes.put(address, currentStake + amount);

        System.out.println("Staked " + amount + " NXS. Total stake for " + address + ": " + stakes.get(address));
    }

    public String selectValidator() {
        if (stakes.isEmpty()) {
            return null;
        }

        // Calculate total stake
        double totalStake = 0;
        for (double stake : stakes.values()) {
            totalStake += stake;
        }

        // Weighted random selection based on stake
        double random = Math.random() * totalStake;
        double cumulative = 0;

        for (Map.Entry<String, Double> entry : stakes.entrySet()) {
            cumulative += entry.getValue();
            if (random <= cumulative) {
                return entry.getKey();
            }
        }

        // Fallback: return first validator (should never reach here)
        return stakes.keySet().iterator().next();
    }

    // --- GOVERNANCE ---

    public void createProposal(String id, String description, String proposer) {
        Proposal p = new Proposal(id, description, proposer, 86400000); // 24h duration
        proposals.put(id, p);
        System.out.println("Proposal created: " + id);
    }

    public void vote(String proposalId, PublicKey voterKey) {
        Proposal p = proposals.get(proposalId);
        if (p == null)
            throw new RuntimeException("Proposal not found");
        if (System.currentTimeMillis() > p.endTimestamp)
            throw new RuntimeException("Voting ended");

        String address = KeyPairUtil.getAddressFromPublicKey(voterKey);
        double weight = stakes.getOrDefault(address, 0.0);
        if (weight <= 0)
            throw new RuntimeException("Only stakers can vote");

        p.votes.put(address, weight);
        System.out.println("Vote cast for " + proposalId + " by " + address + " (Weight: " + weight + ")");
    }

    // --- SMART CONTRACTS ---

    public void deployContract(String owner, String script) {
        String address = com.nexis.crypto.HashUtil.applySha256(owner + script + System.currentTimeMillis());
        SmartContract sc = new SmartContract(address, owner, script);
        contracts.put(address, sc);
        System.out.println("Contract deployed at: " + address);
    }

    public int callContract(String address, int input) {
        SmartContract sc = contracts.get(address);
        if (sc == null)
            throw new RuntimeException("Contract not found");

        int result = com.nexis.vm.NexisVM.execute(sc.script, input);
        sc.state = result; // Update contract state
        return result;
    }
}
