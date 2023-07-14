package io.dataease.api.visualization.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class VisualizationLinkageFieldVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * 联动ID
     */
    private String linkageId;

    /**
     * 源视图字段
     */
    private String sourceField;

    /**
     * 目标视图字段
     */
    private String targetField;

    /**
     * 更新时间
     */
    private Long updateTime;

    private String copyFrom;

    private String copyId;

}
