package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.SysUserAssist;
import io.dataease.plugins.common.base.domain.SysUserAssistExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SysUserAssistMapper {
    long countByExample(SysUserAssistExample example);

    int deleteByExample(SysUserAssistExample example);

    int deleteByPrimaryKey(Long userId);

    int insert(SysUserAssist record);

    int insertSelective(SysUserAssist record);

    List<SysUserAssist> selectByExample(SysUserAssistExample example);

    SysUserAssist selectByPrimaryKey(Long userId);

    int updateByExampleSelective(@Param("record") SysUserAssist record, @Param("example") SysUserAssistExample example);

    int updateByExample(@Param("record") SysUserAssist record, @Param("example") SysUserAssistExample example);

    int updateByPrimaryKeySelective(SysUserAssist record);

    int updateByPrimaryKey(SysUserAssist record);
}