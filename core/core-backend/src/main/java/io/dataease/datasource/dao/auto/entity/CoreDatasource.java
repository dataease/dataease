package io.dataease.datasource.dao.auto.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Table(name = "core_datasource")
public class CoreDatasource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Size(max = 255)
    @Column(name = "description")
    private String description;

    @Size(max = 50)
    @NotNull
    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "pid")
    private Long pid;

    @Size(max = 50)
    @Column(name = "edit_type", length = 50)
    private String editType;

    @NotNull
    @Lob
    @Column(name = "configuration", length = 16777216, nullable = false)
    private String configuration;

    @NotNull
    @Column(name = "create_time", nullable = false)
    private Long createTime;

    @NotNull
    @Column(name = "update_time", nullable = false)
    private Long updateTime;

    @Column(name = "update_by")
    private Long updateBy;

    @Size(max = 50)
    @Column(name = "create_by", length = 50)
    private String createBy;

    @Lob
    @Column(name = "status", length = 16777216)
    private String status;

    @Lob
    @Column(name = "qrtz_instance")
    private String qrtzInstance;

    @Size(max = 50)
    @Column(name = "task_status", length = 50)
    private String taskStatus;

    @ColumnDefault("0")
    @Column(name = "enable_data_fill")
    private Byte enableDataFill;

}
