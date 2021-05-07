package io.dataease.base.mapper;

import io.dataease.base.domain.DatasetTableUnion;
import io.dataease.base.domain.DatasetTableUnionExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DatasetTableUnionMapper {
    long countByExample(DatasetTableUnionExample example);

    int deleteByExample(DatasetTableUnionExample example);

    int deleteByPrimaryKey(String id);

    int insert(DatasetTableUnion record);

    int insertSelective(DatasetTableUnion record);

    List<DatasetTableUnion> selectByExample(DatasetTableUnionExample example);

    DatasetTableUnion selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") DatasetTableUnion record, @Param("example") DatasetTableUnionExample example);

    int updateByExample(@Param("record") DatasetTableUnion record, @Param("example") DatasetTableUnionExample example);

    int updateByPrimaryKeySelective(DatasetTableUnion record);

    int updateByPrimaryKey(DatasetTableUnion record);
}