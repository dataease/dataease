package io.dataease.server.defeign.permissions;

import io.dataease.annotation.DeFeign;
import io.dataease.api.permissions.AuthApi;


@DeFeign(value = "xpack-permissions", path = "/auth")
public interface PermissionFeignService extends AuthApi {

}
