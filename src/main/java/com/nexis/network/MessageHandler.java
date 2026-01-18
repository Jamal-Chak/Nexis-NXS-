package com.nexis.network;

import com.google.gson.Gson;
import com.nexis.core.Block;
import com.nexis.core.Blockchain;
import com.nexis.utils.JsonUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.List;

public class MessageHandler implements Runnable {

    private Socket socket;
    private Node node;
    private PeerManager peerManager;
    private PrintWriter out;
    private BufferedReader in;
    private Gson gson;

    public MessageHandler(Socket socket, Node node, PeerManager peerManager) {
        this.socket = socket;
        this.node = node;
        this.peerManager = peerManager;
        this.gson = JsonUtil.getGson();
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                handleMessage(inputLine);
            }
        } catch (IOException e) {
            // System.out.println("Peer disconnected: " + socket.getInetAddress());
        } finally {
            peerManager.removePeer(this);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    private void handleMessage(String json) {
        try {
            Message message = gson.fromJson(json, Message.class);
            if (message == null || message.type == null) {
                System.err.println("Received malformed message");
                return;
            }
            Blockchain blockchain = node.getBlockchain();

            switch (message.type) {
                case "QUERY_LATEST":
                    sendResponse("RESPONSE_BLOCKCHAIN", gson.toJson(List.of(blockchain.getLatestBlock())));
                    break;

                case "QUERY_ALL":
                    sendResponse("RESPONSE_BLOCKCHAIN", gson.toJson(blockchain.chain));
                    break;

                case "RESPONSE_BLOCKCHAIN":
                    handleBlockchainResponse(message.data);
                    break;

                case "BROADCAST_TRANSACTION":
                    handleTransactionBroadcast(message.data);
                    break;

                default:
                    System.out.println("Unknown message type: " + message.type);
            }
        } catch (Exception e) {
            System.err.println("Error handling message: " + e.getMessage());
        }
    }

    private void handleTransactionBroadcast(String data) {
        com.nexis.core.Transaction tx = gson.fromJson(data, com.nexis.core.Transaction.class);
        if (tx == null)
            return;

        try {
            node.getBlockchain().addTransaction(tx);
            System.out.println("Received and added transaction: " + tx.transactionId);
            // Re-broadcast to other peers
            peerManager.broadcast(gson.toJson(new Message("BROADCAST_TRANSACTION", data)));
        } catch (Exception e) {
            // Transaction might already be in mempool or invalid
            // System.out.println("Transaction rejected: " + e.getMessage());
        }
    }

    private void handleBlockchainResponse(String data) {
        Type listType = new com.google.gson.reflect.TypeToken<java.util.ArrayList<Block>>() {
        }.getType();
        List<Block> receivedBlocks = gson.fromJson(data, listType);

        if (receivedBlocks == null || receivedBlocks.isEmpty())
            return;

        Blockchain blockchain = node.getBlockchain();
        Block latestBlockReceived = receivedBlocks.get(receivedBlocks.size() - 1);
        Block latestBlockHeld = blockchain.getLatestBlock();

        if (latestBlockReceived.index > latestBlockHeld.index) {
            System.out.println("Received longer chain. Current: " + latestBlockHeld.index + ", Received: "
                    + latestBlockReceived.index);
            if (latestBlockHeld.hash.equals(latestBlockReceived.previousHash)) {
                // We are behind by one block - validate and append it
                if (blockchain.isValidBlock(latestBlockReceived, latestBlockHeld)) {
                    System.out.println("Appending new block...");
                    blockchain.chain.add(latestBlockReceived);
                    blockchain.chainStore.save(blockchain);
                    // Broadcast to others
                    peerManager.broadcast(
                            gson.toJson(new Message("RESPONSE_BLOCKCHAIN", gson.toJson(List.of(latestBlockReceived)))));
                } else {
                    System.err.println("Received block is invalid!");
                }
            } else if (receivedBlocks.size() == 1) {
                // We received one block but it doesn't link. Query full chain.
                System.out.println("Querying full chain...");
                sendMessage(gson.toJson(new Message("QUERY_ALL", "")));
            } else {
                // Received full chain, replace if valid
                System.out.println("Replacing chain...");
                replaceChain(receivedBlocks);
            }
        } else {
            System.out.println("Received chain is not longer. Ignoring.");
        }
    }

    private void replaceChain(List<Block> newBlocks) {
        Blockchain blockchain = node.getBlockchain();
        // 1. Validate Genesis Block (must match our genesis)
        if (!newBlocks.get(0).hash.equals(blockchain.chain.get(0).hash)) {
            System.err.println("Received chain has different genesis block. Rejecting.");
            return;
        }

        // 2. Validate entire chain
        for (int i = 1; i < newBlocks.size(); i++) {
            if (!blockchain.isValidBlock(newBlocks.get(i), newBlocks.get(i - 1))) {
                System.err.println("Received chain is invalid at block " + i + ". Rejecting.");
                return;
            }
        }

        // 3. Replace chain
        blockchain.chain = newBlocks;
        blockchain.chainStore.save(blockchain);
        System.out.println("Chain replaced successfully.");
    }

    private void sendResponse(String type, String data) {
        Message msg = new Message(type, data);
        sendMessage(gson.toJson(msg));
    }

    // Inner class for message structure
    public static class Message {
        String type;
        String data;

        public Message(String type, String data) {
            this.type = type;
            this.data = data;
        }
    }
}
