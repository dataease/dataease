package io.dataease.api.ai.runtime;

/** 由上下文工厂创建的不可变执行凭证，不是可由 HTTP 客户端构造的 DTO。 */
public interface AiExecutionContext {
    String executionId();
    String feature();
    Long actorId();
    Long organizationId();
    boolean background();
    Long proxyOrganizationId();
    long deadline();
    AiDataScope scope();
}
