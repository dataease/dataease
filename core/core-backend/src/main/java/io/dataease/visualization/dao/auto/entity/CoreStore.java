package io.dataease.visualization.dao.auto.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@Comment("用户收藏表")
@Entity
@Table(name = "core_store")
public class CoreStore {
    @Id
    @Comment("ID")
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Comment("资源ID")
    @Column(name = "resource_id", nullable = false)
    private Long resourceId;

    @NotNull
    @Comment("用户ID")
    @Column(name = "uid", nullable = false)
    private Long uid;

    @NotNull
    @Comment("资源类型")
    @Column(name = "resource_type", nullable = false)
    private Integer resourceType;

    @NotNull
    @Comment("收藏时间")
    @Column(name = "time", nullable = false)
    private Long time;

}
