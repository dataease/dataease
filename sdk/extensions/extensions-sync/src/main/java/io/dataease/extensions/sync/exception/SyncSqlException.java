package io.dataease.extensions.sync.exception;

import io.dataease.exception.DEException;
import io.dataease.exception.ErrorCategory;
import io.dataease.extensions.sync.vo.DatasourceConfiguration;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 外部调用异常直接携带公共分类和脱敏详情，原始异常保留在 cause 中
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

    public SyncSqlException(String message, Throwable error, DatasourceConfiguration configuration, ErrorCategory category) {
        this(message, error, configuration, false, category);
    }

    /**
     * SQL 已返回成功后发生的连接关闭异常不能被当作数据库拒绝，避免错误开放再次执行
     */
    public SyncSqlException(String message, Throwable error, DatasourceConfiguration configuration, boolean statementCompleted) {
        this(message, error, configuration, statementCompleted, category(error));
    }

    private SyncSqlException(String message, Throwable error, DatasourceConfiguration configuration,
                             boolean statementCompleted, ErrorCategory category) {
        super(category.code(), DEException.of(category, category == ErrorCategory.INTERNAL
                ? "i18n_error_internal" : "i18n_sync_database_operation_failed").getMessage());
        setCategory(category);
        setMessageKey(category == ErrorCategory.INTERNAL ? "i18n_error_internal" : "i18n_sync_database_operation_failed");
        causedBy(error);
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
            if (current instanceof SQLException sql) {
                details.append(" [ErrorCode=").append(sql.getErrorCode()).append(']');
                if (sql.getSQLState() != null) details.append(" [SQLState=").append(sql.getSQLState()).append(']');
            }
            if (current.getMessage() != null) {
                details.append(": ").append(redact(current.getMessage(), configuration));
            }
            current = current.getCause();
        }
        String safe = details.toString();
        diagnosticMessage = safe.length() > 12000 ? safe.substring(0, 12000) + "\n[truncated]" : safe;
        if (category != ErrorCategory.INTERNAL) {
            String brief = getBriefDiagnosticMessage();
            int firstLineEnd = brief.indexOf('\n');
            setErrorData(Map.of("connectionDiagnostic", getMessage() + (firstLineEnd < 0 ? "" : brief.substring(firstLineEnd))));
        }
    }

    /**
     * 只识别外部调用的异常类型，不根据数据库报错文字或 SQLState 推断业务原因
     */
    private static ErrorCategory category(Throwable error) {
        ErrorCategory category = ErrorCategory.INTERNAL;
        Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Throwable cause = error; cause != null && seen.add(cause); cause = cause.getCause()) {
            if (cause instanceof java.sql.SQLTimeoutException || cause instanceof java.net.SocketTimeoutException) {
                return ErrorCategory.UPSTREAM_TIMEOUT;
            }
            if (cause instanceof SQLException || cause instanceof java.net.ConnectException
                    || cause instanceof java.net.UnknownHostException || cause instanceof java.net.NoRouteToHostException
                    || cause instanceof ClassNotFoundException) {
                category = ErrorCategory.UPSTREAM;
            }
        }
        return category;
    }

    public String getDiagnosticMessage() {
        return diagnosticMessage;
    }

    /**
     * 页面只展示主要原因链，省略完整 SQL、调用栈和过长消息
     */
    public String getBriefDiagnosticMessage() {
        java.util.List<String> causes = java.util.Arrays.stream(diagnosticMessage.split("\\n(?=Caused by: )"))
                .skip(1).distinct().toList();
        StringBuilder brief = new StringBuilder(diagnosticMessage.lines().findFirst().orElse(""));
        for (int index = 0; index < Math.min(causes.size(), 3); index++) {
            String line = causes.get(index == 2 ? causes.size() - 1 : index)
                    .replaceFirst("^Caused by: (?:[\\w$]+\\.)*", "Caused by: ")
                    .replaceAll("(?i)\\b(?:https?|ftp|sftp)://\\S+", "***")
                    .replaceAll("(?m)^\\s*at\\s+[^\\r\\n]+", "")
                    .replaceAll("(?is)\\b(?:select\\s+.+?\\s+from\\s+|insert\\s+into\\s+\\S+\\s*(?:\\(|values\\b|select\\b)|update\\s+\\S+\\s+set\\b|delete\\s+from\\s+\\S+(?:\\s+where\\b|\\s*;|\\s*$)|merge\\s+into\\s+\\S+\\s+using\\b|create\\s+table\\s+\\S+\\s*\\(|alter\\s+table\\s+\\S+\\s+(?:add|drop|modify|rename)\\b|drop\\s+table\\s+\\S+(?:\\s*;|\\s*$)).*", "[SQL omitted]");
            brief.append("\n").append(line.length() > 400 ? line.substring(0, 400) + "..." : line);
        }
        return brief.toString();
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
