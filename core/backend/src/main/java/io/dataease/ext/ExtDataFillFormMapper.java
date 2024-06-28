package io.dataease.ext;

import io.dataease.controller.request.datafill.DataFillFormRequest;
import io.dataease.controller.request.datafill.DataFillTaskSearchRequest;
import io.dataease.dto.datafill.DataFillCommitLogDTO;
import io.dataease.dto.datafill.DataFillFormDTO;
import io.dataease.dto.datafill.DataFillTaskDTO;
import io.dataease.dto.datafill.DataFillUserTaskDTO;
import io.dataease.plugins.common.base.domain.DataFillTask;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ExtDataFillFormMapper {
    List<DataFillFormDTO> search(DataFillFormRequest request);

    Map<String, String> searchChildrenIds(String id, String type);

    List<DataFillCommitLogDTO> selectLatestLogByFormDataIds(String formId, List<String> dataIds);

    List<DataFillCommitLogDTO> selectDataFillLogs(String formId, String commitByName);

    List<DataFillTaskDTO> selectDataFillTasks(DataFillTaskSearchRequest request);
    List<DataFillTask> selectActiveDataFillTasks();

    List<DataFillUserTaskDTO> listTodoUserTask(long userId, Date current, String taskName);
    List<DataFillUserTaskDTO> listFinishedUserTask(long userId, Date current, String taskName);
    List<DataFillUserTaskDTO> listExpiredUserTask(long userId, Date current, String taskName);
}
