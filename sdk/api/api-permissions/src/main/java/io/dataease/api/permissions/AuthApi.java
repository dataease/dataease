package io.dataease.api.permissions;

import io.dataease.api.permissions.dto.AuthDTO;
import io.dataease.api.permissions.request.AuthRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;


public interface AuthApi {

    @GetMapping("/query")
    AuthDTO query(@RequestBody AuthRequest request);

    @GetMapping("/queryByUserId/{userId}")
    AuthDTO queryByUserId(@PathVariable("userId") Long userId);
}
