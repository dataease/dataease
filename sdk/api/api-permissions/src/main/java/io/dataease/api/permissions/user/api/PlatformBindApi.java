package io.dataease.api.permissions.user.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface PlatformBindApi {
    @PostMapping("/prepare")
    Prepared prepare(@RequestBody Request request);

    record Request(Integer origin, String returnPath, String pwd, String mfaCode) { }

    record Prepared(String state, int origin, String redirectUri, String returnPath, long expiresAt) { }
}
