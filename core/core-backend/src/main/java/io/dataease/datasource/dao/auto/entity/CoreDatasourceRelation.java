package io.dataease.datasource.dao.auto.entity;

import io.dataease.api.ds.DatasourceRelationApi.Cardinality;
import io.dataease.api.ds.DatasourceRelationApi.JoinType;
import io.dataease.dao.auto.entity.CoreDatasource;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Comment;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Comment("数据源表关联")
@Entity
@Table(name = "core_datasource_relation", indexes = @Index(name = "idx_ds_relation_source", columnList = "datasource_id"))
public class CoreDatasourceRelation {
    @Id
    @Comment("记录主键")
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "datasource_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Comment("所属数据源")
    private CoreDatasource datasource;

    @Comment("关联名称")
    @Column(name = "relation_name", length = 255, nullable = false)
    private String name;

    @Comment("左侧物理表名")
    @Column(name = "left_table", length = 512, nullable = false)
    private String leftTable;

    @Comment("右侧物理表名")
    @Column(name = "right_table", length = 512, nullable = false)
    private String rightTable;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Comment("从左表到右表的关系基数")
    @Column(name = "cardinality", length = 16, nullable = false)
    private Cardinality cardinality;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Comment("推荐的 SQL 连接方式")
    @Column(name = "join_type", length = 16, nullable = false)
    private JoinType joinType;

    @Comment("业务说明")
    @Column(name = "business_description", length = 4000)
    private String description;

    @Comment("是否已确认")
    @Column(name = "confirmed", nullable = false)
    private boolean confirmed;

    @Comment("AI 置信度，范围为 0 到 1，未评分时为空")
    @Column(name = "confidence_score", precision = 5, scale = 4)
    private BigDecimal confidenceScore;

    @OneToMany(mappedBy = "relation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<CoreDatasourceRelationCondition> conditions = new ArrayList<>();

    @Comment("关联引用的物理表和字段是否有效")
    @Column(name = "valid", nullable = false)
    private boolean valid;

    @Comment("左侧物理表是否缺失")
    @Column(name = "left_table_missing", nullable = false)
    private boolean leftTableMissing;

    @Comment("右侧物理表是否缺失")
    @Column(name = "right_table_missing", nullable = false)
    private boolean rightTableMissing;

    @Size(max = 50)
    @Comment("创建人 ID")
    @Column(name = "create_by", length = 50, updatable = false)
    private String createBy;

    @Comment("创建时间，Unix 毫秒时间戳")
    @Column(name = "create_time", updatable = false)
    private Long createTime;
}
