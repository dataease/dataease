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
@Table(name = "snapshot_visualization_outer_params")
public class SnapshotVisualizationOuterParams {
    @Id
    @Size(max = 50)
    @Comment("主键")
    @Column(name = "params_id", nullable = false, length = 50)
    private String paramsId;

    @Size(max = 50)
    @Comment("可视化资源ID")
    @Column(name = "visualization_id", length = 50)
    private String visualizationId;

    @Comment("是否启用外部参数标识（1-是，0-否）")
    @Column(name = "checked")
    private Boolean checked;

    @Size(max = 255)
    @Comment("备注")
    @Column(name = "remark")
    private String remark;

    @Size(max = 50)
    @Comment("复制来源")
    @Column(name = "copy_from", length = 50)
    private String copyFrom;

    @Size(max = 50)
    @Comment("复制来源ID")
    @Column(name = "copy_id", length = 50)
    private String copyId;

}
