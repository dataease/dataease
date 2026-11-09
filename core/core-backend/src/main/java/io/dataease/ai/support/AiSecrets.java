package io.dataease.ai.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;

/** 独立服务端 key ring；无默认密钥，配置故障只阻止 AI 密钥操作。 */
@Component
public class AiSecrets {
    private final Map<String, byte[]> keys;
    private final String activeKey;
    private final ObjectMapper json;
    private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DEC = Base64.getUrlDecoder();
    public AiSecrets(@Value("${dataease.ai.keys:}") String configured,
                     @Value("${dataease.ai.active-key:}") String activeKey, ObjectMapper json) {
        this.activeKey = activeKey;
        this.json = json;
        Map<String, byte[]> parsed = new HashMap<>();
        try {
            if (!configured.isBlank()) {
                for (String item : configured.split(",")) {
                    String[] pair = item.trim().split(":", 2);
                    byte[] raw = Base64.getDecoder().decode(pair[1]);
                    if (!pair[0].matches("[A-Za-z0-9_-]{1,32}") || raw.length != 32
                            || parsed.put(pair[0], raw) != null) throw new IllegalArgumentException();
                }
            }
        } catch (RuntimeException e) {
            parsed.clear();
            org.slf4j.LoggerFactory.getLogger(AiSecrets.class).warn("AI server key ring is unavailable: invalid configuration");
        }
        keys = Map.copyOf(parsed);
    }
    private byte[] key(String id, String purpose) {
        byte[] raw = keys.get(id);
        if (raw == null) throw AiErrors.error("key_unavailable");
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(raw, "HmacSHA256"));
            return mac.doFinal(("dataease-ai-v1:" + purpose).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) { throw AiErrors.error("key_unavailable"); }
    }
    public String encrypt(String value, Long modelId) {
        try {
            byte[] nonce = new byte[12];
            new SecureRandom().nextBytes(nonce);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key(activeKey, "encryption"), "AES"), new GCMParameterSpec(128, nonce));
            cipher.updateAAD(("model:" + modelId).getBytes(StandardCharsets.UTF_8));
            return activeKey + "." + B64.encodeToString(nonce) + "." + B64.encodeToString(cipher.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) { throw AiErrors.error("key_unavailable"); }
    }
    public String decrypt(String envelope, Long modelId) {
        try {
            String[] parts = envelope.split("\\.");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key(parts[0], "encryption"), "AES"), new GCMParameterSpec(128, DEC.decode(parts[1])));
            cipher.updateAAD(("model:" + modelId).getBytes(StandardCharsets.UTF_8));
            return new String(cipher.doFinal(DEC.decode(parts[2])), StandardCharsets.UTF_8);
        } catch (Exception e) { throw AiErrors.error("key_unavailable"); }
    }
    private String hmac(String keyId, String purpose, byte[] data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key(keyId, purpose), "HmacSHA256"));
            return B64.encodeToString(mac.doFinal(data));
        } catch (Exception e) { throw AiErrors.error("key_unavailable"); }
    }
    public String fingerprint(String keyId, List<String> values) {
        try { return hmac(keyId, "connection", json.writeValueAsBytes(values)); }
        catch (Exception e) { throw AiErrors.error("key_unavailable"); }
    }
    public String activeKey() { return activeKey; }
    public String sign(String purpose, Map<String, Object> fields, long lifetimeMillis) {
        try {
            Map<String, Object> payload = new TreeMap<>(fields);
            long now = System.currentTimeMillis();
            payload.put("purpose", purpose); payload.put("v", 1);
            payload.put("iat", now); payload.put("exp", Math.addExact(now, lifetimeMillis));
            String encoded = B64.encodeToString(json.writeValueAsBytes(payload));
            String content = activeKey + "." + encoded;
            return content + "." + hmac(activeKey, "signature", content.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) { throw AiErrors.error("key_unavailable"); }
    }
    public Map<String, Object> verify(String token, String purpose) {
        try {
            if (token == null || token.length() > 8192) throw new IllegalArgumentException();
            String[] parts = token.split("\\.", -1);
            if (parts.length != 3) throw new IllegalArgumentException();
            String expected = hmac(parts[0], "signature", (parts[0] + "." + parts[1]).getBytes(StandardCharsets.UTF_8));
            if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII), parts[2].getBytes(StandardCharsets.US_ASCII))) throw new IllegalArgumentException();
            Map<String, Object> payload = json.readValue(DEC.decode(parts[1]), new TypeReference<>() { });
            long now = System.currentTimeMillis();
            if (!purpose.equals(payload.get("purpose")) || ((Number) payload.get("v")).intValue() != 1
                    || ((Number) payload.get("exp")).longValue() <= now
                    || ((Number) payload.get("iat")).longValue() > now + 5000) throw new IllegalArgumentException();
            return payload;
        } catch (Exception e) { throw AiErrors.error("proof_invalid"); }
    }
}
