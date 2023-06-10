package io.dataease.api.visualization.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author : wangjiahao
 * @date : 2023/6/9 14:57
 */
@Data
public class VisualizationSubjectVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * 主题名称
     */
    private String name;

    /**
     * 主题类型 system 系统主题，self 自定义主题
     */
    private String type;

    private Integer createNum;

    /**
     * 主题内容
     */
    private String details;

    /**
     * 删除标记
     */
    private Boolean deleteFlag;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新时间
     */
    private Long updateTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 删除时间
     */
    private Long deleteTime;

    /**
     * 删除人
     */
    private Long deleteBy;
}
