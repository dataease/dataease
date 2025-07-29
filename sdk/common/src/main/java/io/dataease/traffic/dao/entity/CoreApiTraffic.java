package io.dataease.traffic.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@Entity
@Table(name = "core_api_traffic")
public class CoreApiTraffic {
    @Id
    @Comment("ID")
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 255)
    @NotNull
    @Comment("api")
    @Column(name = "api", nullable = false)
    private String api;

    @NotNull
    @Comment("阈值")
    @ColumnDefault("2")
    @Column(name = "threshold", nullable = false)
    private Integer threshold;

    @NotNull
    @Comment("活动并发")
    @ColumnDefault("0")
    @Column(name = "alive", nullable = false)
    private Integer alive;

}
