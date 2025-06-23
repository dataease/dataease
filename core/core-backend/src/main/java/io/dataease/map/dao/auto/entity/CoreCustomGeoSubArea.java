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
@Entity
@Comment("自定义地理区域分区详情")
@Table(name = "core_custom_geo_sub_area")
public class CoreCustomGeoSubArea {
    @Id
    @Comment("id")
    @Column(name = "id", nullable = false)
    private Long id;

    @Comment("名称")
    @Size(max = 50)
    @NotNull
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Comment("区域范围")
    @Size(max = 1024)
    @Column(name = "scope", length = 1024)
    private String scope;

    @Comment("自定义地理区域id")
    @Size(max = 50)
    @NotNull
    @Column(name = "geo_area_id", nullable = false, length = 50)
    private String geoAreaId;

}
