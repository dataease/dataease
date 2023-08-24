package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.SysLoginLimit;
import io.dataease.plugins.common.base.domain.SysLoginLimitExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SysLoginLimitMapper {
    long countByExample(SysLoginLimitExample example);

    int deleteByExample(SysLoginLimitExample example);

    int insert(SysLoginLimit record);

    int insertSelective(SysLoginLimit record);

    List<SysLoginLimit> selectByExample(SysLoginLimitExample example);

    int updateByExampleSelective(@Param("record") SysLoginLimit record, @Param("example") SysLoginLimitExample example);

    int updateByExample(@Param("record") SysLoginLimit record, @Param("example") SysLoginLimitExample example);
}