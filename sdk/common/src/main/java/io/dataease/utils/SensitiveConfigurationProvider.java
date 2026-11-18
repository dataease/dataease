package io.dataease.utils;

/** Internal extension point. Implementations must authorize resource and organization before loading. */
public interface SensitiveConfigurationProvider {
    String resourceKind();
    String loadAuthorized(String resourceId);
}
