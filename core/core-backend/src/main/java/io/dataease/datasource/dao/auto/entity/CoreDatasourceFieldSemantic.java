package io.dataease.datasource.dao.auto.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;

@Getter
@Setter
@Comment("数据源字段语义")
@Entity
@Table(name = "core_datasource_field_semantic", uniqueConstraints = @UniqueConstraint(name = "uk_ds_field_semantic", columnNames = {"table_semantic_id", "field_name"}))
public class CoreDatasourceFieldSemantic {
    @Id
    @Comment("记录主键")
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "table_semantic_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Comment("所属语义表")
    private CoreDatasourceTableSemantic tableSemantic;

    @Comment("物理字段名")
    @Column(name = "field_name", length = 512, nullable = false)
    private String fieldName;

    @Comment("字段的业务名称")
    @Column(name = "business_name", length = 255)
    private String businessName;

    @Comment("业务说明")
    @Column(name = "business_description", length = 4000)
    private String description;

    @Comment("字段的业务别名")
    @Column(name = "business_aliases", length = 1000)
    private String aliases;

    @Comment("是否已确认")
    @Column(name = "confirmed", nullable = false)
    private boolean confirmed;

    @Comment("AI 置信度，范围为 0 到 1，未评分时为空")
    @Column(name = "confidence_score", precision = 5, scale = 4)
    private BigDecimal confidenceScore;

    @Comment("最近一次同步的物理字段类型")
    @Column(name = "physical_type", length = 512)
    private String physicalType;

    @Comment("最近一次同步的物理字段备注")
    @Column(name = "physical_comment", length = 4000)
    private String physicalComment;

    @Comment("字段在物理表中的顺序，从 0 开始")
    @Column(name = "field_order", nullable = false)
    private int position;

    @Size(max = 50)
    @Comment("创建人 ID")
    @Column(name = "create_by", length = 50, updatable = false)
    private String createBy;

    @Comment("创建时间，Unix 毫秒时间戳")
    @Column(name = "create_time", updatable = false)
    private Long createTime;
}
