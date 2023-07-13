package io.dataease.xpack.permissions.apisix.dao.ext;

import io.dataease.auth.bo.TokenUserBO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DeTokenMapper {

    @Select("select pwd from per_user where id = #{uid}")
    String secret(@Param("uid") Long uid);

    @Select("select id as user_id, default_oid from per_user where account = #{account}")
    TokenUserBO convertFromAccount(@Param("account") String account);
}
