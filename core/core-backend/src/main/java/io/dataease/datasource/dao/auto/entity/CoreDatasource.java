package io.dataease.datasource.dao.auto.entity;

import io.dataease.datasource.dao.interceptor.EncryptDecryptConverter;
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
@Table(name = "core_datasource")
@Comment("数据源")
public class CoreDatasource {
    @Id
    @Comment("主键")
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 255)
    @NotNull
    @Comment("名称")
    @Column(name = "name", nullable = false)
    private String name;

    @Size(max = 255)
    @Comment("描述")
    @Column(name = "description")
    private String description;

    @Size(max = 50)
    @NotNull
    @Comment("类型")
    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "pid")
    @Comment("父级ID")
    private Long pid;

    @Size(max = 50)
    @Comment("更新方式：0：替换；1：追加")
    @Column(name = "edit_type", length = 50)
    private String editType;

    @NotNull
    @Lob
    @Comment("详细信息")
    @Column(name = "configuration", length = 16777216, nullable = false)
    @Convert(converter = EncryptDecryptConverter.class)
    private String configuration;

    @NotNull
    @Comment("创建时间")
    @Column(name = "create_time", nullable = false)
    private Long createTime;

    @NotNull
    @Comment("更新时间")
    @Column(name = "update_time", nullable = false)
    private Long updateTime;

    @Column(name = "update_by")
    @Comment("更新人ID")
    private Long updateBy;

    @Size(max = 50)
    @Comment("主键")
    @Column(name = "创建人ID", length = 50)
    private String createBy;

    @Lob
    @Comment("数据源状态")
    @Column(name = "status", length = 16777216)
    private String status;

    @Lob
    @Comment("同步实例")
    @Column(name = "qrtz_instance")
    private String qrtzInstance;

    @Size(max = 50)
    @Comment("任务状态")
    @Column(name = "task_status", length = 50)
    private String taskStatus;

    @ColumnDefault("0")
    @Comment("启用数据填报功能")
    @Column(name = "enable_data_fill")
    private Byte enableDataFill;

}
