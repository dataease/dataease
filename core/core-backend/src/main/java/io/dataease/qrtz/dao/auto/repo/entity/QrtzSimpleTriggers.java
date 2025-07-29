package io.dataease.qrtz.dao.auto.repo.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.io.Serializable;

@Data
@Entity
@Table(name = "QRTZ_SIMPLE_TRIGGERS")
@IdClass(QrtzSimpleTriggersId.class)
@Comment("SimpleTrigger存储（开源作业调度框架Quartz）")
public class QrtzSimpleTriggers implements Serializable {

    @Id
    @Column(name = "SCHED_NAME", length = 120, nullable = false)
    private String schedName;

    @Id
    @Column(name = "TRIGGER_NAME", length = 200, nullable = false)
    private String triggerName;

    @Id
    @Column(name = "TRIGGER_GROUP", length = 200, nullable = false)
    private String triggerGroup;

    @Column(name = "REPEAT_COUNT", nullable = false)
    private Long repeatCount;

    @Column(name = "REPEAT_INTERVAL", nullable = false)
    private Long repeatInterval;

    @Column(name = "TIMES_TRIGGERED", nullable = false)
    private Long timesTriggered;

    @OneToOne(fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumns({
            @PrimaryKeyJoinColumn(name = "SCHED_NAME", referencedColumnName = "SCHED_NAME"),
            @PrimaryKeyJoinColumn(name = "TRIGGER_NAME", referencedColumnName = "TRIGGER_NAME"),
            @PrimaryKeyJoinColumn(name = "TRIGGER_GROUP", referencedColumnName = "TRIGGER_GROUP")
    })
    private QrtzTriggers trigger;
}
