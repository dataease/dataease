package io.dataease.qrtz.dao.auto.repo.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;

import java.sql.Blob;

@Data
@Entity
@Table(name = "QRTZ_JOB_DETAILS")
@IdClass(QrtzJobDetailsId.class)
@Comment("存储jobDetails信息（开源作业调度框架Quartz）")
public class QrtzJobDetails {

    @Id
    @Column(name = "SCHED_NAME", length = 120, nullable = false)
    private String schedName;

    @Id
    @Column(name = "JOB_NAME", length = 200, nullable = false)
    private String jobName;

    @Id
    @Column(name = "JOB_GROUP", length = 200, nullable = false)
    private String jobGroup;

    @Column(name = "DESCRIPTION", length = 250)
    private String description;

    @Column(name = "JOB_CLASS_NAME", length = 250, nullable = false)
    private String jobClassName;

    @Column(name = "IS_DURABLE", nullable = false)
    private Boolean isDurable;

    @Column(name = "IS_NONCONCURRENT", nullable = false)
    private Boolean isNonconcurrent;

    @Column(name = "IS_UPDATE_DATA", nullable = false)
    private Boolean isUpdateData;

    @Column(name = "REQUESTS_RECOVERY", nullable = false)
    private Boolean requestsRecovery;

    @Lob
    @Column(name = "JOB_DATA", length = 16777216)
    private Blob jobData;
}

