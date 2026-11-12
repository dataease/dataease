package io.dataease.ai.dao.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@Entity
@Table(name = "core_ai_org_policy", indexes = @Index(name = "idx_ai_org_model", columnList = "model_id"))
@Comment("AI 服务配置")
public class CoreAiOrgPolicy extends AiConfigEntity {
    @Column(name = "usage_epoch", nullable = false)
    @Comment("Monotonic AI shutdown fence; capacity/recovery/retention do not change it")
    private Long usageEpoch = 0L;

    @Id
    @Column(name = "org_id", nullable = false)
    @Comment("org_id")
    private Long orgId;
    @Column(name = "policy_mode", length = 16, nullable = false)
    @Comment("mode")
    private String mode = "INHERIT";

    @Column(name = "model_id")
    @Comment("model_id")
    private Long modelId;
}
