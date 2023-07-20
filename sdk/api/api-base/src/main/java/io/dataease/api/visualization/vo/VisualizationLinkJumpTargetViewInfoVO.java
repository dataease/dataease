package io.dataease.api.visualization.vo;

import lombok.Data;

/**
 * @author : WangJiaHao
 * @date : 2023/7/18 14:18
 */
@Data
public class VisualizationLinkJumpTargetViewInfoVO {

    private String targetId;

    private String linkJumpInfoId;
    /**
     * 勾选字段设置的匹配字段，也可以不是勾选字段本身
     */
    private String sourceFieldActiveId;

    private String targetViewId;

    private String targetFieldId;

    private String copyFrom;

    private String copyId;
}
