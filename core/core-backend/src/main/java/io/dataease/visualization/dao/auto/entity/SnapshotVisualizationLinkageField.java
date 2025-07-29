package io.dataease.visualization.dao.auto.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@Entity
@Table(name = "snapshot_visualization_linkage_field")
public class SnapshotVisualizationLinkageField {
    @Id
    @Comment("主键")
    @Column(name = "id", nullable = false)
    private Long id;

    @Comment("联动ID")
    @Column(name = "linkage_id")
    private Long linkageId;

    @Comment("源图表字段")
    @Column(name = "source_field")
    private Long sourceField;

    @Comment("目标图表字段")
    @Column(name = "target_field")
    private Long targetField;

    @Comment("更新时间")
    @Column(name = "update_time")
    private Long updateTime;

    @Comment("复制来源")
    @Column(name = "copy_from")
    private Long copyFrom;

    @Comment("复制来源ID")
    @Column(name = "copy_id")
    private Long copyId;

}
