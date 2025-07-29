package io.dataease.map.dao.ext.entity;

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
@Entity
@Comment("自定义地图区域信息表")
@Table(name = "core_area_custom")
public class CoreAreaCustom {
    @Id
    @Size(max = 255)
    @Comment("主键")
    @Column(name = "id", nullable = false)
    private String id;

    @Comment("自定义区域名称")
    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Comment("父级ID")
    @Size(max = 255)
    @NotNull
    @Column(name = "pid", nullable = false)
    private String pid;

}
