package io.dataease.ai.dao.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @Column(name = "task_max_running", nullable = false)
    @Comment("Shared complete background task slots, independent of model HTTP permits")
    private Integer taskMaxRunning = 2;
    @Column(name = "task_max_queued", nullable = false)
    @Comment("Shared accepted waiting task limit")
    private Integer taskMaxQueued = 100;
    @Column(name = "task_max_queued_per_user", nullable = false)
    @Comment("Waiting task limit per real actor across workspaces")
    private Integer taskMaxQueuedPerUser = 10;
    @Column(name = "task_max_wait_seconds", nullable = false)
    @Comment("Waiting deadline snapshotted at acceptance")
    private Integer taskMaxWaitSeconds = 300;

    @Column(name = "revision", nullable = false)
    @Comment("revision")
    private Long revision = 1L;

    @JsonIgnore
    @Column(name = "key_source", length = 16)
    @Comment("AI 密钥来源，非空表示已经初始化")
    private String keySource;

    @JsonIgnore
    @Column(name = "key_ring", length = 4000)
    @Comment("数据库模式的独立 AI 密钥集合，不通过配置接口读写")
    private String keyRing;

    @JsonIgnore
    @Column(name = "active_key_id", length = 32)
    @Comment("当前 AI 加密和签名密钥标识")
    private String activeKeyId;

    @JsonIgnore
    @Column(name = "key_revision")
    @Comment("AI 密钥版本")
    private Long keyRevision;

    @JsonIgnore
    @Column(name = "key_fingerprint", length = 64)
    @Comment("AI 密钥集合及当前密钥的一致性摘要")
    private String keyFingerprint;
}
