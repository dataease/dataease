package io.dataease.xpack.permissions.server;

import io.dataease.api.permissions.AuthApi;
import io.dataease.api.permissions.dto.AuthDTO;
import io.dataease.api.permissions.request.AuthRequest;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("auth")
@Primary
public class AuthServer implements AuthApi {
    @Override
    public AuthDTO query(AuthRequest request) {
        AuthDTO authDTO = new AuthDTO();
        authDTO.setModel(1);
        List<Long> list = List.of(1L, 2L, 3L, 4L, 5L, 6L);
        authDTO.setResourceId(list);
        return authDTO;
    }

    @Override
    public AuthDTO queryByUserId(Long userId) {
        AuthDTO authDTO = new AuthDTO();
        authDTO.setModel(1);
        List<Long> list = List.of(1L, 2L, 3L, 4L, 5L, 6L);
        authDTO.setResourceId(list);
        return authDTO;
    }
}
