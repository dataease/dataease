package io.dataease.api.sync.task.dto;

import io.dataease.model.KeywordRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class TaskLogGridRequest extends KeywordRequest implements Serializable {
    private String taskId;
    private List<String> status;
    private List<String> lastExecuteTime;
}
