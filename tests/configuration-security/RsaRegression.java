import io.dataease.config.ConfigurationTransport;
import io.dataease.rsa.dao.entity.CoreRsa;
import io.dataease.rsa.manage.RsaManage;
import io.dataease.utils.RsaUtils;
import java.nio.file.Path;
import java.security.KeyPairGenerator;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

/** Historical RSA fixtures never read deployment keys or access the database. */
public class RsaRegression {
    static final com.fasterxml.jackson.databind.ObjectMapper JSON = new com.fasterxml.jackson.databind.ObjectMapper();
    static int assertions;
    static void check(boolean condition) {
        assertions++;
        if (!condition) throw new AssertionError("RSA assertion " + assertions);
    }
    static void denied(Runnable action) {
        try { action.run(); throw new AssertionError("Expected rejection"); }
        catch (io.dataease.exception.DEException expected) { assertions++; }
    }
    static class Transport extends ConfigurationTransport {
        String identity = "user:org";
        @Override protected String owner() { return identity; }
    }
    static class Cache implements io.dataease.cache.DECacheService<Object> {
        final Map<String, Object> values = new java.util.concurrent.ConcurrentHashMap<>();
        public void put(String cache, String key, Object value, Long ttl, java.util.concurrent.TimeUnit unit) { values.put(key, value); }
        public Object get(String cache, String key) { return values.get(key); }
        public Object take(String cache, String key) { return values.remove(key); }
        public boolean cacheExist(String name) { return true; }
        public boolean keyExist(String cache, String key) { return values.containsKey(key); }
        public void keyRemove(String cache, String key) { values.remove(key); }
    }
    static byte[] seal(Transport transport, String path, Object data, Path root, Path temp) throws Exception {
        Path input = temp.resolve("rsa-challenge.json"), output = temp.resolve("rsa-envelope.json");
        JSON.writeValue(input.toFile(), Map.of("challenge", transport.issue(path), "path", path, "data", data));
        var process = new ProcessBuilder("node", root.resolve("tests/configuration-security/seal.cjs").toString(),
                root.resolve("core/core-frontend").toString(), input.toString(), output.toString()).inheritIO().start();
        check(process.waitFor() == 0);
        return java.nio.file.Files.readAllBytes(output);
    }
    public static void main(String[] args) throws Exception {
        new io.dataease.i18n.Translator().setMessageSource(new org.springframework.context.support.StaticMessageSource());
        var context = new org.springframework.context.support.StaticApplicationContext();
        context.getBeanFactory().registerSingleton("dECacheService", new Cache());
        new io.dataease.utils.CommonBeanFactory().setApplicationContext(context);
        run(Path.of(args[0]), Path.of(args[1]));
        System.out.println("PASS: " + assertions + " RSA upgrade assertions (1024/2048, no DB/application startup)");
    }

    static void run(Path root, Path temp) throws Exception {
        for (int bits : new int[]{1024, 2048}) {
            var generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(bits);
            var pair = generator.generateKeyPair();
            var stored = new CoreRsa();
            stored.setPublicKey(Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()));
            stored.setPrivateKey(Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded()));
            stored.setAesKey("abcdefghijklmnop");
            new RsaUtils().setRsaManage(new RsaManage() {
                @Override public CoreRsa query() { return stored; }
                @Override public void save() { throw new AssertionError("Keys must never be replaced"); }
            });
            // Cipher("RSA") is the historical Java transformation; persisted/transmitted
            // ciphertext made by that implementation must remain decryptable.
            var legacyCipher = javax.crypto.Cipher.getInstance("RSA");
            legacyCipher.init(javax.crypto.Cipher.ENCRYPT_MODE, pair.getPublic());
            String historical = Base64.getEncoder().encodeToString(legacyCipher.doFinal(
                    "historical-rsa-value".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            check("historical-rsa-value".equals(RsaUtils.decryptStr(historical)));
            RsaUtils.checkKeyPair();
            check(stored.getPublicKey().equals(Base64.getEncoder().encodeToString(pair.getPublic().getEncoded())));
            check(stored.getAesKey().equals("abcdefghijklmnop"));
            for (String plain : new String[]{"login-password", "a".repeat(bits / 8 - 11), "中文多块配置".repeat(100)}) {
                String encrypted = RsaUtils.encryptStr(plain);
                check(plain.equals(RsaUtils.decryptStr(encrypted)));
                byte[] truncated = Base64.getDecoder().decode(encrypted);
                try {
                    RsaUtils.decryptStr(Base64.getEncoder().encodeToString(Arrays.copyOf(truncated, truncated.length - 1)));
                    throw new AssertionError("Truncated RSA block accepted");
                } catch (RuntimeException expected) { assertions++; }
            }
            Path input = temp.resolve("rsa-input.json"), output = temp.resolve("rsa-output.json");
            JSON.writeValue(input.toFile(), Map.of("publicKey", stored.getPublicKey(), "bits", bits));
            var process = new ProcessBuilder("node", root.resolve("tests/configuration-security/rsa.cjs").toString(),
                    root.resolve("core/core-frontend").toString(), input.toString(), output.toString()).inheritIO().start();
            check(process.waitFor() == 0);
            var frontend = JSON.readTree(output.toFile());
            check("历史登录-password".equals(RsaUtils.decryptStr(frontend.path("ciphertext").asText())));
            var transport = new Transport();
            String path = "/datasource/save";
            String config = "{\"password\":\"fixture\",\"name\":\"历史配置\"}";
            byte[] envelope = seal(transport, path, JSON.readTree(config), root, temp);
            check(JSON.readTree(envelope).path("encryptedKey").binaryValue().length == bits / 8);
            check(JSON.readTree(transport.decrypt(envelope, path, true)).equals(JSON.readTree(config)));
            denied(() -> transport.decrypt(envelope, path, true));
            byte[] bad = seal(transport, path, Map.of("password", "fixture"), root, temp);
            var badTree = JSON.readTree(bad);
            ((com.fasterxml.jackson.databind.node.ObjectNode) badTree).put("encryptedKey", Base64.getEncoder().encodeToString(new byte[bits / 8 - 1]));
            byte[] badEnvelope = JSON.writeValueAsBytes(badTree);
            denied(() -> transport.decrypt(badEnvelope, path, true));
            byte[] crossPath = seal(transport, path, Map.of("password", "fixture"), root, temp);
            denied(() -> transport.decrypt(crossPath, "/engine/save", true));
            byte[] crossUser = seal(transport, path, Map.of("password", "fixture"), root, temp);
            transport.identity = "other:org";
            denied(() -> transport.decrypt(crossUser, path, true));
            transport.identity = "user:org";
            byte[] tampered = seal(transport, path, Map.of("password", "fixture"), root, temp);
            var tree = JSON.readTree(tampered);
            byte[] ciphertext = tree.path("ciphertext").binaryValue();
            ciphertext[0] ^= 1;
            ((com.fasterxml.jackson.databind.node.ObjectNode) tree).put("ciphertext", Base64.getEncoder().encodeToString(ciphertext));
            byte[] altered = JSON.writeValueAsBytes(tree);
            denied(() -> transport.decrypt(altered, path, true));
            var expired = transport.issue(path);
            io.dataease.utils.CacheUtils.put("configuration-transport", expired.get("requestId").toString(),
                    "user:org\n" + path + "\n0", 2L, java.util.concurrent.TimeUnit.MINUTES);
            byte[] expiredBody = JSON.writeValueAsBytes(expired);
            denied(() -> transport.decrypt(expiredBody, path, true));
            var other = generator.generateKeyPair();
            stored.setPrivateKey(Base64.getEncoder().encodeToString(other.getPrivate().getEncoded()));
            RsaUtils.checkKeyPair(); // Diagnostic only: no replacement and no startup exception.
            check(stored.getPrivateKey().equals(Base64.getEncoder().encodeToString(other.getPrivate().getEncoded())));
        }
    }
}
