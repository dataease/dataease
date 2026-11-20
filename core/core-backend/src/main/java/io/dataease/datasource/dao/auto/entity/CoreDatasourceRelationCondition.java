package io.dataease.datasource.dao.auto.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@Comment("数据源表关联条件")
@Entity
@Table(name = "core_datasource_relation_condition", uniqueConstraints = @UniqueConstraint(name = "uk_ds_relation_condition", columnNames = {"relation_id", "condition_order"}))
public class CoreDatasourceRelationCondition {
    @Id
    @Comment("记录主键")
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "relation_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Comment("所属表关联")
    private CoreDatasourceRelation relation;

    @Comment("条件顺序，从 0 开始")
    @Column(name = "condition_order", nullable = false)
    private int position;

    @Comment("左表物理字段名")
    @Column(name = "left_field", length = 512, nullable = false)
    private String leftField;

    @Comment("右表物理字段名")
    @Column(name = "right_field", length = 512, nullable = false)
    private String rightField;

    @Comment("左表存在时，关联字段是否缺失")
    @Column(name = "left_field_missing", nullable = false)
    private boolean leftFieldMissing;

    @Comment("右表存在时，关联字段是否缺失")
    @Column(name = "right_field_missing", nullable = false)
    private boolean rightFieldMissing;

    @Size(max = 50)
    @Comment("创建人 ID")
    @Column(name = "create_by", length = 50, updatable = false)
    private String createBy;

    @Comment("创建时间，Unix 毫秒时间戳")
    @Column(name = "create_time", updatable = false)
    private Long createTime;
}
