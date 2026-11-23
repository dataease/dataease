package io.dataease.exportCenter.manage;

import java.util.Set;

/**
 * 导出任务执行状态。
 * 取消请求只置 CANCELLING；任务真正退出后才落 CANCELLED 终态。
 */
public enum ExportTaskState {
    QUEUED,
    RUNNING,
    CANCELLING,
    SUCCESS,
    FAILED,
    CANCELLED;

    private static final Set<ExportTaskState> TERMINAL = Set.of(SUCCESS, FAILED, CANCELLED);

    public boolean isTerminal() {
        return TERMINAL.contains(this);
    }

    public boolean canTransitionTo(ExportTaskState next) {
        return switch (this) {
            case QUEUED -> next == RUNNING || next == CANCELLING || next == FAILED;
            case RUNNING -> next == CANCELLING || next == SUCCESS || next == FAILED;
            case CANCELLING -> next == CANCELLED || next == FAILED;
            default -> false;
        };
    }

    /** 兼容旧客户端的库表状态映射。 */
    public String legacyStatus() {
        return switch (this) {
            case QUEUED -> "PENDING";
            case RUNNING, CANCELLING -> "IN_PROGRESS";
            case SUCCESS -> "SUCCESS";
            case FAILED -> "FAILED";
            case CANCELLED -> "FAILED";
        };
    }
}
