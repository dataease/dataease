package io.dataease.template.dao.auto.entity;

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
@Comment("模板表")
@Entity
@Table(name = "visualization_template_category_map")
public class VisualizationTemplateCategoryMap {
    @Id
    @Size(max = 50)
    @Comment("主键")
    @Column(name = "id", nullable = false, length = 50)
    private String id;

    @Size(max = 255)
    @Comment("名称")
    @Column(name = "category_id")
    private String categoryId;

    @Size(max = 255)
    @Comment("父级id")
    @Column(name = "template_id")
    private String templateId;

}
