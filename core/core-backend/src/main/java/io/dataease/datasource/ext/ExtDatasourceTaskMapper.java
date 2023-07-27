package io.dataease.datasource.ext;

import io.dataease.datasource.dto.CoreDatasourceTaskDTO;
import io.dataease.request.GridExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author gin
 * @Date 2021/3/9 3:26 下午
 */
@Mapper
public interface ExtDatasourceTaskMapper {

    List<CoreDatasourceTaskDTO> taskWithTriggers(GridExample example);

    List<Long> lastId();

}
