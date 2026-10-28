package io.dataease.api.sync.task.api;

import java.util.Map;

/**
 * 同进程审计查询，不声明 HTTP 路由
 */
public interface SyncTaskAuditLookup {
    String queryTaskNodes(Long resourceId, String taskId);

    Long taskResourceId(String taskId);

    String queryLogNodes(Map<String, String> params);

    Long logResourceId(Map<String, String> params);
}
