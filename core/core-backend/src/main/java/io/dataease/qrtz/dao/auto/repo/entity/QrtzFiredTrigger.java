package io.dataease.qrtz.dao.auto.repo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@Entity
@Table(name = "QRTZ_FIRED_TRIGGERS")
@IdClass(QrtzFiredTriggerId.class)
@Comment("存储已经触发的trigger相关信息（开源作业调度框架Quartz）")
public class QrtzFiredTrigger {

    @Id
    @Column(name = "SCHED_NAME", nullable = false, length = 120)
    private String schedName;

   @Id
    @Column(name = "ENTRY_ID", nullable = false, length = 95)
    private String entryId;

    @Size(max = 200)
    @NotNull
    @Column(name = "TRIGGER_NAME", nullable = false, length = 200)
    private String triggerName;

    @Size(max = 200)
    @NotNull
    @Column(name = "TRIGGER_GROUP", nullable = false, length = 200)
    private String triggerGroup;

    @Size(max = 200)
    @NotNull
    @Column(name = "INSTANCE_NAME", nullable = false, length = 200)
    private String instanceName;

    @NotNull
    @Column(name = "FIRED_TIME", nullable = false)
    private Long firedTime;

    @NotNull
    @Column(name = "SCHED_TIME", nullable = false)
    private Long schedTime;

    @NotNull
    @Column(name = "PRIORITY", nullable = false)
    private Integer priority;

    @Size(max = 16)
    @NotNull
    @Column(name = "STATE", nullable = false, length = 16)
    private String state;

    @Size(max = 200)
    @Column(name = "JOB_NAME", length = 200)
    private String jobName;

    @Size(max = 200)
    @Column(name = "JOB_GROUP", length = 200)
    private String jobGroup;

    @Size(max = 16)
    @Column(name = "IS_NONCONCURRENT", length = 16)
    private String isNonconcurrent;

    @Size(max = 16)
    @Column(name = "REQUESTS_RECOVERY", length = 16)
    private String requestsRecovery;

}
