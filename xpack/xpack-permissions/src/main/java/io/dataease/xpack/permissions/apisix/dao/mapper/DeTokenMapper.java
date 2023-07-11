package io.dataease.xpack.permissions.apisix.dao.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DeTokenMapper {

    @Select("select pwd from per_user where id = #{uid}")
    String secret(@Param("uid") Long uid);
}
