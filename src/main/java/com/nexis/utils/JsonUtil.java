package com.nexis.utils;

import com.google.gson.*;
import com.nexis.crypto.KeyPairUtil;
import java.lang.reflect.Type;
import java.security.PublicKey;

public class JsonUtil {

    public static Gson getGson() {
        return new GsonBuilder()
                .registerTypeHierarchyAdapter(PublicKey.class, new PublicKeyAdapter())
                .create();
    }

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
