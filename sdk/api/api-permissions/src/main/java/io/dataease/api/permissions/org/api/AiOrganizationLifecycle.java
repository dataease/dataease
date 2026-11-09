package io.dataease.api.permissions.org.api;

/** 组织删除的内部事务钩子，使用与配置写入相同的协调顺序并移除策略。 */
public interface AiOrganizationLifecycle {
    void beforeDelete(Long organizationId);
}
