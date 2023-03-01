package io.dataease.server.defeign.permissions;

import io.dataease.feign.DeFeign;
import io.dataease.api.permissions.AuthApi;


@DeFeign(value = "xpack-permissions", path = "/auth")
public interface PermissionFeignService extends AuthApi {

}
