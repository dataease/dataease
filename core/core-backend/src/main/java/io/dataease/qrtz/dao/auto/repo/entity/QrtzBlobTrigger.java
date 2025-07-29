package io.dataease.qrtz.dao.auto.repo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.sql.Blob;

@Getter
@Setter
@Entity
@Table(name = "QRTZ_BLOB_TRIGGERS")
@IdClass(QrtzBlobTriggerId.class)
@Comment("自定义触发器存储（开源作业调度框架Quartz）")
public class QrtzBlobTrigger {

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "SCHED_NAME", referencedColumnName = "SCHED_NAME", nullable = false),
            @JoinColumn(name = "TRIGGER_NAME", referencedColumnName = "TRIGGER_NAME", nullable = false),
            @JoinColumn(name = "TRIGGER_GROUP", referencedColumnName = "TRIGGER_GROUP", nullable = false)
    })
    private QrtzTriggers qrtzTriggers;


    @Id
    @Column(name = "SCHED_NAME", nullable = false, length = 120)
    private String schedName;


    @Id
    @Column(name = "TRIGGER_NAME", nullable = false, length = 200)
    private String triggerName;


    @Id
    @Column(name = "TRIGGER_GROUP", nullable = false, length = 200)
    private String triggerGroup;

    @Column(name = "BLOB_DATA", length = 16777216)
    private byte[] blobData;

}
