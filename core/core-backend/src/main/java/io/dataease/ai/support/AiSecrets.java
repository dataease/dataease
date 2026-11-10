package io.dataease.ai.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.ai.manage.AiKeyringManage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;

/** 独立服务端 key ring；每次操作核对共享状态，配置故障只阻止 AI 密钥操作。 */
@Component
public class AiSecrets {
    private final java.util.function.Supplier<AiKeyMaterial> material;
    private final ObjectMapper json;
    private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DEC = Base64.getUrlDecoder();

    @Autowired
    public AiSecrets(AiKeyringManage keyring, ObjectMapper json) {
        this.material = keyring::current;
        this.json = json;
    }

    /** Explicit isolated key ring for regressions and legacy ciphertext validation. */
    public AiSecrets(String configured, String activeKey, ObjectMapper json) {
        this.material = () -> AiKeyMaterial.parse(configured, activeKey);
        this.json = json;
    }

    private byte[] key(AiKeyMaterial ring, String id, String purpose) {
        byte[] raw = ring.key(id);
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(raw, "HmacSHA256"));
            return mac.doFinal(("dataease-ai-v1:" + purpose).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) { throw AiErrors.error("key_unavailable"); }
    }
    public String encrypt(String value, Long modelId) {
        try {
            AiKeyMaterial ring = material.get();
            byte[] nonce = new byte[12];
            new SecureRandom().nextBytes(nonce);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key(ring, ring.activeKey(), "encryption"), "AES"), new GCMParameterSpec(128, nonce));
            cipher.updateAAD(("model:" + modelId).getBytes(StandardCharsets.UTF_8));
            return ring.activeKey() + "." + B64.encodeToString(nonce) + "." + B64.encodeToString(cipher.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) { throw AiErrors.error("key_unavailable"); }
    }
    public String decrypt(String envelope, Long modelId) {
        try {
            String[] parts = envelope.split("\\.");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key(material.get(), parts[0], "encryption"), "AES"), new GCMParameterSpec(128, DEC.decode(parts[1])));
            cipher.updateAAD(("model:" + modelId).getBytes(StandardCharsets.UTF_8));
            return new String(cipher.doFinal(DEC.decode(parts[2])), StandardCharsets.UTF_8);
        } catch (Exception e) { throw AiErrors.error("key_unavailable"); }
    }
    private String hmac(AiKeyMaterial ring, String keyId, String purpose, byte[] data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key(ring, keyId, purpose), "HmacSHA256"));
            return B64.encodeToString(mac.doFinal(data));
        } catch (Exception e) { throw AiErrors.error("key_unavailable"); }
    }
    public String fingerprint(String keyId, List<String> values) {
        try { return hmac(material.get(), keyId, "connection", json.writeValueAsBytes(values)); }
        catch (Exception e) { throw AiErrors.error("key_unavailable"); }
    }
    public String activeKey() { return material.get().activeKey(); }
    public String sign(String purpose, Map<String, Object> fields, long lifetimeMillis) {
        try {
            AiKeyMaterial ring = material.get();
            Map<String, Object> payload = new TreeMap<>(fields);
            long now = System.currentTimeMillis();
            payload.put("purpose", purpose); payload.put("v", 1);
            payload.put("iat", now); payload.put("exp", Math.addExact(now, lifetimeMillis));
            String encoded = B64.encodeToString(json.writeValueAsBytes(payload));
            String content = ring.activeKey() + "." + encoded;
            return content + "." + hmac(ring, ring.activeKey(), "signature", content.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) { throw AiErrors.error("key_unavailable"); }
    }
    public Map<String, Object> verify(String token, String purpose) {
        try {
            if (token == null || token.length() > 8192) throw new IllegalArgumentException();
            String[] parts = token.split("\\.", -1);
            if (parts.length != 3) throw new IllegalArgumentException();
            String expected = hmac(material.get(), parts[0], "signature", (parts[0] + "." + parts[1]).getBytes(StandardCharsets.UTF_8));
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
