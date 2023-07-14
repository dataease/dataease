package io.dataease.api.visualization.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class VisualizationLinkageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String dvId;

    /**
     * 源视图id
     */
    private String sourceViewId;

    /**
     * 联动视图id
     */
    private String targetViewId;

    /**
     * 更新时间
     */
    private Long updateTime;

    /**
     * 更新人
     */
    private String updatePeople;

    /**
     * 是否启用关联
     */
    private Boolean linkageActive;

    private String ext1;

    private String ext2;

    private String copyFrom;

    private String copyId;
}
