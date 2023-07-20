package io.dataease.api.visualization.vo;

import lombok.Data;

/**
 * @author : WangJiaHao
 * @date : 2023/7/18 12:40
 */
@Data
public class VisualizationLinkJumpVO {
    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * 源仪表板ID
     */
    private String sourceDvId;

    /**
     * 源视图ID
     */
    private String sourceViewId;

    /**
     * 跳转信息
     */
    private String linkJumpInfo;

    /**
     * 是否启用
     */
    private Boolean checked;

    private String copyFrom;

    private String copyId;
}
