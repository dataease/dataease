package io.dataease.base.mapper;

import io.dataease.base.domain.DatasetTableTaskLog;
import io.dataease.base.domain.DatasetTableTaskLogExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DatasetTableTaskLogMapper {
    long countByExample(DatasetTableTaskLogExample example);

    int deleteByExample(DatasetTableTaskLogExample example);

    int deleteByPrimaryKey(String id);

    int insert(DatasetTableTaskLog record);

    int insertSelective(DatasetTableTaskLog record);

    List<DatasetTableTaskLog> selectByExampleWithBLOBs(DatasetTableTaskLogExample example);

    List<DatasetTableTaskLog> selectByExample(DatasetTableTaskLogExample example);

    DatasetTableTaskLog selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") DatasetTableTaskLog record, @Param("example") DatasetTableTaskLogExample example);

    int updateByExampleWithBLOBs(@Param("record") DatasetTableTaskLog record, @Param("example") DatasetTableTaskLogExample example);

    int updateByExample(@Param("record") DatasetTableTaskLog record, @Param("example") DatasetTableTaskLogExample example);

    int updateByPrimaryKeySelective(DatasetTableTaskLog record);

    int updateByPrimaryKeyWithBLOBs(DatasetTableTaskLog record);

    int updateByPrimaryKey(DatasetTableTaskLog record);
}