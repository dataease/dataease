package io.dataease.ai.dao.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@Entity
@Table(name = "core_ai_setting")
@Comment("AI 服务配置")
public class CoreAiSetting extends AiConfigEntity {
    @Id
    @Column(name = "id", nullable = false)
    @Comment("id")
    private Long id = 1L;

    @Column(name = "enabled", nullable = false)
    @Comment("enabled")
    private Boolean enabled = false;

    @Column(name = "default_model_id")
    @Comment("default_model_id")
    private Long defaultModelId;

    @Column(name = "allow_samples", nullable = false)
    @Comment("allow_samples")
    private Boolean allowSamples = false;

    @Column(name = "request_timeout_seconds", nullable = false)
    @Comment("request_timeout_seconds")
    private Integer requestTimeoutSeconds = 60;

    @Column(name = "max_input_chars", nullable = false)
    @Comment("max_input_chars")
    private Integer maxInputChars = 30000;

    @Column(name = "max_output_tokens", nullable = false)
    @Comment("max_output_tokens")
    private Integer maxOutputTokens = 4096;

    @Column(name = "interactive_concurrency", nullable = false)
    @Comment("interactive_concurrency")
    private Integer interactiveConcurrency = 5;

    @Column(name = "background_concurrency", nullable = false)
    @Comment("background_concurrency")
    private Integer backgroundConcurrency = 2;

    @Column(name = "revision", nullable = false)
    @Comment("revision")
    private Long revision = 1L;
}
