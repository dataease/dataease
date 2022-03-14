package io.dataease.base.mapper;

import io.dataease.base.domain.PanelGroupExtendData;
import io.dataease.base.domain.PanelGroupExtendDataExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PanelGroupExtendDataMapper {
    long countByExample(PanelGroupExtendDataExample example);

    int deleteByExample(PanelGroupExtendDataExample example);

    int deleteByPrimaryKey(String id);

    int insert(PanelGroupExtendData record);

    int insertSelective(PanelGroupExtendData record);

    List<PanelGroupExtendData> selectByExampleWithBLOBs(PanelGroupExtendDataExample example);

    List<PanelGroupExtendData> selectByExample(PanelGroupExtendDataExample example);

    PanelGroupExtendData selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") PanelGroupExtendData record, @Param("example") PanelGroupExtendDataExample example);

    int updateByExampleWithBLOBs(@Param("record") PanelGroupExtendData record, @Param("example") PanelGroupExtendDataExample example);

    int updateByExample(@Param("record") PanelGroupExtendData record, @Param("example") PanelGroupExtendDataExample example);

    int updateByPrimaryKeySelective(PanelGroupExtendData record);

    int updateByPrimaryKeyWithBLOBs(PanelGroupExtendData record);

    int updateByPrimaryKey(PanelGroupExtendData record);
}