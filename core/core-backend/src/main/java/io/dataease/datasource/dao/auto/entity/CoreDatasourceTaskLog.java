package io.dataease.datasource.dao.auto.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@Comment("数据源定时同步任务执行日志")
@Entity
@Table(name = "core_datasource_task_log", indexes = {
        @Index(name = "idx_dst_task_log_A", columnList = "ds_id, table_name, start_time"),
        @Index(name = "idx_dst_task_log_ds_id", columnList = "ds_id"),
        @Index(name = "idx_dst_task_log_task_id", columnList = "task_id")
})
public class CoreDatasourceTaskLog {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Comment("主键")
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Comment("数据源ID")
    @Column(name = "ds_id", nullable = false)
    private Long dsId;

    @Comment("任务ID")
    @Column(name = "task_id")
    private Long taskId;

    @Comment("开始时间")
    @Column(name = "start_time")
    private Long startTime;

    @Comment("结束时间")
    @Column(name = "end_time")
    private Long endTime;

    @Size(max = 50)
    @NotNull
    @Comment("执行状态")
    @Column(name = "task_status", nullable = false, length = 50)
    private String taskStatus;

    @Size(max = 255)
    @NotNull
    @Comment("执行状态")
    @Column(name = "table_name", nullable = false)
    private String tableName;

    @Lob
    @Column(name = "info", length = 16777216)
    private String info;

    @Comment("创建时间")
    @Column(name = "create_time")
    private Long createTime;

    @Size(max = 45)
    @Comment("更新频率类型")
    @Column(name = "trigger_type", length = 45)
    private String triggerType;

}
