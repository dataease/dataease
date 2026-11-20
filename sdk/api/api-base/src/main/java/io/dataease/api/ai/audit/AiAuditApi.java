package io.dataease.api.ai.audit;

import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.util.List;

/** Bounded administrator-only detail. Task/result APIs continue to expose only owner-safe projections. */
@RequestMapping("/ai/audit")
public interface AiAuditApi {
    record Entry(@JsonSerialize(using = ToStringSerializer.class) Long id, Integer sequence, String stepId, @JsonSerialize(using = ToStringSerializer.class) Long previousEntryId, String kind,
            Integer technicalRetries, Integer corrections, String requestBody, String responseBody,
            String usageJson, Long startedAt, Long finishedAt, String status, String errorCode, Boolean incomplete,
            @JsonSerialize(using = ToStringSerializer.class) Long modelId, Long modelRevision, Long credentialRevision, Long settingRevision, String modelIdentifier) { }
    record Detail(String executionId, String taskId, String attemptId, String sourceAttemptId, Integer attemptNumber,
            @JsonSerialize(using = ToStringSerializer.class) Long actorId, @JsonSerialize(using = ToStringSerializer.class) Long organizationId, @JsonSerialize(using = ToStringSerializer.class) Long proxyOrganizationId, String feature, String resourceReference,
            String deploymentMode, @JsonSerialize(using = ToStringSerializer.class) Long modelId, Long modelRevision, Long credentialRevision, Long settingRevision,
            String modelIdentifier, Long startedAt, Long finishedAt, Long retentionExpiresAt, String status,
            Boolean incomplete, Long gapCount, Integer lastSequence, boolean entriesTruncated, List<Entry> entries) { }
    @GetMapping("/{executionId}") Detail get(@PathVariable String executionId);
}
