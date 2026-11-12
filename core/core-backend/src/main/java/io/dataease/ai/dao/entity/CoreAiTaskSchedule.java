package io.dataease.ai.dao.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

/** Independent shared coordination row; only short acceptance/claim/terminal transactions hold it. */
@Getter
@Setter
@Entity
@Table(name = "core_ai_task_schedule")
public class CoreAiTaskSchedule {
    @Id
    @Column(name = "id", nullable = false)
    @Comment("Singleton scheduling coordinator")
    private Long id = 1L;
    @Column(name = "last_sequence", nullable = false)
    @Comment("Monotonic acceptance order; independent of node clocks")
    private Long lastSequence = 0L;
    @Column(name = "last_actor_id")
    @Comment("Last claimed real actor; persistent round-robin cursor")
    private Long lastActorId;
}
