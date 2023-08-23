package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.SysUsersRolesExample;
import io.dataease.plugins.common.base.domain.SysUsersRolesKey;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SysUsersRolesMapper {
    long countByExample(SysUsersRolesExample example);

    int deleteByExample(SysUsersRolesExample example);

    int deleteByPrimaryKey(SysUsersRolesKey key);

    int insert(SysUsersRolesKey record);

    int insertSelective(SysUsersRolesKey record);

    List<SysUsersRolesKey> selectByExample(SysUsersRolesExample example);

    int updateByExampleSelective(@Param("record") SysUsersRolesKey record, @Param("example") SysUsersRolesExample example);

    int updateByExample(@Param("record") SysUsersRolesKey record, @Param("example") SysUsersRolesExample example);
}