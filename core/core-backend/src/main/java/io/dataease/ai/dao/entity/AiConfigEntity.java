package io.dataease.ai.dao.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@MappedSuperclass
public abstract class AiConfigEntity {
    @Version
    @Column(name = "row_version", nullable = false)
    @Comment("并发修改版本")
    private Long rowVersion = 0L;
    @Column(name = "create_by")
    private Long createBy;
    @Column(name = "create_time", nullable = false)
    private Long createTime = System.currentTimeMillis();
    @Column(name = "update_by")
    private Long updateBy;
    @Column(name = "update_time", nullable = false)
    private Long updateTime = System.currentTimeMillis();
}
