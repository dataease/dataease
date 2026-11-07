package io.dataease.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.exception.DEException;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.result.ResultCode;
import io.dataease.utils.CacheUtils;
import io.dataease.utils.RsaUtils;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/** Configuration transport is independent of the persisted storage AES key. */
@Component
public class ConfigurationTransport {
    private static final String CACHE = "configuration-transport";
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Set<String> PATHS = Set.of(
            "/sensitive/reveal", "/datasource/checkRepeat", "/datasource/save", "/datasource/update", "/datasource/validate", "/datasource/getSchema",
            "/datasource/checkApiDatasource", "/datasource/loadRemoteFile", "/datasource/multidimensionalTables",
            "/engine/save", "/engine/validate",
            "/sync/datasource/save", "/sync/datasource/update", "/sync/datasource/validate",
            "/sync/datasource/getSchema", "/sync/datasource/fields",
            "/sync/task/add", "/sync/task/update", "/sync/task/target/keyPolicy");

    public static boolean protects(String path) {
        return PATHS.contains(path);
    }

    protected String owner() {
        if (V3UserUtil.getUid() == null || V3UserUtil.getLink() != null) {
            throw new DEException(ResultCode.PERMISSION_NO_ACCESS.code(), ResultCode.PERMISSION_NO_ACCESS.message());
        }
        return V3UserUtil.getUid() + ":" + V3UserUtil.getUser().getOid();
    }

    public Map<String, Object> issue(String path) {
        if (!protects(path)) throw invalid();
        String id = UUID.randomUUID().toString();
        long expiresAt = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(2);
        CacheUtils.put(CACHE, id, owner() + "\n" + path + "\n" + expiresAt, 2L, TimeUnit.MINUTES);
        return Map.of("version", 1, "requestId", id, "publicKey", RsaUtils.publicKey());
    }

    public byte[] decrypt(byte[] body, String path, boolean consume) {
        try {
            JsonNode envelope = JSON.readTree(body);
            if (envelope.path("version").asInt() != 1) throw invalid();
            String id = envelope.path("requestId").asText();
            Object saved = consume ? CacheUtils.take(CACHE, id) : CacheUtils.get(CACHE, id);
            if (!(saved instanceof String state)) throw invalid();
            String[] parts = state.split("\n");
            if (parts.length != 3 || !parts[0].equals(owner()) || !parts[1].equals(path)
                    || Long.parseLong(parts[2]) < System.currentTimeMillis()) throw invalid();
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] wrapped = decoder.decode(envelope.path("encryptedKey").asText());
            byte[] iv = decoder.decode(envelope.path("iv").asText());
            byte[] ciphertext = decoder.decode(envelope.path("ciphertext").asText());
            if (wrapped.length != 256 || iv.length != 12 || ciphertext.length < 16) throw invalid();
            Cipher rsa = Cipher.getInstance("RSA/ECB/OAEPPadding");
            rsa.init(Cipher.DECRYPT_MODE, KeyFactory.getInstance("RSA").generatePrivate(
                    new PKCS8EncodedKeySpec(decoder.decode(RsaUtils.privateKey()))),
                    new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT));
            byte[] key = rsa.doFinal(wrapped);
            if (key.length != 32) throw invalid();
            Cipher aes = Cipher.getInstance("AES/GCM/NoPadding");
            aes.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, iv));
            aes.updateAAD(("dataease:configuration:v1\n" + path + "\n" + id).getBytes(StandardCharsets.UTF_8));
            return aes.doFinal(ciphertext);
        } catch (Exception e) {
            // Never return cryptographic exception details or decrypted configuration.
            throw invalid();
        }
    }

    private DEException invalid() {
        return new DEException(ResultCode.PARAM_IS_INVALID.code(), io.dataease.i18n.Translator.get("i18n_configuration_encrypted_request_invalid"));
    }
}
