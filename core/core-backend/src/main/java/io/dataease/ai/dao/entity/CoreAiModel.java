package io.dataease.ai.dao.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@Entity
@Table(name = "core_ai_model", uniqueConstraints = @UniqueConstraint(name = "uk_ai_model_name", columnNames = "name_key"))
@Comment("AI 服务配置")
public class CoreAiModel extends AiConfigEntity {
    @Id
    @Column(name = "id", nullable = false)
    @Comment("id")
    private Long id;
    @Column(name = "name", length = 128, nullable = false)
    @Comment("name")
    private String name;
    @Column(name = "name_key", length = 256, nullable = false)
    @Comment("name_key")
    private String nameKey;
    @Column(name = "provider", length = 32, nullable = false)
    @Comment("provider")
    private String provider = "OPENAI_COMPATIBLE";
    @Column(name = "base_url", length = 2048, nullable = false)
    @Comment("base_url")
    private String baseUrl;
    @Column(name = "model_identifier", length = 256, nullable = false)
    @Comment("model_identifier")
    private String modelIdentifier;
    @Column(name = "key_envelope", length = 8192)
    @Comment("key_envelope")
    private String keyEnvelope;

    @Column(name = "credential_revision", nullable = false)
    @Comment("credential_revision")
    private Long credentialRevision = 1L;

    @Column(name = "config_revision", nullable = false)
    @Comment("config_revision")
    private Long configRevision = 1L;

    @Column(name = "validated_revision")
    @Comment("validated_revision")
    private Long validatedRevision;
    @Column(name = "output_token_parameter", length = 32, nullable = false)
    @Comment("两项测试确定的输出上限参数能力")
    private String outputTokenParameter = "MAX_TOKENS";

    @Column(name = "enabled", nullable = false)
    @Comment("enabled")
    private Boolean enabled = true;
    @Column(name = "processing_location", length = 16, nullable = false)
    @Comment("processing_location")
    private String processingLocation = "UNKNOWN";
    @Column(name = "processing_note", length = 512)
    @Comment("processing_note")
    private String processingNote;
    @Column(name = "last_test_status", length = 16, nullable = false)
    @Comment("last_test_status")
    private String lastTestStatus = "UNTESTED";

    @Column(name = "last_test_time")
    @Comment("last_test_time")
    private Long lastTestTime;
    @Column(name = "last_test_error_code", length = 64)
    @Comment("last_test_error_code")
    private String lastTestErrorCode;

    @Column(name = "deleted_at")
    @Comment("deleted_at")
    private Long deletedAt;
}
