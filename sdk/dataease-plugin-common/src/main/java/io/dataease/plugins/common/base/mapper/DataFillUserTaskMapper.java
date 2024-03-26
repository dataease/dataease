package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.DataFillUserTask;
import io.dataease.plugins.common.base.domain.DataFillUserTaskExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DataFillUserTaskMapper {
    long countByExample(DataFillUserTaskExample example);

    int deleteByExample(DataFillUserTaskExample example);

    int deleteByPrimaryKey(String id);

    int insert(DataFillUserTask record);

    int insertSelective(DataFillUserTask record);

    List<DataFillUserTask> selectByExample(DataFillUserTaskExample example);

    DataFillUserTask selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") DataFillUserTask record, @Param("example") DataFillUserTaskExample example);

    int updateByExample(@Param("record") DataFillUserTask record, @Param("example") DataFillUserTaskExample example);

    int updateByPrimaryKeySelective(DataFillUserTask record);

    int updateByPrimaryKey(DataFillUserTask record);
}