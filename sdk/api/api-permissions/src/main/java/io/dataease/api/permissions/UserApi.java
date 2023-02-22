package io.dataease.api.permissions;

import io.dataease.api.permissions.dto.UserDto;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

public interface UserApi {

    @GetMapping("/all")
    List<UserDto> all();
}
