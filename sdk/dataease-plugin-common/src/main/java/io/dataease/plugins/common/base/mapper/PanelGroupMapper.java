package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.PanelGroup;
import io.dataease.plugins.common.base.domain.PanelGroupExample;
import io.dataease.plugins.common.base.domain.PanelGroupWithBLOBs;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PanelGroupMapper {
    long countByExample(PanelGroupExample example);

    int deleteByExample(PanelGroupExample example);

    int deleteByPrimaryKey(String id);

    int insert(PanelGroupWithBLOBs record);

    int insertSelective(PanelGroupWithBLOBs record);

    List<PanelGroupWithBLOBs> selectByExampleWithBLOBs(PanelGroupExample example);

    List<PanelGroup> selectByExample(PanelGroupExample example);

    PanelGroupWithBLOBs selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") PanelGroupWithBLOBs record, @Param("example") PanelGroupExample example);

    int updateByExampleWithBLOBs(@Param("record") PanelGroupWithBLOBs record, @Param("example") PanelGroupExample example);

    int updateByExample(@Param("record") PanelGroup record, @Param("example") PanelGroupExample example);

    int updateByPrimaryKeySelective(PanelGroupWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(PanelGroupWithBLOBs record);

    int updateByPrimaryKey(PanelGroup record);
}