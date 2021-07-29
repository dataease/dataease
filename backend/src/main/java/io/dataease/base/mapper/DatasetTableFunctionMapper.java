package io.dataease.base.mapper;

import io.dataease.base.domain.DatasetTableFunction;
import io.dataease.base.domain.DatasetTableFunctionExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DatasetTableFunctionMapper {
    long countByExample(DatasetTableFunctionExample example);

    int deleteByExample(DatasetTableFunctionExample example);

    int deleteByPrimaryKey(Long id);

    int insert(DatasetTableFunction record);

    int insertSelective(DatasetTableFunction record);

    List<DatasetTableFunction> selectByExampleWithBLOBs(DatasetTableFunctionExample example);

    List<DatasetTableFunction> selectByExample(DatasetTableFunctionExample example);

    DatasetTableFunction selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") DatasetTableFunction record, @Param("example") DatasetTableFunctionExample example);

    int updateByExampleWithBLOBs(@Param("record") DatasetTableFunction record, @Param("example") DatasetTableFunctionExample example);

    int updateByExample(@Param("record") DatasetTableFunction record, @Param("example") DatasetTableFunctionExample example);

    int updateByPrimaryKeySelective(DatasetTableFunction record);

    int updateByPrimaryKeyWithBLOBs(DatasetTableFunction record);

    int updateByPrimaryKey(DatasetTableFunction record);
}