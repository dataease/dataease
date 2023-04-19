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
//    List<DataSetTaskLogDTO> listTaskLog(GridExample example);
//
//    List<DataSetTaskLogDTO> listUserTaskLog(GridExample example);
//
//    List<DataSetTaskDTO> taskList(GridExample example);
//
//    List<DataSetTaskDTO> userTaskList(GridExample example);
//
//    List<DataSetTaskDTO> findByPanelId(@Param("panelId") String panelId);
//
//    List<DataSetTaskDTO> findByTableIds(@Param("tableIds") List tableIds);

    List<CoreDatasourceTaskDTO> taskWithTriggers(GridExample example);

    List<Long> lastId();

}
