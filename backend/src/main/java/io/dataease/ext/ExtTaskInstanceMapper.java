package io.dataease.ext;

import io.dataease.dto.TaskInstance;

import java.util.List;

public interface ExtTaskInstanceMapper {

    int runningCount(String taskId);

    void resetRunnings(String taskId);

    void update(TaskInstance taskInstance);

    List<TaskInstance> select();

}
