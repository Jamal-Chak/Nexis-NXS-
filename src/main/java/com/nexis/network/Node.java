package com.nexis.network;

import com.nexis.core.Blockchain;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Node {

    private int port;
    private Blockchain blockchain;
    private PeerManager peerManager;
    private boolean running;

    public Node(int port) {
        this.port = port;
        this.blockchain = new Blockchain();
        this.peerManager = new PeerManager(this);
        this.running = false;
    }

    public void start() {
        this.running = true;

        // Start Server Thread
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("Nexis Node started on port " + port);
                while (running) {
                    Socket clientSocket = serverSocket.accept();
                    peerManager.handleConnection(clientSocket);
                }
            } catch (IOException e) {
                System.err.println("Server error: " + e.getMessage());
            }
        }).start();
    }

    public void connectToPeer(String host, int port) {
        try {
            Socket socket = new Socket(host, port);
            peerManager.handleConnection(socket);
            System.out.println("Connected to peer: " + host + ":" + port);
        } catch (IOException e) {
            System.err.println("Failed to connect to peer: " + e.getMessage());
        }
    }

    public Blockchain getBlockchain() {
        return blockchain;
    }

    public int getPort() {
        return port;
    }

    public PeerManager getPeerManager() {
        return peerManager;
    }

    public void mineMempool(java.security.PublicKey minerAddress) {
        blockchain.mineMempool(minerAddress);
        // Broadcast the new block to all peers
        peerManager.broadcastLatestBlock();
    }

    public void broadcastTransaction(com.nexis.core.Transaction tx) {
        peerManager.broadcastTransaction(tx);
    }
}
