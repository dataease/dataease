package io.dataease.base.domain;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class PanelViewLinkage implements Serializable {
    @ApiModelProperty("ID")
    private String id;
    @ApiModelProperty("仪表板ID")
    private String panelId;
    @ApiModelProperty("源视图ID")
    private String sourceViewId;
    @ApiModelProperty("目标视图ID")
    private String targetViewId;
    @ApiModelProperty("更新时间")
    private Long updateTime;
    @ApiModelProperty("更新者")
    private String updatePeople;
    @ApiModelProperty("额外1")
    private String ext1;
    @ApiModelProperty("额外2")
    private String ext2;

    private static final long serialVersionUID = 1L;
}