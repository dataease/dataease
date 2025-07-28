package io.dataease.rsa.dao.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.io.Serializable;

@Getter
@Setter
@Entity
@Comment("RSA 密钥表")
@Table(name = "core_rsa")
public class CoreRsa implements Serializable {

    private static final long serialVersionUID = 1L;

    @Comment("主键")
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Comment("私钥")
    @NotNull
    @Column(name = "private_key", nullable = false, length = 16777216)
    private String privateKey;

    @Comment("公钥")
    @NotNull
    @Column(name = "public_key", nullable = false, length = 16777216)
    private String publicKey;

    @Comment("生成时间")
    @NotNull
    @Column(name = "create_time", nullable = false)
    private Long createTime;

    @Comment("AES 加密算法的 key")
    @NotNull
    @Column(name = "aes_key", nullable = false, length = 16777216)
    private String aesKey;

}
