package io.dataease.extensions.sync.model.datasource;

/**
 * 同步数据源角色。
 *
 * <p>该值同时持久化在 per_sync_datasource.datasource_role、任务 parameter JSON 和插件元数据中，
 * 必须保持统一：1 表示源端，2 表示目标端。集中定义可避免 JPA 查询、插件注册和任务构建各自使用魔法值。</p>
 */
public final class DatasourceRole {

    public static final int SOURCE = 1;
    public static final int TARGET = 2;

    private DatasourceRole() {
    }

    public static boolean isValid(Integer role) {
        return role != null && (role == SOURCE || role == TARGET);
    }

    public static String displayName(Integer role) {
        if (role == null) {
            return "空";
        }
        return switch (role) {
            case SOURCE -> "源端(1)";
            case TARGET -> "目标端(2)";
            default -> "未知数据源（源端/目标端）(" + role + ")";
        };
    }
}
