package io.dataease.copilot.dao.auto.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@Entity
@Table(name = "core_copilot_token")
public class CoreCopilotToken {
    @Id
    @Comment("ID")
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 255)
    @Comment("free or license")
    @Column(name = "type")
    private String type;

    @Column(name = "token", length = 16777216)
    private String token;

    @Column(name = "update_time")
    private Long updateTime;

}
