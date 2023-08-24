package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.PanelTemplate;
import io.dataease.plugins.common.base.domain.PanelTemplateExample;
import io.dataease.plugins.common.base.domain.PanelTemplateWithBLOBs;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PanelTemplateMapper {
    long countByExample(PanelTemplateExample example);

    int deleteByExample(PanelTemplateExample example);

    int deleteByPrimaryKey(String id);

    int insert(PanelTemplateWithBLOBs record);

    int insertSelective(PanelTemplateWithBLOBs record);

    List<PanelTemplateWithBLOBs> selectByExampleWithBLOBs(PanelTemplateExample example);

    List<PanelTemplate> selectByExample(PanelTemplateExample example);

    PanelTemplateWithBLOBs selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") PanelTemplateWithBLOBs record, @Param("example") PanelTemplateExample example);

    int updateByExampleWithBLOBs(@Param("record") PanelTemplateWithBLOBs record, @Param("example") PanelTemplateExample example);

    int updateByExample(@Param("record") PanelTemplate record, @Param("example") PanelTemplateExample example);

    int updateByPrimaryKeySelective(PanelTemplateWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(PanelTemplateWithBLOBs record);

    int updateByPrimaryKey(PanelTemplate record);
}