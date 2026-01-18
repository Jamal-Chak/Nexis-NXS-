package com.nexis.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nexis.core.Blockchain;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class HttpApiServer {

    private final Node node;
    private final int port;
    private final Gson gson;

    public HttpApiServer(Node node, int port) {
        this.node = node;
        this.port = port;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void start() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            // API Endpoints
            server.createContext("/api/chain", new ChainHandler());
            server.createContext("/api/mempool", new MempoolHandler());
            server.createContext("/api/stats", new StatsHandler());

            // Serve Dashboard (Static HTML)
            server.createContext("/", new DashboardHandler());

            server.setExecutor(null);
            server.start();
            System.out.println("========================================");
            System.out.println("   NEXIS DASHBOARD: http://localhost:" + port);
            System.out.println("========================================");
        } catch (IOException e) {
            System.err.println("Failed to start HTTP server: " + e.getMessage());
        }
    }

    private class ChainHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = gson.toJson(node.getBlockchain().chain);
            sendResponse(exchange, response, "application/json");
        }
    }

    private class MempoolHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = gson.toJson(node.getBlockchain().mempool);
            sendResponse(exchange, response, "application/json");
        }
    }

    private class StatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Blockchain chain = node.getBlockchain();
            Map<String, Object> stats = new HashMap<>();
            stats.put("height", chain.chain.size());
            stats.put("supply", chain.getCurrentSupply());
            stats.put("mempoolSize", chain.mempool.size());
            stats.put("peerCount", node.getPeerManager().getPeerCount());
            stats.put("port", node.getPort());

            String response = gson.toJson(stats);
            sendResponse(exchange, response, "application/json");
        }
    }

    private class DashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                path = "/index.html";
            }

            // Try to serve from dashboard/dist first (Modern React Dashboard)
            java.io.File file = new java.io.File("dashboard/dist" + path);
            if (!file.exists()) {
                // Fallback to legacy dashboard in resources
                file = new java.io.File("src/main/resources/dashboard" + path);
            }

            if (file.exists() && !file.isDirectory()) {
                String contentType = getContentType(path);
                byte[] content = Files.readAllBytes(file.toPath());
                sendResponse(exchange, content, contentType);
            } else {
                exchange.sendResponseHeaders(404, -1);
            }
        }
    }

    private String getContentType(String path) {
        if (path.endsWith(".html"))
            return "text/html";
        if (path.endsWith(".js"))
            return "application/javascript";
        if (path.endsWith(".css"))
            return "text/css";
        if (path.endsWith(".svg"))
            return "image/svg+xml";
        if (path.endsWith(".png"))
            return "image/png";
        return "text/plain";
    }

    private void sendResponse(HttpExchange exchange, String response, String contentType) throws IOException {
        sendResponse(exchange, response.getBytes(), contentType);
    }

    private void sendResponse(HttpExchange exchange, byte[] content, String contentType) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, content.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(content);
        }
    }
}
