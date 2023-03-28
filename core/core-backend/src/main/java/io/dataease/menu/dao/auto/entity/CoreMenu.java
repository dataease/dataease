package io.dataease.menu.dao.auto.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author fit2cloud
 * @since 2023-03-28
 */
@TableName("core_menu")
public class CoreMenu implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 父ID
     */
    private Long pid;

    /**
     * 类型
     */
    private Integer type;

    /**
     * 名称
     */
    private String name;

    /**
     * 组件
     */
    private String component;

    /**
     * 排序
     */
    private Integer menuSort;

    /**
     * 图标
     */
    private String icon;

    /**
     * 路径
     */
    private String path;

    /**
     * 隐藏
     */
    private Boolean hidden;

    /**
     * 是否内部
     */
    private Boolean inLayout;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPid() {
        return pid;
    }

    public void setPid(Long pid) {
        this.pid = pid;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public Integer getMenuSort() {
        return menuSort;
    }

    public void setMenuSort(Integer menuSort) {
        this.menuSort = menuSort;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public Boolean getInLayout() {
        return inLayout;
    }

    public void setInLayout(Boolean inLayout) {
        this.inLayout = inLayout;
    }

    @Override
    public String toString() {
        return "CoreMenu{" +
        "id = " + id +
        ", pid = " + pid +
        ", type = " + type +
        ", name = " + name +
        ", component = " + component +
        ", menuSort = " + menuSort +
        ", icon = " + icon +
        ", path = " + path +
        ", hidden = " + hidden +
        ", inLayout = " + inLayout +
        "}";
    }
}
