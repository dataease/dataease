package io.dataease.sensitive;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.api.sensitive.SensitiveApi;
import io.dataease.api.permissions.user.api.CurrentPasswordVerifier;
import io.dataease.api.permissions.auth.dto.BusiPerCheckDTO;
import io.dataease.constant.AuthEnum;
import io.dataease.constant.BusiResourceEnum;
import io.dataease.datasource.manage.DataSourceManage;
import io.dataease.datasource.dao.auto.repository.CoreDeEngineRepository;
import io.dataease.exception.DEException;
import io.dataease.i18n.Translator;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.result.ResultCode;
import io.dataease.system.manage.CorePermissionManage;
import io.dataease.utils.LogUtil;
import io.dataease.utils.SensitiveConfigurationProvider;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/sensitive")
public class SensitiveServer implements SensitiveApi {
    @Resource
    private DataSourceManage datasourceManage;
    @Resource
    private CoreDeEngineRepository engineRepository;
    @Resource
    private CorePermissionManage permissions;
    @Resource
    private HttpServletResponse response;
    @Autowired(required = false)
    private List<CurrentPasswordVerifier> verifiers = List.of();
    @Autowired(required = false)
    private List<SensitiveConfigurationProvider> providers = List.of();
    // Always enforce a local limit, including installations with login lockout disabled.
    private final com.google.common.cache.Cache<Long, java.util.concurrent.atomic.AtomicInteger> attempts =
            com.google.common.cache.CacheBuilder.newBuilder().maximumSize(10_000)
                    .expireAfterWrite(5, java.util.concurrent.TimeUnit.MINUTES).build();
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public EncryptedValue reveal(Request request) {
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        if (V3UserUtil.getUid() == null || V3UserUtil.getLink() != null || request == null
                || request.secretType() == null || request.resourceId() == null || request.resourceId().isBlank()
                || request.password() == null || request.password().isEmpty() || verifiers.size() != 1) throw denied();
        byte[] key;
        try { key = Base64.getDecoder().decode(request.responseKey()); }
        catch (Exception e) { throw denied(); }
        if (key.length != 32) throw denied();
        try {
            // Each extension enforces its resource and organization permissions.
            verifyPassword(request.password());
            String configuration = authorizedConfiguration(request);
            JsonNode root = JSON.readTree(configuration);
            JsonNode value = root == null ? null : root.get(request.secretType().field());
            if (value == null || !value.isTextual()) throw denied();
            byte[] iv = new byte[12]; RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, iv));
            cipher.updateAAD("dataease:sensitive-reveal:v1".getBytes(StandardCharsets.UTF_8));
            byte[] encrypted = cipher.doFinal(value.textValue().getBytes(StandardCharsets.UTF_8));
            LogUtil.info("Sensitive credential viewed: uid=" + V3UserUtil.getUid() + ", kind="
                    + request.secretType() + ", resource=" + request.resourceId());
            return new EncryptedValue(Base64.getEncoder().encodeToString(iv), Base64.getEncoder().encodeToString(encrypted));
        } catch (DEException e) { throw e; }
        catch (Exception e) { throw denied(); }
        finally { java.util.Arrays.fill(key, (byte) 0); }
    }

    private void verifyPassword(String password) throws java.util.concurrent.ExecutionException {
        var failures = attempts.get(V3UserUtil.getUid(), java.util.concurrent.atomic.AtomicInteger::new);
        synchronized (failures) {
            if (failures.get() >= 5) throw denied();
            try {
                verifiers.getFirst().verify(password);
            } catch (DEException e) {
                failures.incrementAndGet();
                throw e;
            }
        }
    }

    private String authorizedConfiguration(Request request) {
        switch (request.secretType().resourceKind()) {
            case "DATASOURCE": {
                Long id = id(request.resourceId());
                if (!permissions.checkAuth(new BusiPerCheckDTO(id, BusiResourceEnum.DATASOURCE, AuthEnum.MANAGE))) throw denied();
                var datasource = datasourceManage.getCoreDatasource(id);
                if (datasource == null) throw denied();
                return datasource.getConfiguration();
            }
            case "ENGINE": {
                verifiers.getFirst().verifyEnginePermission();
                var engine = engineRepository.findById(id(request.resourceId())).orElseThrow(this::denied);
                return engine.getConfiguration();
            }
            default:
                return providers.stream().filter(p -> p.resourceKind().equals(request.secretType().resourceKind()))
                        .findFirst().orElseThrow(this::denied).loadAuthorized(request.resourceId());
        }
    }

    private Long id(String id) {
        try { long value = Long.parseLong(id); if (value > 0) return value; }
        catch (NumberFormatException ignored) { }
        throw denied();
    }

    private DEException denied() {
        return new DEException(ResultCode.PERMISSION_NO_ACCESS.code(), Translator.get("i18n_sensitive_verification_failed"));
    }
}
