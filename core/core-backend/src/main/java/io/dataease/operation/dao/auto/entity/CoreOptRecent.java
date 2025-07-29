package io.dataease.operation.dao.auto.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@Comment("可视化资源表")
@Entity
@Table(name = "core_opt_recent")
public class CoreOptRecent {
    @Id
    @Comment("ID")
    @Column(name = "id", nullable = false)
    private Long id;

    @Comment("资源ID")
    @Column(name = "resource_id")
    private Long resourceId;

    @Size(max = 255)
    @Comment("资源名称")
    @Column(name = "resource_name")
    private String resourceName;

    @NotNull
    @Comment("用户ID")
    @Column(name = "uid", nullable = false)
    private Long uid;

    @NotNull
    @Comment("资源类型 1-可视化资源 2-仪表板 3-数据大屏 4-数据集 5-数据源 6-模板")
    @Column(name = "resource_type", nullable = false)
    private Integer resourceType;

    @Comment("1 新建 2 修改")
    @Column(name = "opt_type")
    private Integer optType;

    @NotNull
    @Comment("收藏时间")
    @Column(name = "time", nullable = false)
    private Long time;

}
