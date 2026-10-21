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

    public SyncSqlException(String message, Throwable error, DatasourceConfiguration configuration) {
        super(ResultCode.SYSTEM_INNER_ERROR.code(), message);
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
