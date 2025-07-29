package io.dataease.copilot.dao.auto.entity;

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
@Comment("copilot配置信息表")
@Entity
@Table(name = "core_copilot_config")
public class CoreCopilotConfig {
    @Id
    @Comment("ID")
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 255)
    @Comment("copilot服务端地址")
    @Column(name = "copilot_url")
    private String copilotUrl;

    @Size(max = 255)
    @Comment("用户名")
    @Column(name = "username")
    private String username;

    @Size(max = 255)
    @Comment("密码")
    @Column(name = "pwd")
    private String pwd;

}
