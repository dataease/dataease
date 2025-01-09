package io.dataease.datasource.dao.ext.mapper;

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
                    SELECT QRTZ_TRIGGERS.* 
                    FROM QRTZ_TRIGGERS
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
