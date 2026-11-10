package io.dataease.ai.support;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

/** Server-only immutable key ring. Never serialize or log its contents. */
public final class AiKeyMaterial {
    private final Map<String, byte[]> keys;
    private final String activeKey;
    private final String canonical;
    private final String fingerprint;

    private AiKeyMaterial(Map<String, byte[]> keys, String activeKey) {
        if (!keys.containsKey(activeKey)) throw AiErrors.error("key_unavailable");
        this.keys = Map.copyOf(keys);
        this.activeKey = activeKey;
        canonical = keys.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + ":" + Base64.getEncoder().encodeToString(entry.getValue()))
                .collect(Collectors.joining(","));
        try {
            fingerprint = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(("dataease-ai-keyring-v1\n" + activeKey + "\n" + canonical).getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw AiErrors.error("key_unavailable");
        }
    }

    public static AiKeyMaterial parse(String configured, String activeKey) {
        try {
            if (configured == null || configured.isBlank()
                    || activeKey == null || !activeKey.matches("[A-Za-z0-9_-]{1,32}")) throw new IllegalArgumentException();
            Map<String, byte[]> keys = new TreeMap<>();
            for (String item : configured.split(",", -1)) {
                String[] pair = item.trim().split(":", -1);
                if (pair.length != 2 || !pair[0].matches("[A-Za-z0-9_-]{1,32}")) throw new IllegalArgumentException();
                byte[] raw = Base64.getDecoder().decode(pair[1]);
                if (raw.length != 32 || keys.put(pair[0], raw) != null) throw new IllegalArgumentException();
            }
            return new AiKeyMaterial(keys, activeKey);
        } catch (RuntimeException e) {
            throw AiErrors.error("key_unavailable");
        }
    }

    public static AiKeyMaterial generate() {
        byte[] raw = new byte[32];
        new SecureRandom().nextBytes(raw);
        String id = "auto_" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
        return new AiKeyMaterial(Map.of(id, raw), id);
    }

    public String activeKey() { return activeKey; }
    public String canonical() { return canonical; }
    public String fingerprint() { return fingerprint; }
    public byte[] key(String id) {
        byte[] raw = keys.get(id);
        if (raw == null) throw AiErrors.error("key_unavailable");
        return raw.clone();
    }
    @Override public String toString() { return "AI key ring [redacted]"; }
}
