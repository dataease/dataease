import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.cache.DECacheService;
import io.dataease.config.ConfigurationTransport;
import io.dataease.config.ConfigurationRequestAdvice;
import io.dataease.config.ConfigurationResponseAdvice;
import io.dataease.exception.DEException;
import io.dataease.rsa.dao.entity.CoreRsa;
import io.dataease.rsa.manage.RsaManage;
import io.dataease.utils.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.*;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.*;
import org.springframework.web.context.request.*;

public class Regression {
    static final ObjectMapper JSON = new ObjectMapper();
    static int assertions;
    static void check(boolean condition) {
        assertions++;
        if (!condition) throw new AssertionError("Assertion " + assertions);
    }
    static void denied(Runnable action) {
        try { action.run(); throw new AssertionError("Expected rejection"); }
        catch (DEException expected) { assertions++; }
    }
    static void inject(Object target, String field, Object value) throws Exception {
        var f = target.getClass().getDeclaredField(field); f.setAccessible(true); f.set(target, value);
    }
    static class Transport extends ConfigurationTransport {
        String identity = "user:org";
        @Override protected String owner() { return identity; }
    }
    static class Cache implements DECacheService<Object> {
        final Map<String, Object> values = new ConcurrentHashMap<>();
        public void put(String cache, String key, Object value, Long ttl, TimeUnit unit) { values.put(key, value); }
        public Object get(String cache, String key) { return values.get(key); }
        public Object take(String cache, String key) { return values.remove(key); }
        public boolean cacheExist(String name) { return true; }
        public boolean keyExist(String cache, String key) { return values.containsKey(key); }
        public void keyRemove(String cache, String key) { values.remove(key); }
    }
    static byte[] seal(Transport transport, String path, Object data, Path root, Path temp) throws Exception {
        var challenge = transport.issue(path);
        Path input = temp.resolve("challenge.json"), output = temp.resolve("envelope.json");
        JSON.writeValue(input.toFile(), Map.of("challenge", challenge, "path", path, "data", data));
        Process process = new ProcessBuilder("node", root.resolve("tests/configuration-security/seal.cjs").toString(),
                root.resolve("core/core-frontend").toString(), input.toString(), output.toString()).inheritIO().start();
        check(process.waitFor() == 0);
        return Files.readAllBytes(output);
    }
    public static void main(String[] args) throws Exception {
        Path root = Path.of(args[0]), temp = Path.of(args[1]);
        new io.dataease.i18n.Translator().setMessageSource(new org.springframework.context.support.StaticMessageSource());
        var context = new StaticApplicationContext();
        var cache = new Cache(); context.getBeanFactory().registerSingleton("dECacheService", cache);
        new CommonBeanFactory().setApplicationContext(context);
        var pair = RsaUtils.generate();
        var rsa = new CoreRsa(); rsa.setPublicKey(pair.getPublicKey()); rsa.setPrivateKey(pair.getPrivateKey());
        rsa.setAesKey("abcdefghijklmnop");
        new RsaUtils().setRsaManage(new RsaManage() { @Override public CoreRsa query() { return rsa; } });
        check(RsaUtils.publicKey().equals(pair.getPublicKey()));
        check(!RsaUtils.publicKey().contains(rsa.getAesKey()));
        // Retained historical storage formats, including the pre-deployment-key format.
        String plain = "{\"password\":\"fixture-password\"}";
        String legacy = CodingUtil.aesEncrypt(plain, "www.fit2cloud.co", "1234567890123456");
        EncryptUtils.initKeys(rsa.getAesKey());
        check(plain.equals(EncryptUtils.aesDecrypt(legacy)));
        check(plain.equals(EncryptUtils.aesDecrypt(EncryptUtils.aesEncrypt(plain))));
        String original = "{\"host\":\"db.local\",\"password\":\"fixture-password\",\"sshKey\":\"fixture-key\"}";
        var originalTree = JSON.readTree(original);
        var masked = ConfigurationCredentials.mask(originalTree);
        check(masked.path("password").asText().equals("***"));
        check(masked.path("sshKey").asText().equals("***"));
        check(originalTree.path("password").asText().equals("fixture-password"));
        check(JSON.readTree(ConfigurationCredentials.restore(masked.toString(), original)).equals(originalTree));
        denied(() -> ConfigurationCredentials.restore(masked.toString(), null));
        denied(() -> ConfigurationCredentials.restore(masked.toString().replace("db.local", "attacker.local"), original));
        check(JSON.readTree(ConfigurationCredentials.restore("{\"host\":\"db.local\",\"password\":\"\"}", original))
                .path("password").asText().isEmpty());
        var wrappedConfig = ConfigurationCredentials.mask(JSON.readTree("{\"configuration\":\"{\\\"host\\\":\\\"db.local\\\",\\\"jdbc\\\":\\\"jdbc:test?password=fixture\\\"}\"}"));
        var inner = JSON.readTree(wrappedConfig.path("configuration").asText());
        check(inner.path("host").asText().equals("db.local")); check(inner.path("jdbc").asText().equals("***"));
        check(ConfigurationCredentials.mask(JSON.readTree("{\"headers\":[{\"name\":\"Authorization\",\"value\":\"Bearer fixture\"}]}"))
                .path("headers").get(0).path("value").asText().equals("***"));
        check(ConfigurationCredentials.mask(JSON.readTree("{\"sslKey\":\"fixture-private-key\"}"))
                .path("sslKey").asText().equals("***"));
        check(ConfigurationCredentials.mask(JSON.readTree("{\"raw\":\"{\\\"password\\\":\\\"fixture\\\"}\"}"))
                .path("raw").asText().equals("***"));
        check(JSON.readTree(ConfigurationCredentials.restore("{\"name\":\"***\",\"host\":\"db.local\"}", null))
                .path("name").asText().equals("***"));
        check(ConfigurationCredentials.mask(JSON.readTree("{\"name\":\"token\",\"value\": [\"fixture\"]}"))
                .path("value").asText().equals("***"));
        String oldArray = "[{\"name\":\"a\",\"password\":\"first\"},{\"name\":\"b\",\"password\":\"second\"}]";
        denied(() -> ConfigurationCredentials.restore("[{\"name\":\"b\",\"password\":\"***\"}]", oldArray));
        var transport = new Transport();
        String path = "/datasource/save";
        var payload = Map.of("configuration", "长配置".repeat(4000), "password", "fixture-password");
        byte[] encrypted = seal(transport, path, payload, root, temp);
        check(!new String(encrypted, StandardCharsets.UTF_8).contains("fixture-password"));
        check(JSON.readTree(transport.decrypt(encrypted, path, false)).equals(JSON.valueToTree(payload)));
        check(JSON.readTree(transport.decrypt(encrypted, path, true)).equals(JSON.valueToTree(payload)));
        denied(() -> transport.decrypt(encrypted, path, true));
        byte[] wrongPath = seal(transport, path, payload, root, temp);
        denied(() -> transport.decrypt(wrongPath, "/engine/save", true));
        byte[] wrongUser = seal(transport, path, payload, root, temp); transport.identity = "other:org";
        denied(() -> transport.decrypt(wrongUser, path, true)); transport.identity = "user:org";
        byte[] tampered = seal(transport, path, payload, root, temp);
        var broken = JSON.readTree(tampered); ((com.fasterxml.jackson.databind.node.ObjectNode) broken).put("iv", Base64.getEncoder().encodeToString(new byte[12]));
        denied(() -> transport.decrypt(broken.toString().getBytes(StandardCharsets.UTF_8), path, true));
        denied(() -> transport.decrypt("{}".getBytes(StandardCharsets.UTF_8), path, true));
        var expired = transport.issue(path); cache.values.put(expired.get("requestId").toString(), "user:org\n" + path + "\n0");
        byte[] expiredBody = JSON.writeValueAsBytes(expired);
        denied(() -> transport.decrypt(expiredBody, path, true));
        // Spring may pass a wrapped HttpInputMessage, not ServletServerHttpRequest.
        var advice = new ConfigurationRequestAdvice(); inject(advice, "transport", transport);
        byte[] request = seal(transport, path, payload, root, temp);
        var servlet = new MockHttpServletRequest("POST", "/de2api/apisix/check"); servlet.setContextPath("/de2api");
        servlet.addHeader("X-Forwarded-Uri", "/de2api" + path); servlet.addHeader("X-Forwarded-Method", "POST");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(servlet));
        check(JSON.readTree(advice.beforeBodyRead(new MockHttpInputMessage(request), null, Object.class,
                MappingJackson2HttpMessageConverter.class).getBody()).equals(JSON.valueToTree(payload)));
        servlet.setRequestURI("/de2api" + path);
        check(JSON.readTree(advice.beforeBodyRead(new MockHttpInputMessage(request), null, Object.class,
                MappingJackson2HttpMessageConverter.class).getBody()).equals(JSON.valueToTree(payload)));
        servlet.setRequestURI("/de2api/datasource/%73ave;v=1");
        try {
            advice.beforeBodyRead(new MockHttpInputMessage("{}".getBytes(StandardCharsets.UTF_8)), null,
                    Object.class, MappingJackson2HttpMessageConverter.class);
            throw new AssertionError("Encoded route accepted plaintext");
        } catch (DEException expected) { assertions++; }
        RequestContextHolder.resetRequestAttributes();
        var responseAdvice = new ConfigurationResponseAdvice(); inject(responseAdvice, "json", JSON);
        var response = new ServletServerHttpResponse(new MockHttpServletResponse());
        var body = Map.of("configuration", original, "id", "123");
        var result = responseAdvice.beforeBodyWrite(body, null, MediaType.APPLICATION_JSON,
                MappingJackson2HttpMessageConverter.class,
                new ServletServerHttpRequest(new MockHttpServletRequest("GET", "/datasource/get/123")), response);
        check(!result.toString().contains("fixture-password"));
        check(body.get("configuration").equals(original));
        check(response.getHeaders().getCacheControl().equals("no-store"));
        System.out.println("PASS: " + assertions + " configuration security assertions (no DB/application startup)");
    }
}
