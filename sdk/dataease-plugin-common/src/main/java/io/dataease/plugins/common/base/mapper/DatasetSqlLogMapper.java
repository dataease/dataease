package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.DatasetSqlLog;
import io.dataease.plugins.common.base.domain.DatasetSqlLogExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DatasetSqlLogMapper {
    long countByExample(DatasetSqlLogExample example);

    int deleteByExample(DatasetSqlLogExample example);

    int deleteByPrimaryKey(String id);

    int insert(DatasetSqlLog record);

    int insertSelective(DatasetSqlLog record);

    List<DatasetSqlLog> selectByExample(DatasetSqlLogExample example);

    DatasetSqlLog selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") DatasetSqlLog record, @Param("example") DatasetSqlLogExample example);

    int updateByExample(@Param("record") DatasetSqlLog record, @Param("example") DatasetSqlLogExample example);

    int updateByPrimaryKeySelective(DatasetSqlLog record);

    int updateByPrimaryKey(DatasetSqlLog record);
}