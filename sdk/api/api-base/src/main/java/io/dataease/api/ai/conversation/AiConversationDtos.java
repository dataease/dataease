package io.dataease.api.ai.conversation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** 临时开发验收契约：客户端不提供身份、模型配置或历史消息。 */
public final class AiConversationDtos {
    private AiConversationDtos() { }
    public record Resource(String id, String name) { }
    /** Finite projection of an editor's manual preview; SQL is the unexpanded Open.sql basis. */
    public enum PreviewOutcome { SUCCESS, UNKNOWN_COLUMN, UNKNOWN_TABLE, AMBIGUOUS_COLUMN, SYNTAX, TIMEOUT, FAILED }
    public record SqlPreview(String resourceId, Boolean isCross, PreviewOutcome outcome, Integer revision, Integer currentRevision) { }
    public record Open(@NotBlank @Pattern(regexp = "sql|dataset-sql") String scene,
                       @Pattern(regexp = "[0-9]{1,20}|") String resourceId,
                       @NotBlank @Size(max = 100) String editorId,
                       @Size(max = 30000) String sql, @Size(max = 100) String openKey,
                       @Size(max = 100) String previousSessionId, Boolean isCross,
                       @Size(max = 64) java.util.List<@NotBlank @Size(max = 256) String> systemVariableNames, SqlPreview preview) {
        public Open(String scene, String resourceId, String editorId, String sql, String openKey, String previousSessionId, Boolean isCross,
                    java.util.List<String> systemVariableNames) {
            this(scene, resourceId, editorId, sql, openKey, previousSessionId, isCross, systemVariableNames, null);
        }
        public Open(String scene, String resourceId, String editorId, String sql, String openKey, String previousSessionId, Boolean isCross) {
            this(scene, resourceId, editorId, sql, openKey, previousSessionId, isCross, null);
        }
        public Open(String scene, String resourceId, String editorId, String sql, String openKey, String previousSessionId) {
            this(scene, resourceId, editorId, sql, openKey, previousSessionId, null);
        }
        public Open(String scene, String resourceId, String editorId, String sql, String openKey) { this(scene, resourceId, editorId, sql, openKey, null); }
        public Open(String scene, String resourceId, String editorId, String sql) { this(scene, resourceId, editorId, sql, null, null); }
    }
    public record Session(String sessionId, String contextVersion, String scene, String resourceId,
                          String resourceName, String editorId, long expiresAt, String resourceVersion, String draftVersion) {
        public Session(String sessionId, String contextVersion, String scene, String resourceId, String resourceName, String editorId, long expiresAt) {
            this(sessionId, contextVersion, scene, resourceId, resourceName, editorId, expiresAt, null, null);
        }
    }
    public record Turn(@NotBlank @Size(max = 100) String contextVersion,
                       @NotBlank @Size(max = 30000) String text, @Size(max = 100) String submissionKey) {
        public Turn(String contextVersion, String text) { this(contextVersion, text, null); }
    }
    public record Invoke(@NotBlank @Pattern(regexp = "sql") String scene,
                         @Pattern(regexp = "[0-9]{1,20}|") String resourceId,
                         @NotBlank @Size(max = 100) String editorId,
                         @Size(max = 30000) String sql,
                         @NotBlank @Size(max = 30000) String text, @Size(max = 100) String submissionKey) {
        public Invoke(String scene, String resourceId, String editorId, String sql, String text) { this(scene, resourceId, editorId, sql, text, null); }
    }
    public record Retry(@NotBlank @Size(max = 100) String retryKey, @NotBlank @Size(max = 100) String expectedAttemptId) { }
    public record RecoveryProgress(String stepId, String stepKind, int technicalRetries, int corrections, String reason) { }
    public record Execution(String sessionId, String executionId, String contextVersion, String status, long deadline, Session context,
                            String taskId, String attemptId, int attemptNumber, String sourceAttemptId, boolean retryEligible, RecoveryProgress recovery) {
        public Execution(String sessionId, String executionId, String contextVersion, String status, long deadline, Session context,
                         String taskId, String attemptId, int attemptNumber, String sourceAttemptId, boolean retryEligible) {
            this(sessionId, executionId, contextVersion, status, deadline, context, taskId, attemptId, attemptNumber, sourceAttemptId, retryEligible, null);
        }
        public Execution(String sessionId, String executionId, String contextVersion, String status, long deadline, Session context) {
            this(sessionId, executionId, contextVersion, status, deadline, context, null, null, 1, null, false);
        }
        public Execution(String sessionId, String executionId, String contextVersion, String status, long deadline) {
            this(sessionId, executionId, contextVersion, status, deadline, null);
        }
    }
    public record Tool(String callId, String name, String status, java.util.Map<String, String> parameters, String result) { }
    public record Event(long sequence, String type, String text, String error, Tool tool) {
        public Event(long sequence, String type, String text, String error) { this(sequence, type, text, error, null); }
    }
    public record ValueMeaning(String value, String meaning) { }
    public record FieldEvidence(String name, String type, String comment, String businessName, String description,
                                java.util.List<ValueMeaning> values) { }
    public record JoinKey(String leftField, String rightField) {}
    public record RelationEvidence(String name, String description, String leftTable, String rightTable,
                                   java.util.List<JoinKey> conditions, String cardinality, String joinType) {}
    public record TableEvidence(String table, boolean inspected, String description, String grain,
                                java.util.List<FieldEvidence> fields, java.util.List<RelationEvidence> relations) {
        public TableEvidence(String table, boolean inspected, String description, String grain, java.util.List<FieldEvidence> fields) {
            this(table, inspected, description, grain, fields, java.util.List.of());
        }
    }
    public record Result(String type, String text, String sql, String resourceId, String resourceName,
                         String resourceVersion, String editorId, String draftVersion, java.util.List<TableEvidence> evidence) {
        public Result(String type, String text, String sql, String resourceId, String resourceName,
                      String resourceVersion, String editorId, String draftVersion) {
            this(type, text, sql, resourceId, resourceName, resourceVersion, editorId, draftVersion, java.util.List.of());
        }
    }
    public record Application(@NotBlank String contextVersion, @NotBlank String draftVersion,
                              @Pattern(regexp = "[0-9]{1,20}") String targetResourceId) {
        public Application(String contextVersion, String draftVersion) { this(contextVersion, draftVersion, null); }
    }
    public record Events(String sessionId, String executionId, String contextVersion, String status, long cursor, java.util.List<Event> events, Result result,
                         String taskId, String attemptId, int attemptNumber, String sourceAttemptId, boolean retryEligible, RecoveryProgress recovery) {
        public Events(String sessionId, String executionId, String contextVersion, String status, long cursor, java.util.List<Event> events, Result result,
                      String taskId, String attemptId, int attemptNumber, String sourceAttemptId, boolean retryEligible) {
            this(sessionId, executionId, contextVersion, status, cursor, events, result, taskId, attemptId, attemptNumber, sourceAttemptId, retryEligible, null);
        }
        public Events(String sessionId, String executionId, String contextVersion, String status, long cursor, java.util.List<Event> events, Result result) {
            this(sessionId, executionId, contextVersion, status, cursor, events, result, null, null, 1, null, false);
        }
        public Events(String sessionId, String executionId, String contextVersion, String status, long cursor, java.util.List<Event> events) {
            this(sessionId, executionId, contextVersion, status, cursor, events, null);
        }
    }
    public record Answer(String sessionId, String executionId, String contextVersion, String status, String text) { }
}
