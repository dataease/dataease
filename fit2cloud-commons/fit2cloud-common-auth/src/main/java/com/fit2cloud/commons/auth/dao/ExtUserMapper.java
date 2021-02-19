package com.fit2cloud.commons.auth.dao;



import com.fit2cloud.commons.auth.bean.ExtPermissionBean;
import com.fit2cloud.commons.auth.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ExtUserMapper {

    @Select("select password from sys_user where username = #{userName,jdbcType=VARCHAR} ")
    String getPassword(String userName);

    @Select("select role_id from sys_users_roles where username = #{userName,jdbcType=VARCHAR} ")
    List<String> getRole(String userName);

    @Select({
            "select sm.permission ",
            "from sys_users_roles sur ",
            "LEFT JOIN sys_roles_menus srm on srm.role_id = sur.role_id ",
            "LEFT JOIN sys_menu sm on sm.menu_id = srm.menu_id ",
            "where sur.username = #{userName,jdbcType=VARCHAR} "
    })
    List<String> getPermission(String userName);

    @Select("select * from sys_user where username = #{username,jdbcType=VARCHAR}")
    SysUser getUser(String username);

    @Select("select path,permission from sys_menu where path is not null")
    List<ExtPermissionBean> getPermissions();


}
