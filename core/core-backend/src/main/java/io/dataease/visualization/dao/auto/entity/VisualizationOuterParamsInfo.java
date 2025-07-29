package io.dataease.visualization.dao.auto.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@Comment("外部参数配置表")
@Entity
@Table(name = "visualization_outer_params_info")
public class VisualizationOuterParamsInfo {
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

    @Column(name = "default_value", length = 16777216)
    private String defaultValue;

    @Comment("是否启用默认值")
    @ColumnDefault("false")
    @Column(name = "enabled_default")
    private Boolean enabledDefault;

}
