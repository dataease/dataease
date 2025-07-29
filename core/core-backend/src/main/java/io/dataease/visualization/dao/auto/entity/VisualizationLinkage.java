package io.dataease.visualization.dao.auto.entity;

import io.dataease.dao.auto.entity.CoreChartView;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

import java.util.List;

@Getter
@Setter
@Comment("联动记录表")
@Entity
@Table(name = "visualization_linkage")
public class VisualizationLinkage {
    @Id
    @Comment("主键")
    @Column(name = "id", nullable = false)
    private Long id;

    @Comment("联动大屏/仪表板ID")
    @Column(name = "dv_id")
    private Long dvId;

    @Comment("源视图id")
    @Column(name = "source_view_id")
    private Long sourceViewId;

    @Comment("联动视图id")
    @Column(name = "target_view_id")
    private Long targetViewId;

    @Comment("更新时间")
    @Column(name = "update_time")
    private Long updateTime;

    @Size(max = 255)
    @Comment("更新人")
    @Column(name = "update_people")
    private String updatePeople;

    @Comment("是否启用关联")
    @ColumnDefault("false")
    @Column(name = "linkage_active")
    private Boolean linkageActive;

    @Size(max = 2000)
    @Comment("扩展字段1")
    @Column(name = "ext1", length = 2000)
    private String ext1;

    @Size(max = 2000)
    @Comment("扩展字段2")
    @Column(name = "ext2", length = 2000)
    private String ext2;

    @Comment("复制来源")
    @Column(name = "copy_from")
    private Long copyFrom;

    @Comment("复制来源ID")
    @Column(name = "copy_id")
    private Long copyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_view_id", referencedColumnName = "id", insertable = false, updatable = false)
    private CoreChartView sourceView;

    @OneToMany(fetch = FetchType.LAZY)
    private List<VisualizationLinkageField> linkageFields;

}
