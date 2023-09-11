package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.ChartView;
import io.dataease.plugins.common.base.domain.ChartViewExample;
import io.dataease.plugins.common.base.domain.ChartViewWithBLOBs;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ChartViewMapper {
    long countByExample(ChartViewExample example);

    int deleteByExample(ChartViewExample example);

    int deleteByPrimaryKey(String id);

    int insert(ChartViewWithBLOBs record);

    int insertSelective(ChartViewWithBLOBs record);

    List<ChartViewWithBLOBs> selectByExampleWithBLOBs(ChartViewExample example);

    List<ChartView> selectByExample(ChartViewExample example);

    ChartViewWithBLOBs selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ChartViewWithBLOBs record, @Param("example") ChartViewExample example);

    int updateByExampleWithBLOBs(@Param("record") ChartViewWithBLOBs record, @Param("example") ChartViewExample example);

    int updateByExample(@Param("record") ChartView record, @Param("example") ChartViewExample example);

    int updateByPrimaryKeySelective(ChartViewWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(ChartViewWithBLOBs record);

    int updateByPrimaryKey(ChartView record);
}