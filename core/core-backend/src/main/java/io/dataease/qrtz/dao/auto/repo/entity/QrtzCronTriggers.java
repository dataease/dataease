package io.dataease.qrtz.dao.auto.repo.entity;

import jakarta.persistence.Entity;
import lombok.Data;
import jakarta.persistence.*;
import org.hibernate.annotations.Comment;

@Data
@Entity
@Table(name = "QRTZ_CRON_TRIGGERS")
@IdClass(QrtzCronTriggersId.class)
@Comment("CronTrigger存储（开源作业调度框架Quartz）")
public class QrtzCronTriggers {

    @Id
    @Column(name = "SCHED_NAME", length = 120, nullable = false)
    private String schedName;

    @Id
    @Column(name = "TRIGGER_NAME", length = 200, nullable = false)
    private String triggerName;

    @Id
    @Column(name = "TRIGGER_GROUP", length = 200, nullable = false)
    private String triggerGroup;

    @Column(name = "CRON_EXPRESSION", length = 120, nullable = false)
    private String cronExpression;

    @Column(name = "TIME_ZONE_ID", length = 80)
    private String timeZoneId;

    @OneToOne(fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumns({
            @PrimaryKeyJoinColumn(name = "SCHED_NAME", referencedColumnName = "SCHED_NAME"),
            @PrimaryKeyJoinColumn(name = "TRIGGER_NAME", referencedColumnName = "TRIGGER_NAME"),
            @PrimaryKeyJoinColumn(name = "TRIGGER_GROUP", referencedColumnName = "TRIGGER_GROUP")
    })
    private QrtzTriggers trigger;
}

