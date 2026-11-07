package io.dataease.api.permissions.user.api;

/** Internal only. Must verify the current identity and apply the normal password attempt limit. */
public interface CurrentPasswordVerifier {
    void verify(String password);

    /** Enforce the same system-parameter manage permission as engine editing. */
    void verifyEnginePermission();
}
