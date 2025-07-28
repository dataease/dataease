package io.dataease.visualization.dao.auto.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@Comment("主题表")
@Entity
@Table(name = "visualization_subject")
public class VisualizationSubject {
    @Id
    @Size(max = 50)
    @Column(name = "id", nullable = false, length = 50)
    private String id;

    @Size(max = 255)
    @Comment("主题名称")
    @Column(name = "name")
    private String name;

    @Size(max = 255)
    @Comment("主题类型 system 系统主题，self 自定义主题")
    @Column(name = "type")
    private String type;

    @Column(name = "details", length = 16777216)
    private String details;

    @Comment("删除标记")
    @ColumnDefault("false")
    @Column(name = "delete_flag")
    private Boolean deleteFlag;

    @Column(name = "cover_url", length = 16777216)
    private String coverUrl;

    @NotNull
    @Comment("创建序号")
    @ColumnDefault("0")
    @Column(name = "create_num", nullable = false)
    private Integer createNum;

    @Comment("创建时间")
    @Column(name = "create_time")
    private Long createTime;

    @Size(max = 255)
    @Comment("创建人")
    @Column(name = "create_by")
    private String createBy;

    @Comment("更新时间")
    @Column(name = "update_time")
    private Long updateTime;

    @Size(max = 255)
    @Comment("更新人")
    @Column(name = "update_by")
    private String updateBy;

    @Comment("删除时间")
    @Column(name = "delete_time")
    private Long deleteTime;

    @Comment("删除人")
    @Column(name = "delete_by")
    private Long deleteBy;

}
