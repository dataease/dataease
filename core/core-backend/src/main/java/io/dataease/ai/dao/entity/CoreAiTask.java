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
@Table(name = "core_ai_task")
public class CoreAiTask {
    @Column(name = "actor_id")
    @Comment("Real submitting actor; shared quota across workspaces and proxy contexts")
    private Long actorId;
    @Id
    @Column(name = "id", nullable = false)
    @Comment("id")
    private Long id;
    @Column(name = "current_attempt_id", nullable = false)
    @Comment("current_attempt_id")
    private Long currentAttemptId;
    @Column(name = "owner_binding", length = 512, nullable = false)
    @Comment("owner_binding")
    private String ownerBinding;
    @Column(name = "submission_hash", length = 64, nullable = false)
    @Comment("submission_hash")
    private String submissionHash;
    @Column(name = "input_hash", length = 64, nullable = false)
    @Comment("input_hash")
    private String inputHash;
    @Column(name = "owner_json", length = 2048, nullable = false)
    @Comment("owner_json")
    private String ownerJson;
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "input_json", length = 262144, nullable = false)
    @Comment("input_json")
    private String inputJson;
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "context_json", length = 262144, nullable = false)
    @Comment("context_json")
    private String contextJson;
    @Column(name = "created_at", nullable = false)
    @Comment("created_at")
    private Long createdAt;
}
