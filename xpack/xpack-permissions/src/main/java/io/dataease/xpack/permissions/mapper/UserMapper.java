package io.dataease.xpack.permissions.mapper;

import io.dataease.api.permissions.dto.UserDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("select id, account, name, email from sys_user")
    List<UserDto> all();
}
