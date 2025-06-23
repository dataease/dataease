package io.dataease.map.dao.auto.entity;

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
@Entity
@Comment("自定义地理区域表")
@Table(name = "core_custom_geo_area")
public class CoreCustomGeoArea {
    @Id
    @Comment("id")
    @Size(max = 50)
    @Column(name = "id", nullable = false, length = 50)
    private String id;

    @Comment("区域名称")
    @Size(max = 50)
    @Column(name = "name", length = 50)
    private String name;

}
