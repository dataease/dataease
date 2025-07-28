package io.dataease.dataset.dao.auto.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@Comment("table数据集查询sql日志")
@Entity
@Table(name = "core_dataset_table_sql_log")
public class CoreDatasetTableSqlLog {
    @Id
    @Size(max = 50)
    @Comment("ID")
    @ColumnDefault("''")
    @Column(name = "id", nullable = false, length = 50)
    private String id;

    @Size(max = 50)
    @NotNull
    @Comment("数据集SQL节点ID")
    @ColumnDefault("''")
    @Column(name = "table_id", nullable = false, length = 50)
    private String tableId;

    @Comment("开始时间")
    @Column(name = "start_time")
    private Long startTime;

    @Comment("结束时间")
    @Column(name = "end_time")
    private Long endTime;

    @Comment("耗时(毫秒)")
    @Column(name = "spend")
    private Long spend;

    @NotNull
    @Column(name = "`sql`", nullable = false, length = 16777216)
    private String sql;

    @Size(max = 45)
    @Comment("状态")
    @Column(name = "status", length = 45)
    private String status;

}
