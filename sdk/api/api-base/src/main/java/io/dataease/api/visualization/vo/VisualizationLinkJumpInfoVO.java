package io.dataease.api.visualization.vo;

import lombok.Data;

/**
 * @author : WangJiaHao
 * @date : 2023/7/18 13:21
 */
@Data
public class VisualizationLinkJumpInfoVO {
    private String id;

    /**
     * link jump ID
     */
    private String linkJumpId;

    /**
     * 关联类型 inner 内部仪表板，outer 外部链接
     */
    private String linkType;

    /**
     * 跳转类型 _blank 新开页面 _self 当前窗口
     */
    private String jumpType;

    /**
     * 关联仪表板ID
     */
    private String targetDvId;

    /**
     * 字段ID
     */
    private String sourceFieldId;

    /**
     * 内容 linkType = outer时使用
     */
    private String content;

    /**
     * 是否可用
     */
    private Boolean checked;

    /**
     * 是否附加点击参数
     */
    private Boolean attachParams;

    private String copyFrom;

    private String copyId;
}
