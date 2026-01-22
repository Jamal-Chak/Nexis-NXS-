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
            // Phase 2: Revenue Endpoints
            server.createContext("/api/revenue", new RevenueHandler());
            server.createContext("/api/costs", new CostsHandler());
            // Phase 3: Business Metrics Endpoint
            server.createContext("/api/business", new BusinessHandler());
            // Phase 7: Admin Config
            server.createContext("/api/admin/config", new AdminHandler());

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

    // Helper to enforce Rate Limits
    private boolean checkRateLimit(HttpExchange exchange) throws IOException {
        String apiKey = exchange.getRequestHeaders().getFirst("X-API-KEY");
        String remoteAddress = exchange.getRemoteAddress().getAddress().getHostAddress();

        // Use API Key if present, otherwise IP
        String clientId = (apiKey != null && !apiKey.isEmpty()) ? apiKey : remoteAddress;

        ApiKeyManager.Tier tier = ApiKeyManager.getTier(apiKey);
        int limit = ApiKeyManager.getRateLimit(tier);

        if (!RateLimiter.allowRequest(clientId, limit)) {
            String error = "{\"error\": \"Rate limit exceeded\", \"tier\": \"" + tier + "\"}";
            sendResponse(exchange, error.getBytes(), "application/json", 429);
            return false;
        }
        return true;
    }

    private class ChainHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!checkRateLimit(exchange))
                return;
            String response = gson.toJson(node.getBlockchain().chain);
            sendResponse(exchange, response, "application/json");
        }
    }

    private class MempoolHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!checkRateLimit(exchange))
                return;
            String response = gson.toJson(node.getBlockchain().mempool);
            sendResponse(exchange, response, "application/json");
        }
    }

    private class StatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!checkRateLimit(exchange))
                return;
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

    private class RevenueHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!checkRateLimit(exchange))
                return;
            // Return the entire RevenueTracker object as JSON
            String response = gson.toJson(node.getBlockchain().revenueTracker);
            sendResponse(exchange, response, "application/json");
        }
    }

    private class CostsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!checkRateLimit(exchange))
                return;
            Map<String, Object> costs = new HashMap<>();
            // Simulated cost data
            costs.put("costPerBlock", node.getBlockchain().revenueTracker.estimatedValidatorCostPerBlock);
            costs.put("currency", "USD (Simulated)");

            String response = gson.toJson(costs);
            sendResponse(exchange, response, "application/json");
        }
    }

    private class BusinessHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!checkRateLimit(exchange))
                return;
            // Instantiate MetricsEngine on the fly (or could be field)
            com.nexis.core.MetricsEngine engine = new com.nexis.core.MetricsEngine(node.getBlockchain());
            Map<String, Object> summary = engine.getBusinessSummary();

            String response = gson.toJson(summary);
            sendResponse(exchange, response, "application/json");
        }
    }

    private class AdminHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Security: Only localhost or Admin Key (Pending stricter impl)
            String apiKey = exchange.getRequestHeaders().getFirst("X-API-KEY");
            if (!"NEXIS_ADMIN_KEY_001".equals(apiKey)) {
                sendResponse(exchange, "Unauthorized", "text/plain", 401);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {
                // Simple toggle for MVP (In real app, parse JSON body)
                boolean current = com.nexis.core.NetworkConfig.getInstance().isPrivateNetwork();
                com.nexis.core.NetworkConfig.getInstance().setPrivateNetwork(!current);

                String msg = "Private Mode Toggled: " + !current;
                sendResponse(exchange, msg, "text/plain");
            } else {
                // GET config
                boolean isPrivate = com.nexis.core.NetworkConfig.getInstance().isPrivateNetwork();
                sendResponse(exchange, "{\"isPrivateNetwork\": " + isPrivate + "}", "application/json");
            }
        }
    }

    private class DashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!checkRateLimit(exchange))
                return;
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
        sendResponse(exchange, content, contentType, 200);
    }

    private void sendResponse(HttpExchange exchange, String response, String contentType, int statusCode)
            throws IOException {
        sendResponse(exchange, response.getBytes(), contentType, statusCode);
    }

    private void sendResponse(HttpExchange exchange, byte[] content, String contentType, int statusCode)
            throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, content.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(content);
        }
    }
}
