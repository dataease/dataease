package com.fit2cloud.commons.auth.mapper;

import com.fit2cloud.commons.auth.entity.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;


/**
    * <p>
    * 系统用户 Mapper 接口
    * </p>
    *
    * @author cyw
    * @since 2021-02-19
*/
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

}
