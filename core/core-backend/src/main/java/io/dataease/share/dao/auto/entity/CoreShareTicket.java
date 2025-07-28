package io.dataease.share.dao.auto.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@Comment("分享Ticket表")
@Entity
@Table(name = "core_share_ticket")
public class CoreShareTicket {
    @Id
    @Comment("ID")
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 255)
    @NotNull
    @Comment("分享uuid")
    @Column(name = "uuid", nullable = false)
    private String uuid;

    @Size(max = 255)
    @NotNull
    @Comment("ticket")
    @Column(name = "ticket", nullable = false)
    private String ticket;

    @Comment("ticket有效期")
    @Column(name = "exp")
    private Long exp;

    @Column(name = "args", length = 16777216)
    private String args;

    @Comment("首次访问时间")
    @Column(name = "access_time")
    private Long accessTime;

}
