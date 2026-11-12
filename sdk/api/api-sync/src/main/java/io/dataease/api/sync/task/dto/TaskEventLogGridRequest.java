package io.dataease.api.sync.task.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 页面统一日志筛选，原同步日志接口保持原有查询范围
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskEventLogGridRequest extends TaskLogGridRequest {
    private List<TaskLogType> taskTypes;
}
