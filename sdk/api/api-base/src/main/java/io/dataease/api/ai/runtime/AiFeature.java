package io.dataease.api.ai.runtime;

/** 后端功能 Bean；不向 HTTP 暴露。业务负责资源、数据及样本确认依据的当前校验。 */
public interface AiFeature {
    String code();
    void authorize(AiExecutionContext context);
    /** finalPrompt 已完成模板及 Advisor 增补；必须用 scope 检查资源/权限版本及实际输入。 */
    void authorizeInput(AiExecutionContext context, org.springframework.ai.chat.prompt.Prompt finalPrompt);

    default void checkSampleConfirmation(AiExecutionContext context) {
        throw new UnsupportedOperationException("Sample confirmation unavailable");
    }
    default TaskIdentity taskIdentity(String taskId) {
        throw new UnsupportedOperationException("Background AI unavailable");
    }
    record TaskIdentity(Long actorId, Long organizationId, Long proxyOrganizationId, String identityVersion) {
        public TaskIdentity(Long actorId, Long organizationId, String identityVersion) { this(actorId, organizationId, null, identityVersion); }
    }
}
