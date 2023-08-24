package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.PanelAppTemplateLog;
import io.dataease.plugins.common.base.domain.PanelAppTemplateLogExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PanelAppTemplateLogMapper {
    long countByExample(PanelAppTemplateLogExample example);

    int deleteByExample(PanelAppTemplateLogExample example);

    int deleteByPrimaryKey(String id);

    int insert(PanelAppTemplateLog record);

    int insertSelective(PanelAppTemplateLog record);

    List<PanelAppTemplateLog> selectByExample(PanelAppTemplateLogExample example);

    PanelAppTemplateLog selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") PanelAppTemplateLog record, @Param("example") PanelAppTemplateLogExample example);

    int updateByExample(@Param("record") PanelAppTemplateLog record, @Param("example") PanelAppTemplateLogExample example);

    int updateByPrimaryKeySelective(PanelAppTemplateLog record);

    int updateByPrimaryKey(PanelAppTemplateLog record);
}