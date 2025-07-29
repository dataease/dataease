package io.dataease.qrtz.dao.auto.repo.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.sql.Blob;

@Data
@Entity
@Table(name = "QRTZ_TRIGGERS")
@IdClass(QrtzTriggersId.class)
@Comment("存储定义的trigger（开源作业调度框架Quartz）")
public class QrtzTriggers {

    @Id
    @Column(name = "SCHED_NAME", length = 120, nullable = false)
    private String schedName;

    @Id
    @Column(name = "TRIGGER_NAME", length = 200, nullable = false)
    private String triggerName;

    @Id
    @Column(name = "TRIGGER_GROUP", length = 200, nullable = false)
    private String triggerGroup;

    @Column(name = "JOB_NAME", length = 200, nullable = false)
    private String jobName;

    @Column(name = "JOB_GROUP", length = 200, nullable = false)
    private String jobGroup;

    @Column(name = "DESCRIPTION", length = 250)
    private String description;

    @Column(name = "NEXT_FIRE_TIME")
    private Long nextFireTime;

    @Column(name = "PREV_FIRE_TIME")
    private Long prevFireTime;

    @Column(name = "PRIORITY")
    private Integer priority;

    @Column(name = "TRIGGER_STATE", length = 16, nullable = false)
    private String triggerState;

    @Column(name = "TRIGGER_TYPE", length = 8, nullable = false)
    private String triggerType;

    @Column(name = "START_TIME", nullable = false)
    private Long startTime;

    @Column(name = "END_TIME")
    private Long endTime;

    @Column(name = "CALENDAR_NAME", length = 200)
    private String calendarName;

    @Column(name = "MISFIRE_INSTR")
    private Short misfireInstr;

    @Column(name = "JOB_DATA", length = 16777216)
    private byte[] jobData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "SCHED_NAME", referencedColumnName = "SCHED_NAME", insertable = false, updatable = false),
            @JoinColumn(name = "JOB_NAME", referencedColumnName = "JOB_NAME", insertable = false, updatable = false),
            @JoinColumn(name = "JOB_GROUP", referencedColumnName = "JOB_GROUP", insertable = false, updatable = false)
    })
    private QrtzJobDetails jobDetails;
}
