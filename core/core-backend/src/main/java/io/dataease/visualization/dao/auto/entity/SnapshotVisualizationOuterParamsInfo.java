package io.dataease.visualization.dao.auto.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@Entity
@Table(name = "snapshot_visualization_outer_params_info")
public class SnapshotVisualizationOuterParamsInfo {
    @Id
    @Size(max = 50)
    @Comment("主键")
    @Column(name = "params_info_id", nullable = false, length = 50)
    private String paramsInfoId;

    @Size(max = 50)
    @Comment("visualization_outer_params 表的 ID")
    @Column(name = "params_id", length = 50)
    private String paramsId;

    @Size(max = 255)
    @Comment("参数名")
    @Column(name = "param_name")
    private String paramName;

    @Comment("是否启用")
    @Column(name = "checked")
    private Boolean checked;

    @Size(max = 255)
    @Comment("复制来源")
    @Column(name = "copy_from")
    private String copyFrom;

    @Size(max = 50)
    @Comment("复制来源ID")
    @Column(name = "copy_id", length = 50)
    private String copyId;

    @Comment("是否必填")
    @ColumnDefault("false")
    @Column(name = "required")
    private Boolean required;

    @Size(max = 255)
    @Comment("默认值 JSON格式")
    @Column(name = "default_value")
    private String defaultValue;

    @Comment("是否启用默认值")
    @ColumnDefault("false")
    @Column(name = "enabled_default")
    private Boolean enabledDefault;

}
