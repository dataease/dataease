package io.dataease.plugins.common.base.mapper;

import io.dataease.plugins.common.base.domain.ExportTask;
import io.dataease.plugins.common.base.domain.ExportTaskExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ExportTaskMapper {
    long countByExample(ExportTaskExample example);

    int deleteByExample(ExportTaskExample example);

    int deleteByPrimaryKey(String id);

    int insert(ExportTask record);

    int insertSelective(ExportTask record);

    List<ExportTask> selectByExampleWithBLOBs(ExportTaskExample example);

    List<ExportTask> selectByExample(ExportTaskExample example);

    ExportTask selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") ExportTask record, @Param("example") ExportTaskExample example);

    int updateByExampleWithBLOBs(@Param("record") ExportTask record, @Param("example") ExportTaskExample example);

    int updateByExample(@Param("record") ExportTask record, @Param("example") ExportTaskExample example);

    int updateByPrimaryKeySelective(ExportTask record);

    int updateByPrimaryKeyWithBLOBs(ExportTask record);

    int updateByPrimaryKey(ExportTask record);
}