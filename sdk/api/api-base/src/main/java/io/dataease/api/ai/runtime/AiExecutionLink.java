package io.dataease.api.ai.runtime;

/** Trusted lifecycle coordinates for execution observers; never populated from a client identity. */
public record AiExecutionLink(String taskId, String attemptId, int attemptNumber, String sourceAttemptId) { }
