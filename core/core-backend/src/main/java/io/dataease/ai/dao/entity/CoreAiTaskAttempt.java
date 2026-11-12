package io.dataease.ai.dao.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Persistent background execution facts; never serialized directly to clients. */
@Getter
@Setter
@Entity
@Table(name = "core_ai_task_attempt")
public class CoreAiTaskAttempt {
    @Id
    @Column(name = "id", nullable = false)
    @Comment("id")
    private Long id;
    @Column(name = "task_id", nullable = false)
    @Comment("task_id")
    private Long taskId;
    @Column(name = "source_attempt_id")
    @Comment("Original attempt selected for this retry operation")
    private Long sourceAttemptId;
    @Column(name = "attempt_number", nullable = false)
    @Comment("attempt_number")
    private Integer attemptNumber;
    @Column(name = "retry_hash", length = 64, nullable = false)
    @Comment("retry_hash")
    private String retryHash;
    @Column(name = "status", length = 24, nullable = false)
    @Comment("status")
    private String status;
    @Column(name = "node_id", length = 64)
    @Comment("node_id")
    private String nodeId;
    @Column(name = "fence", length = 64)
    @Comment("fence")
    private String fence;
    @Column(name = "created_at", nullable = false)
    @Comment("created_at")
    private Long createdAt;
    @Column(name = "started_at")
    @Comment("started_at")
    private Long startedAt;
    @Column(name = "finished_at")
    @Comment("finished_at")
    private Long finishedAt;
    @Column(name = "deadline")
    @Comment("deadline")
    private Long deadline;
    @Column(name = "error_code", length = 64)
    @Comment("error_code")
    private String errorCode;
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "result_json", length = 1048576)
    @Comment("result_json")
    private String resultJson;
    @Column(name = "model_id")
    @Comment("model_id")
    private Long modelId;
    @Column(name = "model_revision")
    @Comment("model_revision")
    private Long modelRevision;
    @Column(name = "credential_revision")
    @Comment("credential_revision")
    private Long credentialRevision;
    @Column(name = "model_identifier", length = 256)
    @Comment("model_identifier")
    private String modelIdentifier;
}
