package io.dataease.api.permissions.login.api;

/** Internal extension contract; must never be exposed as an HTTP signing or verification endpoint. */
public interface PlatformTokenVerifier {

    /** Validate the platform, browser and exchange transaction, then atomically consume the JWT. */
    void verifyAndConsume(String token, int origin);
}
