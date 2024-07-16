package io.dataease.api.template.vo;

import lombok.Data;

import java.util.List;

/**
 * @author : WangJiaHao
 * @date : 2023/11/7 13:22
 */
@Data
public class VisualizationTemplateVO {

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

    /**
     * 模板类型 system 系统内置 self 用户自建
     */
    private String templateType;

    /**
     * template 样式
     */
    private String templateStyle;

    /**
     * template 数据
     */
    private String templateData;

    /**
     * 预存数据
     */
    private String dynamicData;

    /**
     * app数据
     */
    private String appData;

    /**
     * 使用次数
     */
    private Integer useCount;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 分类
     */
    private List<String> categories;
}
