package io.dataease.base.mapper;

import io.dataease.base.domain.ChartGroup;
import io.dataease.base.domain.ChartGroupExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ChartGroupMapper {
    long countByExample(ChartGroupExample example);

    int deleteByExample(ChartGroupExample example);

    int deleteByPrimaryKey(String id);

    int insert(ChartGroup record);

    int insertSelective(ChartGroup record);

    List<ChartGroup> selectByExample(ChartGroupExample example);

    ChartGroup selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ChartGroup record, @Param("example") ChartGroupExample example);

    int updateByExample(@Param("record") ChartGroup record, @Param("example") ChartGroupExample example);

    int updateByPrimaryKeySelective(ChartGroup record);

    int updateByPrimaryKey(ChartGroup record);
}