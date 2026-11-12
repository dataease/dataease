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
@Table(name = "core_ai_audit_entry")
public class CoreAiAuditEntry {
    @Id
    @Column(name = "id")
    @Comment("Audit entry id")
    private Long id;
    @Column(name = "execution_id", length = 100)
    @Comment("Runtime execution id")
    private String executionId;
    @Column(name = "sequence")
    @Comment("Actual send or tool invocation order")
    private Integer sequence;
    @Column(name = "step_id", length = 100)
    @Comment("Recovery logical step id")
    private String stepId;
    @Column(name = "previous_entry_id")
    @Comment("Previous actual invocation of same logical step")
    private Long previousEntryId;
    @Column(name = "kind", length = 16)
    @Comment("MODEL or TOOL")
    private String kind;
    @Column(name = "technical_retries")
    @Comment("Attempt technical retry count at invocation")
    private Integer technicalRetries;
    @Column(name = "corrections")
    @Comment("Separate model correction count")
    private Integer corrections;
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "request_body", length = 8388608)
    @Comment("Actual payload excluding authentication and known system secret")
    private String requestBody;
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "response_body", length = 8388608)
    @Comment("Actual response consumed, including partial vendor process")
    private String responseBody;
    @Column(name = "usage_json", length = 4000)
    @Comment("Actual provider usage, null means unknown")
    private String usageJson;
    @Column(name = "started_at")
    @Comment("Actual invocation start")
    private Long startedAt;
    @Column(name = "finished_at")
    @Comment("Actual invocation end")
    private Long finishedAt;
    @Column(name = "status", length = 24)
    @Comment("SUCCEEDED FAILED CANCELLED")
    private String status;
    @Column(name = "error_code", length = 64)
    @Comment("Safe error category only")
    private String errorCode;
    @Column(name = "incomplete")
    @Comment("Failed/cancelled/truncated or redacted capture")
    private Boolean incomplete;
    @Column(name = "model_id")
    @Comment("Actual model id at this invocation")
    private Long modelId;
    @Column(name = "model_revision")
    @Comment("Actual model configuration revision")
    private Long modelRevision;
    @Column(name = "credential_revision")
    @Comment("Credential revision only")
    private Long credentialRevision;
    @Column(name = "setting_revision")
    @Comment("Actual settings revision")
    private Long settingRevision;
    @Column(name = "model_identifier", length = 256)
    @Comment("Actual model identifier")
    private String modelIdentifier;
}
