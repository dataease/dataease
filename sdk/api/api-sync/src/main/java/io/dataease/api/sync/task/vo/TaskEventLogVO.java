package io.dataease.api.sync.task.vo;

import io.dataease.api.sync.task.dto.TaskLogType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 统一列表的轻量记录，字段快照按需读取
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskEventLogVO extends TaskLogVO {
    private TaskLogType taskType;
    private boolean deletable;
}
