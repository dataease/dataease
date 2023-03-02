package io.dataease.server.defeign.permissions.auth;

import io.dataease.feign.DeFeign;
import io.dataease.api.permissions.auth.api.AuthApi;


@DeFeign(value = "xpack-permissions", path = "/auth")
public interface PermissionFeignService extends AuthApi {

}
