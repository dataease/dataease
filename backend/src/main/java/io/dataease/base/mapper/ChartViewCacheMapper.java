package io.dataease.base.mapper;

import io.dataease.base.domain.ChartViewCache;
import io.dataease.base.domain.ChartViewCacheExample;
import io.dataease.base.domain.ChartViewCacheWithBLOBs;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ChartViewCacheMapper {
    long countByExample(ChartViewCacheExample example);

    int deleteByExample(ChartViewCacheExample example);

    int deleteByPrimaryKey(String id);

    int insert(ChartViewCacheWithBLOBs record);

    int insertSelective(ChartViewCacheWithBLOBs record);

    List<ChartViewCacheWithBLOBs> selectByExampleWithBLOBs(ChartViewCacheExample example);

    List<ChartViewCache> selectByExample(ChartViewCacheExample example);

    ChartViewCacheWithBLOBs selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ChartViewCacheWithBLOBs record, @Param("example") ChartViewCacheExample example);

    int updateByExampleWithBLOBs(@Param("record") ChartViewCacheWithBLOBs record, @Param("example") ChartViewCacheExample example);

    int updateByExample(@Param("record") ChartViewCache record, @Param("example") ChartViewCacheExample example);

    int updateByPrimaryKeySelective(ChartViewCacheWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(ChartViewCacheWithBLOBs record);

    int updateByPrimaryKey(ChartViewCache record);
}