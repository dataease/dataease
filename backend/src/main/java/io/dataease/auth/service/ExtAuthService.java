package io.dataease.auth.service;

import io.dataease.commons.model.AuthURD;

import java.util.Set;

public interface ExtAuthService {

    Set<Long> userIdsByRD(AuthURD request);

    AuthURD resourceTarget(String resourceId);
}
