package io.dataease.exportCenter.dao.ext.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.dataease.exportCenter.dao.auto.entity.CoreExportTask;
import io.dataease.model.ExportTaskDTO;
import org.apache.ibatis.annotations.*;


@Mapper
public interface ExportTaskExtMapper extends BaseMapper<CoreExportTask> {

    @Select(
            """     
                    select * 
                    from core_export_task
                    ${ew.customSqlSegment} 
                    """
    )
    @Results(
            id = "exportTasksMap",
            value = {
                    @Result(property = "id", column = "id"),
                    @Result(property = "user_id", column = "userId"),
                    @Result(property = "file_name", column = "fileName"),
                    @Result(property = "file_size", column = "fileSize"),
                    @Result(property = "file_size_unit", column = "fileSizeUnit"),
                    @Result(property = "export_from", column = "exportFrom"),
                    @Result(property = "export_status", column = "exportStatus"),
                    @Result(property = "msg", column = "msg"),
                    @Result(property = "export_from_type", column = "exportFromType"),
                    @Result(property = "export_progress", column = "exportProgress"),
                    @Result(property = "export_machine_name", column = "exportMachineName"),
                    @Result(property = "export_from_name", column = "exportFromName"),
                    @Result(property = "org_name", column = "orgName"),
                    @Result(property = "export_time", column = "exportTime")
            }
    )
    IPage<ExportTaskDTO> pager(IPage<ExportTaskDTO> page, @Param("ew") QueryWrapper queryWrapper);


}
