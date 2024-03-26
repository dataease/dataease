package io.dataease.plugins.common.dto;

import lombok.Data;

import java.util.Objects;

@Data
public class PluginSysMenu {

    private Long menuId;

    private Long pid;

    private Integer subCount;

    private Integer type;

    private String title;

    private String name;

    private String component;

    private Integer menuSort;

    private String icon;

    private String path;

    private Boolean iFrame;

    private Boolean cache;

    private Boolean hidden;

    private String permission;

    private String createBy;

    private String updateBy;

    private Long createTime;

    private Long updateTime;

    private boolean noLayout;

    private boolean useBasicResource;

    private static final long serialVersionUID = 1L;

    /**
     * 由于该类型作为HashSet key所以必须重写以下方法
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PluginSysMenu menu = (PluginSysMenu) o;
        return Objects.equals(menuId, menu.menuId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(menuId);
    }
}
