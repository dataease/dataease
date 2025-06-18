package io.dataease.dao.auto.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@Entity
@Comment("数据集分组表")
@Table(name = "core_dataset_group")
public class CoreDatasetGroup {
    @Id
    @Column(name = "id", nullable = false)
    @Comment("ID")
    private Long id;

    @Comment("名称")
    @Size(max = 128)
    @Column(name = "name", length = 128)
    private String name;

    @Comment("父级ID")
    @Column(name = "pid")
    private Long pid;

    @Comment("当前分组处于第几级")
    @ColumnDefault("0")
    @Column(name = "level")
    private Integer level;

    @Comment("node类型：folder or dataset")
    @Size(max = 50)
    @NotNull
    @Column(name = "node_type", nullable = false, length = 50)
    private String nodeType;

    @Comment("sql,union")
    @Size(max = 50)
    @Column(name = "type", length = 50)
    private String type;

    @Comment("连接模式：0-直连，1-同步(包括excel、api等数据存在de中的表)")
    @ColumnDefault("0")
    @Column(name = "mode")
    private Integer mode;

    @Comment("关联关系树")
    @Lob
    @Column(name = "info", length = 16777216)
    private String info;

    @Comment("创建人ID")
    @Size(max = 50)
    @Column(name = "create_by", length = 50)
    private String createBy;

    @Comment("创建时间")
    @Column(name = "create_time")
    private Long createTime;

    @Comment("Quartz 实例 ID")
    @Size(max = 1024)
    @Column(name = "qrtz_instance", length = 1024)
    private String qrtzInstance;

    @Comment("同步状态")
    @Size(max = 45)
    @Column(name = "sync_status", length = 45)
    private String syncStatus;

    @Comment("更新人ID")
    @Size(max = 50)
    @Column(name = "update_by", length = 50)
    private String updateBy;

    @Comment("最后同步时间")
    @ColumnDefault("0")
    @Column(name = "last_update_time")
    private Long lastUpdateTime;

    @Comment("关联sql")
    @Lob
    @Column(name = "union_sql", length = 16777216)
    private String unionSql;

    @Comment("是否跨源")
    @Column(name = "is_cross")
    private Boolean isCross;

    public CoreDatasetGroup() {
    }

    // 全参构造函数
    public CoreDatasetGroup(Long id, String name, Long pid, Integer level, String node_type, String type,
                            Integer mode, String info, String create_by, Long create_time, String qrtz_instance,
                            String sync_status, String update_by, Long last_update_time, String union_sql) {
        this.id = id;
        this.name = name;
        this.pid = pid;
        this.level = level;
        this.nodeType = node_type;
        this.type = type;
        this.mode = mode;
        this.info = info;
        this.createBy = create_by;
        this.createTime = create_time;
        this.qrtzInstance = qrtz_instance;
        this.syncStatus = sync_status;
        this.updateBy = update_by;
        this.lastUpdateTime = last_update_time;
        this.unionSql = union_sql;
    }

}
