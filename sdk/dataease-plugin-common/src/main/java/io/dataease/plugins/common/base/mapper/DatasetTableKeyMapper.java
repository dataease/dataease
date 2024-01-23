package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.DatasetTableKey;
import io.dataease.plugins.common.base.domain.DatasetTableKeyExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DatasetTableKeyMapper {
    long countByExample(DatasetTableKeyExample example);

    int deleteByExample(DatasetTableKeyExample example);

    int deleteByPrimaryKey(String id);

    int insert(DatasetTableKey record);

    int insertSelective(DatasetTableKey record);

    List<DatasetTableKey> selectByExample(DatasetTableKeyExample example);

    DatasetTableKey selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") DatasetTableKey record, @Param("example") DatasetTableKeyExample example);

    int updateByExample(@Param("record") DatasetTableKey record, @Param("example") DatasetTableKeyExample example);

    int updateByPrimaryKeySelective(DatasetTableKey record);

    int updateByPrimaryKey(DatasetTableKey record);
}