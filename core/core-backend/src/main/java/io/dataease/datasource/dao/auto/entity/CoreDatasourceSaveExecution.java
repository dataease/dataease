package io.dataease.datasource.dao.auto.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@Comment("数据源保存执行记录")
@Entity
@Table(name = "core_datasource_save_execution", indexes = {
        @Index(name = "idx_ds_save_exec_ds_id", columnList = "ds_id"),
        @Index(name = "idx_ds_save_exec_status", columnList = "status")
})
public class CoreDatasourceSaveExecution {
    @Id
    @Comment("主键")
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Comment("数据源ID")
    @Column(name = "ds_id", nullable = false)
    private Long dsId;

    @Size(max = 64)
    @Comment("配置版本号")
    @Column(name = "config_version", length = 64)
    private String configVersion;

    @Size(max = 50)
    @NotNull
    @Comment("执行步骤")
    @Column(name = "step", nullable = false, length = 50)
    private String step;

    @Size(max = 50)
    @NotNull
    @Comment("执行状态")
    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "result", length = 16777216)
    private String result;

    @Comment("重试次数")
    @Column(name = "retry_count")
    private Integer retryCount;

    @Comment("创建时间")
    @Column(name = "create_time")
    private Long createTime;

    @Comment("更新时间")
    @Column(name = "update_time")
    private Long updateTime;

    @Size(max = 128)
    @Comment("执行节点标识")
    @Column(name = "node_id", length = 128)
    private String nodeId;

    @Comment("租约到期时间")
    @Column(name = "lease_time")
    private Long leaseTime;
}
