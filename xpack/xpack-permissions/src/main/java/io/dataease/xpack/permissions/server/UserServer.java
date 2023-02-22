package io.dataease.xpack.permissions.server;

import io.dataease.api.permissions.UserApi;
import io.dataease.api.permissions.dto.UserDto;
import io.dataease.xpack.permissions.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserServer implements UserApi {

    @Resource
    private UserMapper userMapper;

    @Override
    public List<UserDto> all() {
        return userMapper.all();
    }
}
