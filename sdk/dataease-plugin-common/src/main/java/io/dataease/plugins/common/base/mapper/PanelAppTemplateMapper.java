package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.PanelAppTemplate;
import io.dataease.plugins.common.base.domain.PanelAppTemplateExample;
import io.dataease.plugins.common.base.domain.PanelAppTemplateWithBLOBs;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PanelAppTemplateMapper {
    long countByExample(PanelAppTemplateExample example);

    int deleteByExample(PanelAppTemplateExample example);

    int deleteByPrimaryKey(String id);

    int insert(PanelAppTemplateWithBLOBs record);

    int insertSelective(PanelAppTemplateWithBLOBs record);

    List<PanelAppTemplateWithBLOBs> selectByExampleWithBLOBs(PanelAppTemplateExample example);

    List<PanelAppTemplate> selectByExample(PanelAppTemplateExample example);

    PanelAppTemplateWithBLOBs selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") PanelAppTemplateWithBLOBs record, @Param("example") PanelAppTemplateExample example);

    int updateByExampleWithBLOBs(@Param("record") PanelAppTemplateWithBLOBs record, @Param("example") PanelAppTemplateExample example);

    int updateByExample(@Param("record") PanelAppTemplate record, @Param("example") PanelAppTemplateExample example);

    int updateByPrimaryKeySelective(PanelAppTemplateWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(PanelAppTemplateWithBLOBs record);

    int updateByPrimaryKey(PanelAppTemplate record);
}