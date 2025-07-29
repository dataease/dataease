package io.dataease.template.dao.auto.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@Comment("模板表")
@Entity
@Table(name = "visualization_template_category")
public class VisualizationTemplateCategory {
    @Id
    @Size(max = 50)
    @Comment("主键")
    @Column(name = "id", nullable = false, length = 50)
    private String id;

    @Size(max = 255)
    @Comment("名称")
    @Column(name = "name")
    private String name;

    @Size(max = 255)
    @Comment("父级id")
    @Column(name = "pid")
    private String pid;

    @Comment("层级")
    @Column(name = "level")
    private Integer level;

    @Size(max = 255)
    @Comment("模版种类  dataV or dashboard 目录或者文件夹")
    @Column(name = "dv_type")
    private String dvType;

    @Size(max = 255)
    @Comment("节点类型  folder or panel 目录或者文件夹")
    @Column(name = "node_type")
    private String nodeType;

    @Size(max = 255)
    @Comment("创建人")
    @Column(name = "create_by")
    private String createBy;

    @Comment("创建时间")
    @Column(name = "create_time")
    private Long createTime;

    @Comment("缩略图")
    @Column(name = "snapshot", length = 16777216)
    private String snapshot;

    @Size(max = 255)
    @Comment("模版类型 system 系统内置 self 用户自建")
    @Column(name = "template_type")
    private String templateType;

}
