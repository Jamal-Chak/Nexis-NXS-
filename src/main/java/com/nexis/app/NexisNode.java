package com.nexis.app;

import com.nexis.core.Block;
import com.nexis.core.Blockchain;
import com.nexis.core.Transaction;
import com.nexis.crypto.KeyPairUtil;
import com.nexis.network.Node;
import com.nexis.wallet.Wallet;

import java.util.Scanner;

public class NexisNode {

    private static Node node;
    private static Wallet wallet;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("       NEXIS (NXS) BLOCKCHAIN NODE      ");
        System.out.println("========================================");

        System.out.print("Enter port for this node (e.g., 5000): ");
        int port = Integer.parseInt(scanner.nextLine());

        node = new Node(port);
        node.start();

        // Start Dashboard Server (Port + 3000)
        int dashboardPort = port + 3000;
        new com.nexis.network.HttpApiServer(node, dashboardPort).start();

        System.out.print("Enter peer port to connect to (or press Enter to skip): ");
        String peerPortStr = scanner.nextLine();
        if (!peerPortStr.isEmpty()) {
            try {
                int peerPort = Integer.parseInt(peerPortStr);
                node.connectToPeer("localhost", peerPort);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port. Skipping connection.");
            }
        }

        // Initialize a default wallet for the user
        wallet = new Wallet();
        System.out.println("\nYour Wallet Address: " + KeyPairUtil.getAddressFromPublicKey(wallet.publicKey));

        boolean running = true;
        while (running) {
            printMenu();
            if (!scanner.hasNextLine()) {
                running = false;
                break;
            }
            String choice = scanner.nextLine().toLowerCase();

            switch (choice) {
                case "balance":
                    showBalance();
                    break;
                case "send":
                    sendNXS();
                    break;
                case "mine":
                    mine();
                    break;
                case "chain":
                    showChain();
                    break;
                case "peers":
                    showPeers();
                    break;
                case "stake":
                    stake();
                    break;
                case "pos_mine":
                    posMine();
                    break;
                case "propose":
                    propose();
                    break;
                case "vote":
                    vote();
                    break;
                case "deploy":
                    deploy();
                    break;
                case "call":
                    call();
                    break;
                case "bridge":
                    bridge();
                    break;
                case "wallet":
                    showWalletInfo();
                    break;
                case "exit":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }

        System.out.println("Shutting down Nexis Node...");
        System.exit(0);
    }

    private static void printMenu() {
        System.out.println("\n--- MENU ---");
        System.out.println("balance  - Check your balance");
        System.out.println("send     - Send NXS to an address");
        System.out.println("mine     - Mine (PoW)");
        System.out.println("stake    - Stake NXS to become validator");
        System.out.println("pos_mine - Produce block (PoS)");
        System.out.println("propose  - Create a governance proposal");
        System.out.println("vote     - Vote on a proposal");
        System.out.println("deploy   - Deploy a smart contract");
        System.out.println("call     - Call a smart contract");
        System.out.println("bridge   - Bridge NXS to TwineEngine");
        System.out.println("chain    - View the blockchain");
        System.out.println("peers    - View connected peers");
        System.out.println("wallet   - View your wallet info");
        System.out.println("exit     - Exit");
        System.out.print("> ");
    }

    private static void showBalance() {
        double balance = node.getBlockchain().getBalance(wallet.publicKey);
        System.out.println("Current Balance: " + balance + " NXS");
    }

    private static void sendNXS() {
        System.out.print("Enter recipient address (Public Key String): ");
        String recipientAddr = scanner.nextLine();
        System.out.print("Enter amount: ");
        try {
            double amount = Double.parseDouble(scanner.nextLine());
            System.out.print("Enter fee (default 0.1): ");
            String feeStr = scanner.nextLine();
            double fee = feeStr.isEmpty() ? 0.1 : Double.parseDouble(feeStr);

            java.security.PublicKey recipientKey = KeyPairUtil.getPublicKeyFromString(recipientAddr);

            Transaction tx = wallet.sendFunds(recipientKey, amount, fee);
            node.getBlockchain().addTransaction(tx);

            // Broadcast the transaction
            node.broadcastTransaction(tx);

            System.out.println("Transaction added to mempool and broadcasted!");
        } catch (Exception e) {
            System.err.println("Error sending NXS: " + e.getMessage());
        }
    }

    private static void mine() {
        System.out.println("Mining started (PoW)...");
        node.mineMempool(wallet.publicKey);
        System.out.println("Mining complete! Reward sent to your wallet.");
    }

    private static void stake() {
        System.out.print("Enter amount to stake: ");
        try {
            double amount = Double.parseDouble(scanner.nextLine());
            node.getBlockchain().stake(wallet.publicKey, amount);
            System.out.println("Successfully staked " + amount + " NXS.");
        } catch (Exception e) {
            System.err.println("Error staking: " + e.getMessage());
        }
    }

    private static void posMine() {
        System.out.println("Attempting to produce block (PoS)...");
        String selectedValidator = node.getBlockchain().selectValidator();
        if (selectedValidator == null) {
            System.out.println("No validators available. Please stake first.");
            return;
        }

        System.out.println("Selected Validator: " + selectedValidator);
        if (selectedValidator.equals(wallet.getAddress())) {
            node.getBlockchain().mineMempoolPoS(wallet);
            node.getPeerManager().broadcastLatestBlock();
            System.out.println("You produced a block! Reward sent to your wallet.");
        } else {
            System.out.println("You were not selected for this block. (Wait for your turn)");
        }
    }

    private static void propose() {
        System.out.print("Enter proposal ID: ");
        String id = scanner.nextLine();
        System.out.print("Enter description: ");
        String desc = scanner.nextLine();
        node.getBlockchain().createProposal(id, desc, wallet.getAddress());
    }

    private static void vote() {
        System.out.print("Enter proposal ID to vote on: ");
        String id = scanner.nextLine();
        try {
            node.getBlockchain().vote(id, wallet.publicKey);
        } catch (Exception e) {
            System.err.println("Voting failed: " + e.getMessage());
        }
    }

    private static void deploy() {
        System.out.println("Enter contract script (e.g., 'PUSH:10 PUSH:20 ADD'): ");
        String script = scanner.nextLine();
        node.getBlockchain().deployContract(wallet.getAddress(), script);
    }

    private static void call() {
        System.out.print("Enter contract address: ");
        String addr = scanner.nextLine();
        System.out.print("Enter input value (integer): ");
        try {
            int input = Integer.parseInt(scanner.nextLine());
            int result = node.getBlockchain().callContract(addr, input);
            System.out.println("Contract Result: " + result);
        } catch (Exception e) {
            System.err.println("Contract call failed: " + e.getMessage());
        }
    }

    private static void bridge() {
        System.out.print("Enter amount to bridge to TwineEngine: ");
        try {
            double amount = Double.parseDouble(scanner.nextLine());
            double balance = node.getBlockchain().getBalance(wallet.publicKey);
            if (balance < amount) {
                System.out.println("Insufficient balance.");
                return;
            }
            // Simulated bridge: Burn on Nexis, Mint on TwineEngine (Conceptual)
            System.out.println("Bridging " + amount + " NXS to TwineEngine...");
            System.out.println("[SUCCESS] Assets locked on Nexis. Minting TW-NXS on TwineEngine ecosystem.");
        } catch (Exception e) {
            System.err.println("Bridge failed: " + e.getMessage());
        }
    }

    private static void showChain() {
        Blockchain chain = node.getBlockchain();
        System.out.println("\n--- BLOCKCHAIN ---");
        for (Block block : chain.chain) {
            System.out.println("Block #" + block.index);
            System.out.println("  Hash: " + block.hash);
            System.out.println("  Prev: " + block.previousHash);
            System.out.println("  TXs:  " + block.transactions.size());
            if (block.validator != null) {
                System.out.println("  Validator: " + block.validator);
            }
        }
    }

    private static void showPeers() {
        int count = node.getPeerManager().getPeerCount();
        System.out.println("Connected to " + count + " peers.");
    }

    private static void showWalletInfo() {
        System.out.println("Public Key: " + KeyPairUtil.getStringFromKey(wallet.publicKey));
        System.out.println("Address:    " + KeyPairUtil.getAddressFromPublicKey(wallet.publicKey));
    }
}
