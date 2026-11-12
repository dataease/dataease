package io.dataease.ai.dao.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Administrator-only execution evidence; never exposed through ordinary task projections. */
@Getter
@Setter
@Entity
@Table(name = "core_ai_audit_execution")
public class CoreAiAuditExecution {
    @Id
    @Column(name = "id", length = 100)
    @Comment("Runtime execution id")
    private String id;
    @Column(name = "task_id", length = 100)
    @Comment("Trusted task id, foreground or background")
    private String taskId;
    @Column(name = "attempt_id", length = 100)
    @Comment("Trusted attempt id")
    private String attemptId;
    @Column(name = "source_attempt_id", length = 100)
    @Comment("Manual retry source attempt")
    private String sourceAttemptId;
    @Column(name = "attempt_number")
    @Comment("Attempt number")
    private Integer attemptNumber;
    @Column(name = "actor_id")
    @Comment("Execution subject")
    private Long actorId;
    @Column(name = "organization_id")
    @Comment("Applicable workspace, null for global/community")
    private Long organizationId;
    @Column(name = "proxy_organization_id")
    @Comment("Proxy workspace")
    private Long proxyOrganizationId;
    @Column(name = "feature", length = 100)
    @Comment("Registered capability")
    private String feature;
    @Column(name = "resource_reference", length = 1000)
    @Comment("Server-authorized resource reference and version")
    private String resourceReference;
    @Column(name = "deployment_mode", length = 32)
    @Comment("Execution deployment mode")
    private String deploymentMode;
    @Column(name = "model_id")
    @Comment("Selected model")
    private Long modelId;
    @Column(name = "model_revision")
    @Comment("Selected model configuration revision")
    private Long modelRevision;
    @Column(name = "credential_revision")
    @Comment("Credential revision only, never the credential")
    private Long credentialRevision;
    @Column(name = "setting_revision")
    @Comment("Execution settings revision")
    private Long settingRevision;
    @Column(name = "model_identifier", length = 256)
    @Comment("Selected model identifier")
    private String modelIdentifier;
    @Column(name = "started_at")
    @Comment("First actual work time")
    private Long startedAt;
    @Column(name = "finished_at")
    @Comment("Attempt end time, independent from result delivery")
    private Long finishedAt;
    @Column(name = "retention_expires_at")
    @Comment("Audit retention expiry, independent from configurable result delivery")
    private Long retentionExpiresAt;
    @Column(name = "status", length = 24)
    @Comment("RUNNING COMPLETED FAILED CANCELLED")
    private String status;
    @Column(name = "incomplete")
    @Comment("Evidence is partial, degraded or has a capture gap")
    private Boolean incomplete;
    @Column(name = "gap_count")
    @Comment("Known missing audit writes")
    private Long gapCount;
    @Column(name = "last_sequence")
    @Comment("Last issued entry sequence")
    private Integer lastSequence;
}
