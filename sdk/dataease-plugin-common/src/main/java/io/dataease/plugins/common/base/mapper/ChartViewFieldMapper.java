package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.ChartViewField;
import io.dataease.plugins.common.base.domain.ChartViewFieldExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ChartViewFieldMapper {
    long countByExample(ChartViewFieldExample example);

    int deleteByExample(ChartViewFieldExample example);

    int deleteByPrimaryKey(String id);

    int insert(ChartViewField record);

    int insertSelective(ChartViewField record);

    List<ChartViewField> selectByExampleWithBLOBs(ChartViewFieldExample example);

    List<ChartViewField> selectByExample(ChartViewFieldExample example);

    ChartViewField selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ChartViewField record, @Param("example") ChartViewFieldExample example);

    int updateByExampleWithBLOBs(@Param("record") ChartViewField record, @Param("example") ChartViewFieldExample example);

    int updateByExample(@Param("record") ChartViewField record, @Param("example") ChartViewFieldExample example);

    int updateByPrimaryKeySelective(ChartViewField record);

    int updateByPrimaryKeyWithBLOBs(ChartViewField record);

    int updateByPrimaryKey(ChartViewField record);
}
