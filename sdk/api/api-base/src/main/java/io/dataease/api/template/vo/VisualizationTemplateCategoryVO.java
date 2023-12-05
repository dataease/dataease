package io.dataease.api.template.vo;

import lombok.Data;

/**
 * @author : WangJiaHao
 * @date : 2023/11/7 13:22
 */
@Data
public class VisualizationTemplateCategoryVO {

    /**
     * 主键
     */
    private String id;

    /**
     * 名称
     */
    private String name;

    /**
     * 父级id
     */
    private String pid;

    /**
     * 层级
     */
    private Integer level;

    /**
     * 模板种类  dataV or dashboard 目录或者文件夹
     */
    private String dvType;

    /**
     * 节点类型  folder or panel 目录或者文件夹
     */
    private String nodeType;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 缩略图
     */
    private String snapshot;
}
