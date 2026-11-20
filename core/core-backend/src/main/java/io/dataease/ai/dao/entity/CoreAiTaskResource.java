package io.dataease.ai.dao.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

/** One current owner per trusted canonical datasource/table/operation key across all nodes. */
@Getter
@Setter
@Entity
@Table(name = "core_ai_task_resource")
public class CoreAiTaskResource {
    @Id
    @Column(name = "resource_key", length = 64, nullable = false)
    @Comment("SHA-256 of canonical datasource, physical table and scene operation")
    private String resourceKey;
    @Column(name = "task_id", nullable = false)
    @Comment("Owning task")
    private Long taskId;
    @Column(name = "attempt_id", nullable = false)
    @Comment("Owning attempt; release must match this exact attempt")
    private Long attemptId;
}
