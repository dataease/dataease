package io.dataease.template.dao.auto.entity;

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

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@Comment("dataease模板配置版本记录表")
@Entity
@Table(name = "de_template_version")
public class DeTemplateVersion {
    @Id
    @Comment("主键")
    @Column(name = "installed_rank", nullable = false)
    private Long installedRank;

    @Size(max = 50)
    @Comment("版本")
    @Column(name = "version", length = 50)
    private String version;

    @Size(max = 200)
    @Comment("描述")
    @Column(name = "description", length = 200)
    private String description;

    @Size(max = 20)
    @Comment("类型")
    @Column(name = "type", length = 20)
    private String type;

    @Size(max = 1000)
    @NotNull
    @Comment("脚本")
    @Column(name = "script", nullable = false, length = 1000)
    private String script;

    @Comment("CheckSum校验码")
    @Column(name = "checksum")
    private Integer checksum;

    @Size(max = 100)
    @Comment("安装人")
    @Column(name = "installed_by", length = 100)
    private String installedBy;

    @NotNull
    @Comment("安装时间")
    @Column(name = "installed_on", nullable = false)
    private LocalDateTime installedOn;

    @Comment("执行时间")
    @Column(name = "execution_time")
    private Integer executionTime;

    @NotNull
    @Comment("执行状态")
    @Column(name = "success", nullable = false)
    private Boolean success = false;

}
