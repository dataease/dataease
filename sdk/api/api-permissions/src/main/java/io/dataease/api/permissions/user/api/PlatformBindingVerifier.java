package io.dataease.api.permissions.user.api;

/** Internal extension contract. Verification and write authorization are never HTTP endpoints. */
public interface PlatformBindingVerifier {
    PlatformBindApi.Prepared issue(PlatformBindApi.Request request, String configuredRedirectUri);

    void verifyAndConsume(String state, int origin, String configuredRedirectUri);
}
