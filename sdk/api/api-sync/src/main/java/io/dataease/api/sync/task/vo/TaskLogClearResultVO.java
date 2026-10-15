package io.dataease.api.sync.task.vo;

/**
 * 当前用户最近一次日志清理结果
 */
public record TaskLogClearResultVO(String id, String status, int total, String message,
                                   long createdTime, long updatedTime) {
}
