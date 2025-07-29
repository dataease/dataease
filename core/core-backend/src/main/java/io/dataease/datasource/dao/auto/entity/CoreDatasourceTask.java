package io.dataease.datasource.dao.auto.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@Comment("数据源定时同步任务")
@Entity
@Table(name = "core_datasource_task")
public class CoreDatasourceTask {
    @Id
    @Comment("主键")
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Comment("数据源ID")
    @Column(name = "ds_id", nullable = false)
    private Long dsId;

    @Size(max = 255)
    @NotNull
    @Comment("任务名称")
    @Column(name = "name", nullable = false)
    private String name;

    @Size(max = 50)
    @NotNull
    @Comment("更新方式")
    @Column(name = "update_type", nullable = false, length = 50)
    private String updateType;

    @Comment("开始时间")
    @Column(name = "start_time")
    private Long startTime;

    @Size(max = 50)
    @NotNull
    @Comment("执行频率：0 一次性 1 cron")
    @Column(name = "sync_rate", nullable = false, length = 50)
    private String syncRate;

    @Size(max = 255)
    @Comment("cron表达式")
    @Column(name = "cron")
    private String cron;

    @Comment("简单重复间隔")
    @Column(name = "simple_cron_value")
    private Long simpleCronValue;

    @Size(max = 50)
    @Comment("简单重复类型：分、时、天")
    @Column(name = "simple_cron_type", length = 50)
    private String simpleCronType;

    @Size(max = 50)
    @Comment("结束限制 0 无限制 1 设定结束时间")
    @Column(name = "end_limit", length = 50)
    private String endLimit;

    @Comment("结束时间")
    @Column(name = "end_time")
    private Long endTime;

    @Comment("创建时间")
    @Column(name = "create_time")
    private Long createTime;

    @Comment("上次执行时间")
    @Column(name = "last_exec_time")
    private Long lastExecTime;

    @Size(max = 50)
    @Comment("上次执行结果")
    @Column(name = "last_exec_status", length = 50)
    private String lastExecStatus;

    @Column(name = "extra_data", length = 16777216)
    private String extraData;

    @Size(max = 50)
    @Comment("任务状态")
    @Column(name = "task_status", length = 50)
    private String taskStatus;

}
