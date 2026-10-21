package io.dataease.api.permissions.user.api;

/** Internal extension contract. Verification and write authorization are never HTTP endpoints. */
public interface PlatformBindingVerifier {
    default PlatformBindApi.VerificationMethod verificationMethod() {
        return PlatformBindApi.VerificationMethod.UNAVAILABLE;
    }

    PlatformBindApi.Prepared issue(PlatformBindApi.Request request, String configuredRedirectUri);

    void verifyAndConsume(String state, int origin, String configuredRedirectUri);
}
