package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.SysStartupJob;
import io.dataease.plugins.common.base.domain.SysStartupJobExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SysStartupJobMapper {
    long countByExample(SysStartupJobExample example);

    int deleteByExample(SysStartupJobExample example);

    int deleteByPrimaryKey(String id);

    int insert(SysStartupJob record);

    int insertSelective(SysStartupJob record);

    List<SysStartupJob> selectByExample(SysStartupJobExample example);

    SysStartupJob selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") SysStartupJob record, @Param("example") SysStartupJobExample example);

    int updateByExample(@Param("record") SysStartupJob record, @Param("example") SysStartupJobExample example);

    int updateByPrimaryKeySelective(SysStartupJob record);

    int updateByPrimaryKey(SysStartupJob record);
}
