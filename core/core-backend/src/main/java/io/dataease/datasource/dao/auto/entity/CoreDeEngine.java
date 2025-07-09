package io.dataease.datasource.dao.auto.entity;

import io.dataease.dao.auto.interceptor.EncryptDecryptConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@Comment("数据引擎")
@Entity
@Table(name = "core_de_engine")
public class CoreDeEngine {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Comment("主键")
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 50)
    @Comment("名称")
    @Column(name = "name", length = 50)
    private String name;

    @Size(max = 50)
    @Comment("描述")
    @Column(name = "description", length = 50)
    private String description;

    @Size(max = 50)
    @NotNull
    @Comment("类型")
    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @NotNull
    @Lob
    @Column(name = "configuration", nullable = false, length = 16777216)
    @Convert(converter = EncryptDecryptConverter.class)
    private String configuration;

    @Comment("Create timestamp")
    @Column(name = "create_time")
    private Long createTime;

    @Comment("Update timestamp")
    @Column(name = "update_time")
    private Long updateTime;

    @Size(max = 50)
    @Comment("创建人ID")
    @Column(name = "create_by", length = 50)
    private String createBy;

    @Size(max = 45)
    @Comment("状态")
    @Column(name = "status", length = 45)
    private String status;

}
