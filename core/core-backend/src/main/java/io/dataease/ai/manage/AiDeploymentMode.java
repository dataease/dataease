package io.dataease.ai.manage;

import io.dataease.ai.support.AiErrors;
import io.dataease.api.permissions.auth.api.InteractiveAuthApi;
import io.dataease.license.bo.F2CLicResult;
import io.dataease.license.dao.po.LicensePO;
import io.dataease.license.manage.F2CLicManage;
import io.dataease.license.utils.LicenseUtil;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Objects;
import java.util.function.Supplier;

/** 从现有许可管理源读取部署模式，不把线程上下文缺失或异常解释为社区模式。 */
@Component
public class AiDeploymentMode {
    public enum Mode { COMMUNITY, LICENSED }
    public record State(Mode mode, String revision) {
        public State { Objects.requireNonNull(mode); Objects.requireNonNull(revision); }
        public boolean organizations() { return mode == Mode.LICENSED; }
    }
    private record Verified(State state, F2CLicResult license) { }

    private final ObjectProvider<F2CLicManage> licenses;
    private final ObjectProvider<InteractiveAuthApi> authorization;
    private final ApplicationContext beans;

    public AiDeploymentMode(ObjectProvider<F2CLicManage> licenses,
                            ObjectProvider<InteractiveAuthApi> authorization, ApplicationContext beans) {
        this.licenses = licenses;
        this.authorization = authorization;
        this.beans = beans;
    }

    public boolean substitute() {
        return beans.containsBean("substituleLoginServer") && !beans.containsBean("loginServer")
                && !beans.containsBean("userServer") && authorization.getIfAvailable() == null;
    }

    public State current() {
        return verified().state();
    }

    /** Bind a freshly verified result for existing business dispatch, never a copied request snapshot. */
    public <T> T inScope(State expected, Supplier<T> operation) {
        Verified verified = verified();
        if (!Objects.equals(expected, verified.state())) throw AiErrors.error("conflict");
        F2CLicResult previous = LicenseUtil.get();
        LicenseUtil.set(verified.license());
        try { return operation.get(); }
        finally { LicenseUtil.set(previous); }
    }

    private Verified verified() {
        try {
            F2CLicManage source = licenses.getIfAvailable();
            if (source == null) {
                if (substitute()) return new Verified(new State(Mode.COMMUNITY, "substitute-v1"), null);
                throw AiErrors.error("configuration_unavailable");
            }
            LicensePO beforeRecord = source.read();
            String before = fingerprint(beforeRecord);
            F2CLicResult result = source.validate();
            LicensePO afterRecord = source.read();
            String after = fingerprint(afterRecord);
            if (result == null || result.getStatus() == null || !before.equals(after)) {
                throw AiErrors.error("configuration_unavailable");
            }
            if ((beforeRecord == null || afterRecord == null)
                    && !(beforeRecord == null && afterRecord == null && result.getStatus() == F2CLicResult.Status.no_record)) {
                throw AiErrors.error("configuration_unavailable");
            }
            Mode mode = switch (result.getStatus()) {
                case valid -> Mode.LICENSED;
                case no_record, invalid, expired -> Mode.COMMUNITY;
            };
            return new Verified(new State(mode, after + ":" + result.getStatus().name()), result);
        } catch (RuntimeException failure) {
            throw AiErrors.error("configuration_unavailable");
        }
    }

    private String fingerprint(LicensePO record) {
        if (record == null) return "no-record";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (Object value : new Object[]{record.getId(), record.getUpdateTime(), record.getLicense(), record.getF2cLicense()}) {
                byte[] bytes = Objects.toString(value, "").getBytes(StandardCharsets.UTF_8);
                digest.update((bytes.length + ":").getBytes(StandardCharsets.US_ASCII));
                digest.update(bytes);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (java.security.NoSuchAlgorithmException impossible) {
            throw AiErrors.error("configuration_unavailable");
        }
    }
}
