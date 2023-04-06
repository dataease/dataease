package io.dataease.xpack.permissions.user.dao.ext.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.dataease.xpack.permissions.user.dao.ext.entity.RolePO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RoleExtMapper extends BaseMapper<RolePO> {
}
