package io.dataease.api.xpack.component.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class XpackMenuVO implements Serializable {

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

    /**
     * 参与授权
     */
    private Boolean auth;
}
