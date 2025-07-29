package io.dataease.startup.dao.auto.entity;

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
@Comment("项目启动任务")
@Entity
@Table(name = "core_sys_startup_job")
public class CoreSysStartupJob {
    @Id
    @Size(max = 64)
    @Comment("ID")
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    @Size(max = 255)
    @Comment("任务名称")
    @Column(name = "name")
    private String name;

    @Size(max = 255)
    @Comment("任务状态")
    @Column(name = "status")
    private String status;

}
