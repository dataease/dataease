package io.dataease.system.dao.auto.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Length;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Comment("系统设置表")
@Entity
@Table(name = "core_sys_setting")
public class CoreSysSetting {
    @Id
    @Comment("ID")
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 255)
    @NotNull
    @Comment("键")
    @Column(name = "pkey", nullable = false)
    private String pkey;

    @NotNull
    @Comment("值")
    @JdbcTypeCode(SqlTypes.LONG32VARCHAR)
    @Column(name = "pval", nullable = false)
    private String pval;

    @Size(max = 255)
    @NotNull
    @Comment("类型")
    @Column(name = "type", nullable = false)
    private String type;

    @NotNull
    @Comment("顺序")
    @ColumnDefault("0")
    @Column(name = "sort", nullable = false)
    private Integer sort;

    public CoreSysSetting(Long id, String pkey, String pval, String type, Integer sort) {
        this.id = id;
        this.pkey = pkey;
        this.pval = pval;
        this.type = type;
        this.sort = sort;
    }

    public CoreSysSetting() {
    }
}
