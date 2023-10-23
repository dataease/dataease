package io.dataease.defeign.permissions.auth;

import io.dataease.api.permissions.auth.api.InteractiveAuthApi;
import io.dataease.feign.DeFeign;

@DeFeign(value = "xpack-permissions", path = "/interactive")
public interface InteractiveAuthFeignService extends InteractiveAuthApi {
}
