package io.dataease.datasource.dao.auto.entity;

import io.dataease.dao.auto.entity.CoreDatasource;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;

@Getter
@Setter
@Comment("数据源表语义")
@Entity
@Table(name = "core_datasource_table_semantic", uniqueConstraints = @UniqueConstraint(name = "uk_ds_table_semantic", columnNames = {"datasource_id", "table_name"}))
public class CoreDatasourceTableSemantic {
    @Id
    @Comment("记录主键")
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "datasource_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Comment("所属数据源")
    private CoreDatasource datasource;

    @Comment("物理表名")
    @Column(name = "table_name", length = 512, nullable = false)
    private String tableName;

    @Comment("业务说明")
    @Column(name = "business_description", length = 4000)
    private String description;

    @Comment("数据粒度，例如一行代表一笔订单")
    @Column(name = "data_grain", length = 512)
    private String grain;

    @Comment("语义表是否已添加，手工移除后为 false")
    @Column(name = "active", nullable = false)
    private boolean active;

    @Comment("是否已确认")
    @Column(name = "confirmed", nullable = false)
    private boolean confirmed;

    @Comment("AI 置信度，范围为 0 到 1，未评分时为空")
    @Column(name = "confidence_score", precision = 5, scale = 4)
    private BigDecimal confidenceScore;

    @Comment("最近一次同步的物理表显示名称")
    @Column(name = "physical_label", length = 4000)
    private String physicalLabel;

    @Comment("最近一次同步时物理表是否存在")
    @Column(name = "available", nullable = false)
    private boolean available;

    @Size(max = 50)
    @Comment("创建人 ID")
    @Column(name = "create_by", length = 50, updatable = false)
    private String createBy;

    @Comment("创建时间，Unix 毫秒时间戳")
    @Column(name = "create_time", updatable = false)
    private Long createTime;
}
