package io.dataease.visualization.dao.auto.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@Entity
@Table(name = "snapshot_visualization_outer_params_target_view_info")
public class SnapshotVisualizationOuterParamsTargetViewInfo {
    @Id
    @Comment("主键")
    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Comment("visualization_outer_params_info 表的 ID")
    @Column(name = "params_info_id")
    private Long paramsInfoId;

    @Comment("联动视图ID/联动过滤项ID")
    @Column(name = "target_view_id")
    private Long targetViewId;

    @Comment("联动字段ID")
    @Column(name = "target_field_id")
    private String targetFieldId;

    @Size(max = 255)
    @Comment("复制来源")
    @Column(name = "copy_from")
    private String copyFrom;

    @Size(max = 50)
    @Comment("复制来源ID")
    @Column(name = "copy_id", length = 50)
    private String copyId;

    @Comment("联动数据集id/联动过滤组件id")
    @Column(name = "target_ds_id")
    private Long targetDsId;

    @Size(max = 50)
    @Comment("匹配方式")
    @Column(name = "match_mode", length = 50)
    private String matchMode;

}
