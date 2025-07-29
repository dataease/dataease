package io.dataease.menu.dao.auto.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@Entity
@Table(name = "core_menu")
@Comment("路由菜单")
public class CoreMenu {
    @Id
    @Comment("主键")
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Comment("父ID")
    @Column(name = "pid", nullable = false)
    private Long pid;

    @Comment("类型")
    @Column(name = "type")
    private Integer type;

    @Comment("名称")
    @Size(max = 45)
    @Column(name = "name", length = 45)
    private String name;

    @Comment("组件")
    @Size(max = 45)
    @Column(name = "component", length = 45)
    private String component;

    @Comment("排序")
    @Column(name = "menu_sort")
    private Integer menuSort;

    @Comment("图标")
    @Size(max = 45)
    @Column(name = "icon", length = 45)
    private String icon;

    @Comment("路径")
    @Size(max = 45)
    @Column(name = "path", length = 45)
    private String path;

    @Comment("隐藏")
    @NotNull
    @ColumnDefault("false")
    @Column(name = "hidden", nullable = false)
    private Boolean hidden = false;

    @Comment("是否内部")
    @NotNull
    @ColumnDefault("true")
    @Column(name = "in_layout", nullable = false)
    private Boolean inLayout = false;

    @Comment("参与授权")
    @NotNull
    @ColumnDefault("false")
    @Column(name = "auth", nullable = false)
    private Boolean auth = false;

}
