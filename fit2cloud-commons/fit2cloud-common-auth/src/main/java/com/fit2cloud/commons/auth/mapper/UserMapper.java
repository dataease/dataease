package com.fit2cloud.commons.auth.mapper;

import com.fit2cloud.commons.auth.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;


/**
    * <p>
    *  Mapper 接口
    * </p>
    *
    * @author cyw
    * @since 2021-02-03
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
