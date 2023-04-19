package io.dataease.commons.constants;

public enum TaskStatus {
    WaitingForExecution,  // 等待执行
    Stopped,            // 停止
    Suspend,         // 暂停
    UnderExecution,        // 执行中
    Completed,          //完成
    Error,               //错误

    Warning               //警告
}
