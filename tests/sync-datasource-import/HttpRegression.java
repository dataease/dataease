package io.dataease.regression;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.config.ConfigurationRequestAdvice;
import io.dataease.config.ConfigurationResponseAdvice;
import io.dataease.config.ConfigurationTransport;
import io.dataease.datasource.server.DatasourceServer;
import io.dataease.exception.DEException;
import io.dataease.utils.CacheUtils;
import io.dataease.utils.RsaUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.spec.MGF1ParameterSpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/** Real MVC request decoding, production endpoint and response advice; no server socket or database. */
public class HttpRegression {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static int passed;
    private static void check(boolean value) { if (!value) throw new AssertionError("HTTP scenario " + passed); passed++; }
    private static void inject(Object target, String name, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(name); field.setAccessible(true); field.set(target, value);
    }
    @RestControllerAdvice
    public static class Errors {
        @ExceptionHandler(DEException.class)
        public ResponseEntity<Map<String, Boolean>> rejected() {
            return ResponseEntity.badRequest().body(Map.of("rejected", true));
        }
    }
    public static void run(DatasourceServer server, String configuration) throws Exception {
        var generator = KeyPairGenerator.getInstance("RSA"); generator.initialize(2048);
        var keys = generator.generateKeyPair();
        var cache = new HashMap<String, Object>();
        var transport = new ConfigurationTransport() { @Override protected String owner() { return "synthetic-user:synthetic-org"; } };
        var input = new ConfigurationRequestAdvice(); inject(input, "transport", transport);
        var output = new ConfigurationResponseAdvice(); inject(output, "json", JSON);
        var mvc = MockMvcBuilders.standaloneSetup(server).setControllerAdvice(input, output, new Errors())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(JSON)).build();
        try (var rsa = mockStatic(RsaUtils.class); var caches = mockStatic(CacheUtils.class)) {
            rsa.when(RsaUtils::publicKey).thenReturn(Base64.getEncoder().encodeToString(keys.getPublic().getEncoded()));
            rsa.when(RsaUtils::privateKey).thenReturn(Base64.getEncoder().encodeToString(keys.getPrivate().getEncoded()));
            caches.when(() -> CacheUtils.put(anyString(), anyString(), any(), anyLong(), any(TimeUnit.class)))
                    .thenAnswer(call -> { cache.put(call.getArgument(1), call.getArgument(2)); return null; });
            caches.when(() -> CacheUtils.take(anyString(), anyString())).thenAnswer(call -> cache.remove(call.getArgument(1)));
            var request = Map.<String, Object>of("syncSourceId", "source", "name", "HTTP import", "pid", "0");
            byte[] checkBody = seal(transport, "/datasource/checkRepeat", request, keys.getPublic());
            var checked = mvc.perform(post("/datasource/checkRepeat").contentType(MediaType.APPLICATION_JSON).content(checkBody)).andReturn().getResponse();
            check(checked.getStatus() == 200 && checked.getContentAsString().equals("false"));
            check(mvc.perform(post("/datasource/checkRepeat").contentType(MediaType.APPLICATION_JSON).content(checkBody)).andReturn().getResponse().getStatus() == 400);
            var saved = mvc.perform(post("/datasource/save").contentType(MediaType.APPLICATION_JSON)
                    .content(seal(transport, "/datasource/save", request, keys.getPublic()))).andReturn().getResponse();
            check(saved.getStatus() == 200);
            var body = JSON.readTree(saved.getContentAsString());
            check(body.path("name").asText().equals("HTTP import") && body.path("id").asText().equals("123"));
            check(JSON.readTree(body.path("configuration").asText()).path("password").asText().equals("******"));
            check(!saved.getContentAsString().contains("synthetic-secret") && "no-store".equals(saved.getHeader("Cache-Control")));
            check(mvc.perform(post("/datasource/save").contentType(MediaType.APPLICATION_JSON).content(JSON.writeValueAsBytes(request))).andReturn().getResponse().getStatus() == 400);
            var tampered = new HashMap<String, Object>(request); tampered.put("configuration", configuration);
            check(mvc.perform(post("/datasource/save").contentType(MediaType.APPLICATION_JSON)
                    .content(seal(transport, "/datasource/save", tampered, keys.getPublic()))).andReturn().getResponse().getStatus() == 400);
            check(mvc.perform(post("/datasource/save").contentType(MediaType.APPLICATION_JSON)
                    .content(seal(transport, "/datasource/checkRepeat", request, keys.getPublic()))).andReturn().getResponse().getStatus() == 400);
        }
        System.out.println("PASS " + passed + " encrypted MVC import and response masking checks (synthetic cache/key, no database)");
    }
    private static byte[] seal(ConfigurationTransport transport, String path, Object request, java.security.PublicKey publicKey) throws Exception {
        var challenge = transport.issue(path); String id = (String) challenge.get("requestId");
        byte[] key = new byte[32], iv = new byte[12]; new SecureRandom().nextBytes(key); new SecureRandom().nextBytes(iv);
        var aes = Cipher.getInstance("AES/GCM/NoPadding");
        aes.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, iv));
        aes.updateAAD(("dataease:configuration:v1\n" + path + "\n" + id).getBytes(StandardCharsets.UTF_8));
        var rsa = Cipher.getInstance("RSA/ECB/OAEPPadding");
        rsa.init(Cipher.ENCRYPT_MODE, publicKey, new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT));
        var base64 = Base64.getEncoder();
        return JSON.writeValueAsBytes(Map.of("version", 1, "requestId", id, "encryptedKey", base64.encodeToString(rsa.doFinal(key)),
                "iv", base64.encodeToString(iv), "ciphertext", base64.encodeToString(aes.doFinal(JSON.writeValueAsBytes(request)))));
    }
}
