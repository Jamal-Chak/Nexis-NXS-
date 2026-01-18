package com.nexis.storage;

import com.google.gson.*;
import com.nexis.core.Block;
import com.nexis.core.Blockchain;
import com.nexis.crypto.KeyPairUtil;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class ChainStore {

    private static final String CHAIN_FILE = "nexis_chain.json";

    private Gson gson;

    public ChainStore() {
        this.gson = new GsonBuilder()
                .registerTypeHierarchyAdapter(PublicKey.class, new PublicKeyAdapter())
                .setPrettyPrinting()
                .create();
    }

    public void save(Blockchain blockchain) {
        try (FileWriter writer = new FileWriter(CHAIN_FILE)) {
            gson.toJson(blockchain.chain, writer);
            System.out.println("Blockchain saved to " + CHAIN_FILE);
        } catch (IOException e) {
            System.err.println("Failed to save blockchain: " + e.getMessage());
        }
    }

    public List<Block> load() {
        File file = new File(CHAIN_FILE);
        if (!file.exists()) {
            System.out.println("No existing blockchain found. Starting fresh.");
            return null;
        }

        try (FileReader reader = new FileReader(CHAIN_FILE)) {
            Type listType = new com.google.gson.reflect.TypeToken<ArrayList<Block>>() {
            }.getType();
            List<Block> chain = gson.fromJson(reader, listType);
            System.out.println("Blockchain loaded from " + CHAIN_FILE);
            return chain;
        } catch (IOException e) {
            System.err.println("Failed to load blockchain: " + e.getMessage());
            return null;
        }
    }

    // Type Adapter for PublicKey
    private static class PublicKeyAdapter implements JsonSerializer<PublicKey>, JsonDeserializer<PublicKey> {
        @Override
        public JsonElement serialize(PublicKey src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(KeyPairUtil.getStringFromKey(src));
        }

        @Override
        public PublicKey deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            String hex = json.getAsString();
            if (hex == null || hex.isEmpty())
                return null;
            return KeyPairUtil.getPublicKeyFromString(hex);
        }
    }
}
