package io.dataease.qrtz.dao.auto.repo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@Entity
@Table(name = "QRTZ_LOCKS")
@IdClass(QrtzLockId.class)
@Comment("Quartz锁表，为多个节点调度提供分布式锁（开源作业调度框架Quartz）")
public class QrtzLock {
    @Id
    @Column(name = "SCHED_NAME", length = 120, nullable = false)
    private String schedName;

    @Id
    @Column(name = "LOCK_NAME", length = 40, nullable = false)
    private String lockName;

}
