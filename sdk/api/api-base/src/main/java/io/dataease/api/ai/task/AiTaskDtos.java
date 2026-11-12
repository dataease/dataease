package io.dataease.api.ai.task;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.dataease.api.ai.conversation.AiConversationDtos.Result;

public final class AiTaskDtos {
    private AiTaskDtos() { }
    public record Origin(String sessionId, String contextVersion) { }
    public record Submit(String submissionKey, String capability, String scene, String resourceId, String editorId, String sql, String text, Origin origin) {
        public Submit(String submissionKey, String capability, String scene, String resourceId, String editorId, String sql, String text) {
            this(submissionKey, capability, scene, resourceId, editorId, sql, text, null);
        }
    }
    public record Notice(@JsonSerialize(using = ToStringSerializer.class) Long taskId,
                         @JsonSerialize(using = ToStringSerializer.class) Long attemptId,
                         int attemptNumber, String status, String errorCode, long expiresAt, boolean retryEligible) {
        public Notice(Long taskId, Long attemptId, int attemptNumber, String status, String errorCode, long expiresAt) {
            this(taskId, attemptId, attemptNumber, status, errorCode, expiresAt, false);
        }
    }
    public record Retry(String retryKey, Long expectedAttemptId) { }
    /** No identity snapshots, input bodies, credentials or internal ownership/fencing metadata. */
    public record Task(@JsonSerialize(using = ToStringSerializer.class) Long taskId, @JsonSerialize(using = ToStringSerializer.class) Long attemptId, int attemptNumber, String status, String scene, String resourceId,
                       String resourceName, long createdAt, Long startedAt, Long finishedAt, String errorCode,
                       @JsonSerialize(using = ToStringSerializer.class) Long modelId, Long modelRevision, String modelIdentifier, Result result, String executionId,
                       @JsonSerialize(using = ToStringSerializer.class) Long sourceAttemptId, boolean retryEligible,
                       io.dataease.api.ai.conversation.AiConversationDtos.RecoveryProgress recovery) {
        public Task(Long taskId, Long attemptId, int attemptNumber, String status, String scene, String resourceId, String resourceName,
                    long createdAt, Long startedAt, Long finishedAt, String errorCode, Long modelId, Long modelRevision, String modelIdentifier, Result result) {
            this(taskId, attemptId, attemptNumber, status, scene, resourceId, resourceName, createdAt, startedAt, finishedAt, errorCode, modelId, modelRevision, modelIdentifier,
                    result, null, null, false, null);
        }
    }
}
