package io.dataease.xpack.permissions.auth;

import io.dataease.api.permissions.auth.api.AuthApi;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("auth")
@Primary
public class AuthServer implements AuthApi {

}
