package io.dataease.api.ai.runtime;

/** 可信业务构造；reference 指向资源及版本，permissionVersion 为权限依据版本，均不能来自未验证前端声明。 */
public record AiDataScope(String reference, String permissionVersion, String inputFingerprint,
                          boolean samples, String confirmation) {
    public AiDataScope {
        if (reference == null || reference.isBlank() || permissionVersion == null || permissionVersion.isBlank()
                || (inputFingerprint != null && !inputFingerprint.matches("[0-9a-f]{64}"))) throw new IllegalArgumentException("AI scope required");
        if (samples && (inputFingerprint == null || confirmation == null || confirmation.isBlank())) throw new IllegalArgumentException("AI sample confirmation required");
    }
    @Override public String toString() { return "AI data scope [redacted]"; }
}
