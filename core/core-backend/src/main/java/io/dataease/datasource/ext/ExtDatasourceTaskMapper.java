package io.dataease.datasource.ext;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.datasource.dto.CoreDatasourceTaskDTO;
import io.dataease.request.GridExample;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @Author gin
 * @Date 2021/3/9 3:26 下午
 */
@Mapper
public interface ExtDatasourceTaskMapper {


    @Select(
            """     
                    SELECT core_datasource.name as datasource_name,core_datasource_task.* , QRTZ_TRIGGERS.NEXT_FIRE_TIME as NEXT_FIRE_TIME
                    FROM core_datasource_task
                    left join core_datasource on core_datasource.id=core_datasource_task.ds_id
                    left join QRTZ_TRIGGERS on core_datasource_task.id=QRTZ_TRIGGERS.TRIGGER_NAME
                     ${ew.customSqlSegment}
                             """
    )
    @Results(
            id = "taskWithTriggers",
            value = {
                    @Result(property = "id", column = "id"),
                    @Result(property = "datasourceName", column = "datasource_name"),
                    @Result(property = "dsId", column = "ds_id"),
                    @Result(property = "nextExecTime", column = "NEXT_FIRE_TIME")
            }
    )
    List<CoreDatasourceTaskDTO> taskWithTriggers(@Param("ew") QueryWrapper queryWrapper);


}
