package com.fit2cloud.commons.auth.mapper;

import com.fit2cloud.commons.auth.entity.SysUsersRoles;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;


/**
    * <p>
    * 用户角色关联 Mapper 接口
    * </p>
    *
    * @author cyw
    * @since 2021-02-19
*/
@Mapper
public interface SysUsersRolesMapper extends BaseMapper<SysUsersRoles> {

}
