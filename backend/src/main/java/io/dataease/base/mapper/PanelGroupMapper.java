package io.dataease.base.mapper;

import io.dataease.base.domain.PanelGroup;
import io.dataease.base.domain.PanelGroupExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PanelGroupMapper {
    long countByExample(PanelGroupExample example);

    int deleteByExample(PanelGroupExample example);

    int deleteByPrimaryKey(String id);

    int insert(PanelGroup record);

    int insertSelective(PanelGroup record);

    List<PanelGroup> selectByExample(PanelGroupExample example);

    PanelGroup selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") PanelGroup record, @Param("example") PanelGroupExample example);

    int updateByExample(@Param("record") PanelGroup record, @Param("example") PanelGroupExample example);

    int updateByPrimaryKeySelective(PanelGroup record);

    int updateByPrimaryKey(PanelGroup record);
}