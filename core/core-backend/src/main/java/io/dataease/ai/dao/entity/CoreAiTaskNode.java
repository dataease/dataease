package io.dataease.ai.dao.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

/** Persistent background execution facts; never serialized directly to clients. */
@Getter
@Setter
@Entity
@Table(name = "core_ai_task_node")
public class CoreAiTaskNode {
    @Id
    @Column(name = "id", length = 64, nullable = false)
    @Comment("id")
    private String id;
    @Column(name = "heartbeat_at", nullable = false)
    @Comment("heartbeat_at")
    private Long heartbeatAt;
}
