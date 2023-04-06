package io.dataease.xpack.permissions.login.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.dataease.xpack.permissions.login.dao.po.LoginUserPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LoginMapper extends BaseMapper<LoginUserPO> {

}
