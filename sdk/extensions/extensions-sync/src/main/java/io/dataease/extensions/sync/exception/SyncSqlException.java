package io.dataease.extensions.sync.exception;

import io.dataease.exception.DEException;
import io.dataease.extensions.sync.vo.DatasourceConfiguration;
import io.dataease.result.ResultCode;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 保留调度使用的原始错误消息，单独提供脱敏的 SQL 诊断文本
 */
public class SyncSqlException extends DEException {
    private static final Pattern JDBC_URL = Pattern.compile("(?i)jdbc:[^\\s\\\"'<>]+");
    private static final Pattern CREDENTIAL = Pattern.compile(
            "(?i)([\\\"']?(?:password|passwd|pwd|username|user|token|secret|api[_-]?key|access[_-]?key)[\\\"']?\\s*[=:]\\s*)(?:\\\"[^\\\"]*\\\"|'[^']*'|[^\\s,;&}]+)");
    private static final Pattern USER_INFO = Pattern.compile("(://)[^/\\s@]+@");
    private final String diagnosticMessage;
    private final boolean executionUncertain;

    public SyncSqlException(String message, Throwable error, DatasourceConfiguration configuration) {
        this(message, error, configuration, false);
    }

    /**
     * SQL 已返回成功后发生的连接关闭异常不能被当作数据库拒绝，避免错误开放再次执行
     */
    public SyncSqlException(String message, Throwable error, DatasourceConfiguration configuration, boolean statementCompleted) {
        super(ResultCode.SYSTEM_INNER_ERROR.code(), message);
        // 只有数据库明确返回的 SQL 错误才表示失败，断线、超时和缺少 SQLState 都不能据此重发 DDL
        executionUncertain = statementCompleted || !(error instanceof SQLException sql) || sql.getSQLState() == null
                || sql.getSQLState().startsWith("08") || sql.getSQLState().startsWith("HYT")
                || "40003".equals(sql.getSQLState())
                || error instanceof java.sql.SQLTimeoutException || error instanceof java.sql.SQLRecoverableException;
        StringBuilder details = new StringBuilder(redact(message, configuration));
        Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        Throwable current = error;
        int depth = 0;
        while (current != null && seen.add(current) && depth++ < 12) {
            details.append("\nCaused by: ").append(current.getClass().getName());
            if (current instanceof SQLException sql && sql.getSQLState() != null) {
                details.append(" [SQLState=").append(sql.getSQLState()).append(']');
            }
            if (current.getMessage() != null) {
                details.append(": ").append(redact(current.getMessage(), configuration));
            }
            current = current.getCause();
        }
        String safe = details.toString();
        diagnosticMessage = safe.length() > 12000 ? safe.substring(0, 12000) + "\n[truncated]" : safe;
    }

    public String getDiagnosticMessage() {
        return diagnosticMessage;
    }

    /**
     * 供 DDL 收尾区分数据库明确拒绝与提交后结果未知，不向调用方暴露未脱敏的异常链
     */
    public boolean isExecutionUncertain() {
        return executionUncertain;
    }

    /**
     * 隐藏 JDBC URL、URL 用户信息、常见凭据字段及已知配置值
     */
    private static String redact(String text, DatasourceConfiguration configuration) {
        text = JDBC_URL.matcher(text).replaceAll("jdbc:***");
        text = USER_INFO.matcher(text).replaceAll("$1***@");
        text = CREDENTIAL.matcher(text).replaceAll("$1***");
        if (configuration != null) {
            String[] values = {configuration.getPassword(), configuration.getSshPassword(),
                    configuration.getSshKey(), configuration.getSshKeyPassword(),
                    configuration.getUsername(), configuration.getSshUserName(),
                    configuration.getJdbcUrl(), configuration.getUrl(), configuration.getJdbc(),
                    configuration.getHost(), configuration.getSshHost(), configuration.getDataBase()};
            for (String value : values) {
                if (value != null && !value.isEmpty()) {
                    text = text.replace(value, "***");
                    text = text.replace(URLEncoder.encode(value, StandardCharsets.UTF_8), "***");
                }
            }
            if (configuration.getParameters() != null) {
                for (var entry : configuration.getParameters().entrySet()) {
                    if (entry.getKey() != null && entry.getKey().matches("(?i).*(password|passwd|pwd|token|secret|key|user).*")
                            && entry.getValue() != null && !entry.getValue().isEmpty()) {
                        text = text.replace(entry.getValue(), "***")
                                .replace(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8), "***");
                    }
                }
            }
        }
        return text;
    }
}
