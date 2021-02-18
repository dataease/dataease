package com.fit2cloud.commons.auth.dao;


import com.fit2cloud.commons.auth.bean.ExtPermissionBean;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ExtUserMapper {

    @Select("select password from user where user_id = #{userId,jdbcType=VARCHAR} ")
    String getPassword(String userId);

    @Select("select role_id from role_user where user_id = #{userId,jdbcType=VARCHAR} ")
    List<String> getRole(String userId);

    @Select({
            "select resource_id from permission p " ,
            "left join role_user ru on p.relation_id = ru.role_id " ,
            "where type = 'role' and ru.user_id = #{userId,jdbcType=VARCHAR} "
    })
    List<String> getPermission(String userId);

    @Select({
            "select p.*,r.url ",
            "from permission p left join resource r on p.resource_id = r.id"
    })
    List<ExtPermissionBean> getPermissions();
}
