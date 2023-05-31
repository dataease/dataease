package io.dataease.defeign.permissions.user;

import io.dataease.api.permissions.user.api.UserApi;
import io.dataease.feign.DeFeign;

@DeFeign(value = "xpack-permissions", path = "/user")
public interface UserFeignService extends UserApi {
}
