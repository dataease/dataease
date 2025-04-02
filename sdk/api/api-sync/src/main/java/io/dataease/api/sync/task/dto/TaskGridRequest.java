package io.dataease.api.sync.task.dto;

import io.dataease.model.KeywordRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class TaskGridRequest extends KeywordRequest implements Serializable {
    private List<String> logStatus;
    private List<String> status;
    private List<String> lastExecuteTime;
    private List<String> nextExecuteTime;
}
