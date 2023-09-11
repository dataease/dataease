package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.PanelPdfTemplate;
import io.dataease.plugins.common.base.domain.PanelPdfTemplateExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PanelPdfTemplateMapper {
    long countByExample(PanelPdfTemplateExample example);

    int deleteByExample(PanelPdfTemplateExample example);

    int deleteByPrimaryKey(String id);

    int insert(PanelPdfTemplate record);

    int insertSelective(PanelPdfTemplate record);

    List<PanelPdfTemplate> selectByExampleWithBLOBs(PanelPdfTemplateExample example);

    List<PanelPdfTemplate> selectByExample(PanelPdfTemplateExample example);

    PanelPdfTemplate selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") PanelPdfTemplate record, @Param("example") PanelPdfTemplateExample example);

    int updateByExampleWithBLOBs(@Param("record") PanelPdfTemplate record, @Param("example") PanelPdfTemplateExample example);

    int updateByExample(@Param("record") PanelPdfTemplate record, @Param("example") PanelPdfTemplateExample example);

    int updateByPrimaryKeySelective(PanelPdfTemplate record);

    int updateByPrimaryKeyWithBLOBs(PanelPdfTemplate record);

    int updateByPrimaryKey(PanelPdfTemplate record);
}