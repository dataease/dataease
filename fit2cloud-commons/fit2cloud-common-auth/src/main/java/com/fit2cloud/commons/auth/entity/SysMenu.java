package com.fit2cloud.commons.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 系统菜单
 * </p>
 *
 * @author cyw
 * @since 2021-02-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_menu")
public class SysMenu implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "menu_id", type = IdType.AUTO)
    private Long menuId;

    /**
     * 上级菜单ID
     */
    @TableField("pid")
    private Long pid;

    /**
     * 子菜单数目
     */
    @TableField("sub_count")
    private Integer subCount;

    /**
     * 菜单类型
     */
    @TableField("type")
    private Integer type;

    /**
     * 菜单标题
     */
    @TableField("title")
    private String title;

    /**
     * 组件名称
     */
    @TableField("name")
    private String name;

    /**
     * 组件
     */
    @TableField("component")
    private String component;

    /**
     * 排序
     */
    @TableField("menu_sort")
    private Integer menuSort;

    /**
     * 图标
     */
    @TableField("icon")
    private String icon;

    /**
     * 链接地址
     */
    @TableField("path")
    private String path;

    /**
     * 是否外链
     */
    @TableField("i_frame")
    private Boolean iFrame;

    /**
     * 缓存
     */
    @TableField("cache")
    private Boolean cache;

    /**
     * 隐藏
     */
    @TableField("hidden")
    private Boolean hidden;

    /**
     * 权限
     */
    @TableField("permission")
    private String permission;

    /**
     * 创建者
     */
    @TableField("create_by")
    private String createBy;

    /**
     * 更新者
     */
    @TableField("update_by")
    private String updateBy;

    /**
     * 创建日期
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;


}
