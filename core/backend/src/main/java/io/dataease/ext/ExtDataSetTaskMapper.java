package io.dataease.ext;

import io.dataease.controller.dataset.request.DataSetTaskInstanceGridRequest;
import io.dataease.controller.dataset.request.DatasetTaskGridRequest;
import io.dataease.dto.dataset.DataSetTaskDTO;
import io.dataease.dto.dataset.DataSetTaskLogDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author gin
 * @Date 2021/3/9 3:26 下午
 */
@Mapper
public interface ExtDataSetTaskMapper {
    List<DataSetTaskLogDTO> listTaskLog(DataSetTaskInstanceGridRequest request);

    List<DataSetTaskLogDTO> listUserTaskLog(DataSetTaskInstanceGridRequest request);

    List<DataSetTaskDTO> taskList(DatasetTaskGridRequest request);

    List<DataSetTaskDTO> userTaskList(DatasetTaskGridRequest request);

    String datasetPrivileges(DatasetTaskGridRequest request);

    List<DataSetTaskDTO> taskWithTriggers(DatasetTaskGridRequest request);

    List<DataSetTaskDTO> findByPanelId(@Param("panelId") String panelId);

    List<DataSetTaskDTO> findByTableIds(@Param("tableIds") List tableIds);
}
