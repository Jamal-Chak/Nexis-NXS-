package com.nexis.network;

import com.google.gson.Gson;
import com.nexis.utils.JsonUtil;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PeerManager {

    private Node node;
    private List<MessageHandler> peers;

    public PeerManager(Node node) {
        this.node = node;
        this.peers = new CopyOnWriteArrayList<>();
    }

    public void handleConnection(Socket socket) {
        MessageHandler handler = new MessageHandler(socket, node, this);
        peers.add(handler);
        new Thread(handler).start();

        // Query the latest block from the new peer
        Gson gson = new Gson();
        MessageHandler.Message queryMsg = new MessageHandler.Message("QUERY_LATEST", "");
        handler.sendMessage(gson.toJson(queryMsg));
    }

    public void broadcast(String message) {
        for (MessageHandler peer : peers) {
            peer.sendMessage(message);
        }
    }

    public void broadcastLatestBlock() {
        System.out.println("[DEBUG] Broadcasting latest block to " + peers.size() + " peers");
        Gson gson = JsonUtil.getGson();
        String blockJson = gson.toJson(List.of(node.getBlockchain().getLatestBlock()));
        MessageHandler.Message msg = new MessageHandler.Message("RESPONSE_BLOCKCHAIN", blockJson);
        broadcast(gson.toJson(msg));
    }

    public void broadcastTransaction(com.nexis.core.Transaction tx) {
        System.out.println("[DEBUG] Broadcasting transaction to " + peers.size() + " peers");
        Gson gson = JsonUtil.getGson();
        String txJson = gson.toJson(tx);
        MessageHandler.Message msg = new MessageHandler.Message("BROADCAST_TRANSACTION", txJson);
        broadcast(gson.toJson(msg));
    }

    public int getPeerCount() {
        return peers.size();
    }

    public void removePeer(MessageHandler peer) {
        peers.remove(peer);
    }
}
