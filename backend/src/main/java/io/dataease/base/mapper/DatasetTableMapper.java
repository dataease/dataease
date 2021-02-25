package io.dataease.base.mapper;

import io.dataease.base.domain.DatasetTable;
import io.dataease.base.domain.DatasetTableExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DatasetTableMapper {
    long countByExample(DatasetTableExample example);

    int deleteByExample(DatasetTableExample example);

    int deleteByPrimaryKey(String id);

    int insert(DatasetTable record);

    int insertSelective(DatasetTable record);

    List<DatasetTable> selectByExampleWithBLOBs(DatasetTableExample example);

    List<DatasetTable> selectByExample(DatasetTableExample example);

    DatasetTable selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") DatasetTable record, @Param("example") DatasetTableExample example);

    int updateByExampleWithBLOBs(@Param("record") DatasetTable record, @Param("example") DatasetTableExample example);

    int updateByExample(@Param("record") DatasetTable record, @Param("example") DatasetTableExample example);

    int updateByPrimaryKeySelective(DatasetTable record);

    int updateByPrimaryKeyWithBLOBs(DatasetTable record);

    int updateByPrimaryKey(DatasetTable record);
}