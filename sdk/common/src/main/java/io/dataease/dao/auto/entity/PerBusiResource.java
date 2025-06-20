package io.dataease.dao.auto.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@Entity
@Table(name = "per_busi_resource")
public class PerBusiResource {
    @Id
    @Comment("资源ID")
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 255)
    @NotNull
    @Comment("名称")
    @Convert(disableConversion = true)
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Comment("类型ID")
    @Column(name = "rt_id", nullable = false)
    private Integer rtId;

    @Comment("所属组织ID")
    @Column(name = "org_id")
    private Long orgId;

    @NotNull
    @Comment("上级资源ID")
    @Column(name = "pid", nullable = false)
    private Long pid;

    @Size(max = 255)
    @Comment("寻根路径")
    @Convert(disableConversion = true)
    @Column(name = "root_path")
    private String rootPath;

    @NotNull
    @Comment("叶子结点")
    @ColumnDefault("0")
    @Column(name = "leaf", nullable = false)
    private Boolean leaf = false;

    @NotNull
    @Comment("拓展标识")
    @ColumnDefault("0")
    @Column(name = "extra_flag", nullable = false)
    private Integer extraFlag;

    @NotNull
    @Comment("拓展字段1")
    @ColumnDefault("1")
    @Column(name = "extra_flag1", nullable = false)
    private Integer extraFlag1;

    @NotNull
    @Comment("创建时间")
    @ColumnDefault("0")
    @Column(name = "create_time", nullable = false)
    private Long createTime;

    @NotNull
    @Comment("创建者")
    @Column(name = "creator", nullable = false)
    private Long creator;

}
