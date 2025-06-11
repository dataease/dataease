package io.dataease.font.dao.auto.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

@Data
@Getter
@Setter
@Entity
@Table(name = "core_font")
public class CoreFont {
    @Id
    @Comment("ID")
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 255)
    @NotNull
    @Comment("字体名称")
    @Column(name = "name", nullable = false)
    private String name;

    @Size(max = 255)
    @Comment("文件名称")
    @Column(name = "file_name")
    private String fileName;

    @Size(max = 255)
    @Comment("文件转换名称")
    @Column(name = "file_trans_name")
    private String fileTransName;

    @Comment("是否默认")
    @ColumnDefault("0")
    @Column(name = "is_default")
    private Boolean isDefault;

    @NotNull
    @Comment("更新时间")
    @Column(name = "update_time", nullable = false)
    private Long updateTime;

    @Comment("是否内置")
    @ColumnDefault("0")
    @Column(name = "is_BuiltIn")
    private Boolean isBuiltin;

    @Column(name = "size")
    private Double size;

    @Size(max = 255)
    @Column(name = "size_type")
    private String sizeType;

}
