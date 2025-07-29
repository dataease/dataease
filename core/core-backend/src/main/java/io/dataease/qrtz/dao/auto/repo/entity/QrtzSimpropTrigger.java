package io.dataease.qrtz.dao.auto.repo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "QRTZ_SIMPROP_TRIGGERS")
@IdClass(QrtzSimpropTriggerId.class)
@Comment("存储CalendarIntervalTrigger和DailyTimeIntervalTrigger两种类型的触发器（开源作业调度框架Quartz）")
public class QrtzSimpropTrigger {
    @Id
    @Column(name = "SCHED_NAME", length = 120, nullable = false)
    private String schedName;

    @Id
    @Column(name = "TRIGGER_NAME", length = 200, nullable = false)
    private String triggerName;

    @Id
    @Column(name = "TRIGGER_GROUP", length = 200, nullable = false)
    private String triggerGroup;

    @MapsId("id")
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @PrimaryKeyJoinColumns({
            @PrimaryKeyJoinColumn(name = "SCHED_NAME", referencedColumnName = "SCHED_NAME"),
            @PrimaryKeyJoinColumn(name = "TRIGGER_NAME", referencedColumnName = "TRIGGER_NAME"),
            @PrimaryKeyJoinColumn(name = "TRIGGER_GROUP", referencedColumnName = "TRIGGER_GROUP")
    })
    private QrtzTriggers qrtzTriggers;

    @Size(max = 512)
    @Column(name = "STR_PROP_1", length = 512)
    private String strProp1;

    @Size(max = 512)
    @Column(name = "STR_PROP_2", length = 512)
    private String strProp2;

    @Size(max = 512)
    @Column(name = "STR_PROP_3", length = 512)
    private String strProp3;

    @Column(name = "INT_PROP_1")
    private Integer intProp1;

    @Column(name = "INT_PROP_2")
    private Integer intProp2;

    @Column(name = "LONG_PROP_1")
    private Long longProp1;

    @Column(name = "LONG_PROP_2")
    private Long longProp2;

    @Column(name = "DEC_PROP_1", precision = 13, scale = 4)
    private BigDecimal decProp1;

    @Column(name = "DEC_PROP_2", precision = 13, scale = 4)
    private BigDecimal decProp2;

    @Size(max = 1)
    @Column(name = "BOOL_PROP_1", length = 1)
    private String boolProp1;

    @Size(max = 1)
    @Column(name = "BOOL_PROP_2", length = 1)
    private String boolProp2;

}
