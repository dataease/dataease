package io.dataease.share.dao.auto.entity;

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
@Comment("公共链接")
@Entity
@Table(name = "xpack_share")
public class XpackShare {
    @Id
    @Comment("ID")
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Comment("创建人")
    @Column(name = "creator", nullable = false)
    private Long creator;

    @NotNull
    @Comment("创建时间")
    @Column(name = "time", nullable = false)
    private Long time;

    @Comment("过期时间")
    @Column(name = "exp")
    private Long exp;

    @Size(max = 16)
    @NotNull
    @Comment("uuid")
    @Column(name = "uuid", nullable = false, length = 16)
    private String uuid;

    @Size(max = 255)
    @Comment("密码")
    @Column(name = "pwd")
    private String pwd;

    @NotNull
    @Comment("资源ID")
    @Column(name = "resource_id", nullable = false)
    private Long resourceId;

    @NotNull
    @Comment("组织ID")
    @Column(name = "oid", nullable = false)
    private Long oid;

    @NotNull
    @Column(name = "type", nullable = false)
    private Integer type;

    @NotNull
    @Comment("自动生成密码")
    @ColumnDefault("true")
    @Column(name = "auto_pwd", nullable = false)
    private Boolean autoPwd = false;

    @NotNull
    @Comment("ticket")
    @ColumnDefault("false")
    @Column(name = "ticket_require", nullable = false)
    private Boolean ticketRequire = false;

}
