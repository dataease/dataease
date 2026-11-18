import io.dataease.api.sensitive.SensitiveApi;
import io.dataease.api.sensitive.SecretType;
import io.dataease.api.permissions.user.api.CurrentPasswordVerifier;
import io.dataease.datasource.manage.DataSourceManage;
import io.dataease.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.dao.auto.entity.CoreDeEngine;
import io.dataease.datasource.dao.auto.repository.CoreDeEngineRepository;
import io.dataease.sensitive.SensitiveServer;
import io.dataease.system.manage.CorePermissionManage;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.utils.SensitiveConfigurationProvider;
import org.springframework.mock.web.MockHttpServletResponse;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.lang.reflect.Proxy;

/** Exercises the real reveal boundary without a running application or a database. */
public class SensitiveRegression {
    static class Verifier implements CurrentPasswordVerifier {
        int checks;
        boolean engineAllowed;
        public void verify(String password) { checks++; if (!"current-user-password".equals(password)) throw new io.dataease.exception.DEException(403, "denied"); }
        public void verifyEnginePermission() { if (!engineAllowed) throw new io.dataease.exception.DEException(403, "denied"); }
    }
    static String decrypt(SensitiveApi.EncryptedValue result, byte[] key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, Base64.getDecoder().decode(result.iv())));
        cipher.updateAAD("dataease:sensitive-reveal:v1".getBytes(StandardCharsets.UTF_8));
        return new String(cipher.doFinal(Base64.getDecoder().decode(result.ciphertext())), StandardCharsets.UTF_8);
    }
    static void run() throws Exception {
        var server = new SensitiveServer(); var verifier = new Verifier();
        var response = new MockHttpServletResponse();
        boolean[] authorized = {true}; int[] reads = {0};
        Regression.inject(server, "response", response);
        Regression.inject(server, "verifiers", List.of(verifier));
        Regression.inject(server, "permissions", new CorePermissionManage() {
            @Override public boolean checkAuth(io.dataease.api.permissions.auth.dto.BusiPerCheckDTO dto) {
                Regression.check(dto.getId().equals(101L));
                Regression.check(dto.getAuthEnum() == io.dataease.constant.AuthEnum.MANAGE);
                return authorized[0];
            }
        });
        Regression.inject(server, "datasourceManage", new DataSourceManage(null) {
            @Override public CoreDatasource getCoreDatasource(Long id) {
                reads[0]++; var ds = new CoreDatasource();
                ds.setConfiguration("{\"password\":\"历史连接密码-0123456789\",\"sshPassword\":\"long synthetic ssh password\\nline 2\",\"username\":\"other-field\"}"); return ds;
            }
        });
        byte[] key = new byte[32]; new java.security.SecureRandom().nextBytes(key);
        String encoded = Base64.getEncoder().encodeToString(key);
        var request = new SensitiveApi.Request("101", SecretType.DATASOURCE_PASSWORD, "current-user-password", encoded);
        try {
            V3UserUtil.setUid(17L);
            var first = server.reveal(request); var second = server.reveal(request);
            Regression.check(decrypt(first, key).equals("历史连接密码-0123456789"));
            Regression.check(!first.iv().equals(second.iv()));
            Regression.check(!first.ciphertext().contains("0123456789"));
            byte[] wrongKey = key.clone(); wrongKey[0] ^= 1;
            try { decrypt(first, wrongKey); throw new AssertionError("Expected response authentication failure"); }
            catch (javax.crypto.AEADBadTagException expected) { Regression.check(true); }
            try { Regression.JSON.readValue("{\"secretType\":\"ARBITRARY_JSON_PATH\"}", SensitiveApi.Request.class); throw new AssertionError("Unknown selector"); }
            catch (com.fasterxml.jackson.core.JsonProcessingException expected) { Regression.check(true); }
            Regression.check(response.getHeader("Cache-Control").equals("no-store"));
            // Fields without an original visibility control cannot be queried through the API either.
            for (String selector : List.of("DATASOURCE_SSH_KEY", "DATASOURCE_SSL_KEY", "SYNC_SSH_PASSWORD", "SYNC_SSH_KEY", "SYNC_SSH_KEY_PASSWORD")) {
                try {
                    Regression.JSON.readValue("{\"secretType\":\"" + selector + "\"}", SensitiveApi.Request.class);
                    throw new AssertionError("Removed selector accepted");
                } catch (com.fasterxml.jackson.core.JsonProcessingException expected) { Regression.check(true); }
            }
            var ssh = server.reveal(new SensitiveApi.Request("101", SecretType.DATASOURCE_SSH_PASSWORD, "current-user-password", encoded));
            Regression.check(decrypt(ssh, key).equals("long synthetic ssh password\nline 2"));
            Regression.denied(() -> server.reveal(new SensitiveApi.Request("101", SecretType.DATASOURCE_PASSWORD, "wrong-password", encoded)));
            int previousChecks = verifier.checks, previousReads = reads[0];
            authorized[0] = false;
            Regression.denied(() -> server.reveal(request));
            Regression.check(verifier.checks == previousChecks + 1 && reads[0] == previousReads);
            authorized[0] = true;
            Regression.denied(() -> server.reveal(new SensitiveApi.Request("101", SecretType.DATASOURCE_PASSWORD, "current-user-password", "bad")));
            Regression.denied(() -> server.reveal(new SensitiveApi.Request("101", null, "current-user-password", encoded)));
            Regression.denied(() -> server.reveal(new SensitiveApi.Request("-1", SecretType.DATASOURCE_PASSWORD, "current-user-password", encoded)));
            Regression.denied(() -> server.reveal(new SensitiveApi.Request("101", SecretType.DATASOURCE_SSH_KEY_PASSWORD, "current-user-password", encoded)));
            Regression.denied(() -> server.reveal(new SensitiveApi.Request("101", SecretType.SYNC_PASSWORD, "current-user-password", encoded)));
            Regression.inject(server, "providers", List.of(new SensitiveConfigurationProvider() {
                public String resourceKind() { return "SYNC"; }
                public String loadAuthorized(String id) { if (!"sync-allowed".equals(id)) throw new io.dataease.exception.DEException(403, "denied"); return "{\"password\":\"sync-synthetic\"}"; }
            }));
            Regression.check(decrypt(server.reveal(new SensitiveApi.Request("sync-allowed", SecretType.SYNC_PASSWORD, "current-user-password", encoded)), key).equals("sync-synthetic"));
            Regression.denied(() -> server.reveal(new SensitiveApi.Request("other-org", SecretType.SYNC_PASSWORD, "current-user-password", encoded)));
            Regression.denied(() -> server.reveal(new SensitiveApi.Request("101", SecretType.ENGINE_PASSWORD, "current-user-password", encoded)));
            verifier.engineAllowed = true;
            var engine = new CoreDeEngine(); engine.setConfiguration("{\"password\":\"engine-synthetic\"}");
            Regression.inject(server, "engineRepository", Proxy.newProxyInstance(CoreDeEngineRepository.class.getClassLoader(), new Class[]{CoreDeEngineRepository.class},
                    (proxy, method, args) -> method.getName().equals("findById") ? Optional.of(engine) : null));
            Regression.check(decrypt(server.reveal(new SensitiveApi.Request("101", SecretType.ENGINE_PASSWORD, "current-user-password", encoded)), key).equals("engine-synthetic"));
            Regression.inject(server, "verifiers", List.of()); Regression.denied(() -> server.reveal(request));
            Regression.inject(server, "verifiers", List.of(verifier, verifier)); Regression.denied(() -> server.reveal(request));
            Regression.inject(server, "verifiers", List.of(verifier));
            V3UserUtil.setLink(new io.dataease.permission.model.LinkIdentity(1L, 17L, 101L, 2L));
            Regression.denied(() -> server.reveal(request));
            V3UserUtil.clear(); V3UserUtil.setUid(17L);
            // Five failures block subsequent guesses even if normal login lockout is disabled.
            V3UserUtil.setUid(18L);
            for (int i = 0; i < 5; i++) Regression.denied(() -> server.reveal(new SensitiveApi.Request("101", SecretType.DATASOURCE_PASSWORD, "bad", encoded)));
            int attempts = verifier.checks;
            Regression.denied(() -> server.reveal(request));
            Regression.check(verifier.checks == attempts);
            V3UserUtil.clear(); Regression.denied(() -> server.reveal(request));
        } finally { V3UserUtil.clear(); }
    }
}
