package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.DatasetTableTask;
import io.dataease.plugins.common.base.domain.DatasetTableTaskExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DatasetTableTaskMapper {
    long countByExample(DatasetTableTaskExample example);

    int deleteByExample(DatasetTableTaskExample example);

    int deleteByPrimaryKey(String id);

    int insert(DatasetTableTask record);

    int insertSelective(DatasetTableTask record);

    List<DatasetTableTask> selectByExample(DatasetTableTaskExample example);

    DatasetTableTask selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") DatasetTableTask record, @Param("example") DatasetTableTaskExample example);

    int updateByExample(@Param("record") DatasetTableTask record, @Param("example") DatasetTableTaskExample example);

    int updateByPrimaryKeySelective(DatasetTableTask record);

    int updateByPrimaryKey(DatasetTableTask record);
}