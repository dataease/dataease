package io.dataease.api.sync.task.dto;

/**
 * 统一任务日志的记录类型，不参与同步运行状态判断
 */
public enum TaskLogType {
    DATA_SYNC,
    FIELD_CHANGE
}
