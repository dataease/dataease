package io.dataease.base.mapper;

import io.dataease.base.domain.DatasetTableIncrementalConfig;
import io.dataease.base.domain.DatasetTableIncrementalConfigExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DatasetTableIncrementalConfigMapper {
    long countByExample(DatasetTableIncrementalConfigExample example);

    int deleteByExample(DatasetTableIncrementalConfigExample example);

    int insert(DatasetTableIncrementalConfig record);

    int insertSelective(DatasetTableIncrementalConfig record);

    List<DatasetTableIncrementalConfig> selectByExample(DatasetTableIncrementalConfigExample example);

    int updateByExampleSelective(@Param("record") DatasetTableIncrementalConfig record, @Param("example") DatasetTableIncrementalConfigExample example);

    int updateByExample(@Param("record") DatasetTableIncrementalConfig record, @Param("example") DatasetTableIncrementalConfigExample example);
}