package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.DatasetTableIncrementalConfig;
import io.dataease.plugins.common.base.domain.DatasetTableIncrementalConfigExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DatasetTableIncrementalConfigMapper {
    long countByExample(DatasetTableIncrementalConfigExample example);

    int deleteByExample(DatasetTableIncrementalConfigExample example);

    int deleteByPrimaryKey(String id);

    int insert(DatasetTableIncrementalConfig record);

    int insertSelective(DatasetTableIncrementalConfig record);

    List<DatasetTableIncrementalConfig> selectByExample(DatasetTableIncrementalConfigExample example);

    DatasetTableIncrementalConfig selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") DatasetTableIncrementalConfig record, @Param("example") DatasetTableIncrementalConfigExample example);

    int updateByExample(@Param("record") DatasetTableIncrementalConfig record, @Param("example") DatasetTableIncrementalConfigExample example);

    int updateByPrimaryKeySelective(DatasetTableIncrementalConfig record);

    int updateByPrimaryKey(DatasetTableIncrementalConfig record);
}