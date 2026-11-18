package io.dataease.substitute.permissions.login;

import io.dataease.api.permissions.user.api.CurrentPasswordVerifier;
import io.dataease.auth.config.SubstituleLoginConfig;
import io.dataease.exception.DEException;
import io.dataease.i18n.Translator;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.result.ResultCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayDeque;

@Component
@ConditionalOnMissingBean(name = "loginServer")
public class CommunityCurrentPasswordVerifier implements CurrentPasswordVerifier {
    private final ArrayDeque<Long> failures = new ArrayDeque<>();

    @Override
    public void verifyEnginePermission() {
        if (!Long.valueOf(1L).equals(V3UserUtil.getUid()) || V3UserUtil.getLink() != null) throw denied();
    }

    @Override
    public synchronized void verify(String password) {
        long now = System.currentTimeMillis();
        while (!failures.isEmpty() && failures.peekFirst() < now - 300_000) failures.removeFirst();
        if (!Long.valueOf(1L).equals(V3UserUtil.getUid()) || V3UserUtil.getLink() != null || failures.size() >= 5) throw denied();
        if (password == null || !MessageDigest.isEqual(password.getBytes(StandardCharsets.UTF_8),
                SubstituleLoginConfig.getPwd().getBytes(StandardCharsets.UTF_8))) {
            failures.addLast(now);
            throw denied();
        }
    }

    private DEException denied() {
        return new DEException(ResultCode.PERMISSION_NO_ACCESS.code(), Translator.get("i18n_sensitive_verification_failed"));
    }
}
