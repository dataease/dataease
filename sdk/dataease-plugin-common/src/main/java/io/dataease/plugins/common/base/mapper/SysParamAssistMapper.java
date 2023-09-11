package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.SysParamAssist;
import io.dataease.plugins.common.base.domain.SysParamAssistExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SysParamAssistMapper {
    long countByExample(SysParamAssistExample example);

    int deleteByExample(SysParamAssistExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SysParamAssist record);

    int insertSelective(SysParamAssist record);

    List<SysParamAssist> selectByExampleWithBLOBs(SysParamAssistExample example);

    List<SysParamAssist> selectByExample(SysParamAssistExample example);

    SysParamAssist selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SysParamAssist record, @Param("example") SysParamAssistExample example);

    int updateByExampleWithBLOBs(@Param("record") SysParamAssist record, @Param("example") SysParamAssistExample example);

    int updateByExample(@Param("record") SysParamAssist record, @Param("example") SysParamAssistExample example);

    int updateByPrimaryKeySelective(SysParamAssist record);

    int updateByPrimaryKeyWithBLOBs(SysParamAssist record);
}