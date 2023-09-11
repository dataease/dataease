package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.PanelWatermark;
import io.dataease.plugins.common.base.domain.PanelWatermarkExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PanelWatermarkMapper {
    long countByExample(PanelWatermarkExample example);

    int deleteByExample(PanelWatermarkExample example);

    int deleteByPrimaryKey(String id);

    int insert(PanelWatermark record);

    int insertSelective(PanelWatermark record);

    List<PanelWatermark> selectByExampleWithBLOBs(PanelWatermarkExample example);

    List<PanelWatermark> selectByExample(PanelWatermarkExample example);

    PanelWatermark selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") PanelWatermark record, @Param("example") PanelWatermarkExample example);

    int updateByExampleWithBLOBs(@Param("record") PanelWatermark record, @Param("example") PanelWatermarkExample example);

    int updateByExample(@Param("record") PanelWatermark record, @Param("example") PanelWatermarkExample example);

    int updateByPrimaryKeySelective(PanelWatermark record);

    int updateByPrimaryKeyWithBLOBs(PanelWatermark record);

    int updateByPrimaryKey(PanelWatermark record);
}