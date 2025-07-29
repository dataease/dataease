package io.dataease.map.dao.auto.entity;

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
@Comment("地图区域表")
@Entity
@Table(name = "area")
public class Area {
    @Id
    @Size(max = 255)
    @Comment("区域id,和文件对应")
    @Column(name = "id", nullable = false)
    private String id;

    @Size(max = 255)
    @Comment("区域级别，从高到低country,province,city,district,更细的待定")
    @Column(name = "level")
    private String level;

    @Size(max = 255)
    @Comment("区域名称")
    @Column(name = "name")
    private String name;

    @Size(max = 255)
    @NotNull
    @Comment("父级区域id")
    @Column(name = "pid", nullable = false)
    private String pid;

}
