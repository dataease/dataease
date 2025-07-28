package io.dataease.template.dao.auto.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@Comment("模板视图明细信息表")
@Entity
@Table(name = "visualization_template_extend_data")
public class VisualizationTemplateExtendData {
    @Id
    @Comment("主键")
    @Column(name = "id", nullable = false)
    private Long id;

    @Comment("模板ID")
    @Column(name = "dv_id")
    private Long dvId;

    @Comment("视图ID")
    @Column(name = "view_id")
    private Long viewId;

    @Comment("视图详情")
    @Column(name = "view_details", length = 16777216)
    private String viewDetails;

    @Size(max = 255)
    @Comment("复制来源")
    @Column(name = "copy_from")
    private String copyFrom;

    @Size(max = 255)
    @Comment("复制来源ID")
    @Column(name = "copy_id")
    private String copyId;

}
