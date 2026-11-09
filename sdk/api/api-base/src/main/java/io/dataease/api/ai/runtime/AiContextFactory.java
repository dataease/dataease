package io.dataease.api.ai.runtime;

public interface AiContextFactory {
    AiExecutionContext current(String registeredFeature, AiDataScope scope);
    /** 仅供受控任务系统调用；由已注册功能恢复并重新检查任务真实执行身份。 */
    AiExecutionContext task(String registeredFeature, String taskId, AiDataScope scope);
}
