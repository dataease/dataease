package io.dataease.dto.panel;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@ApiModel("仪表板分享目标")
@Data
public class PanelShareOutDTO implements Serializable {

    @ApiModelProperty("仪表板ID")
    private String panelId;

    @ApiModelProperty("分享ID")
    private Long shareId;

    @ApiModelProperty("分享类型{0:用户,1:角色,2:组织}")
    private int type;

    @ApiModelProperty("目标ID")
    private String targetId;

    @ApiModelProperty("目标名称")
    private String targetName;

    @ApiModelProperty("分享时间")
    private Long createTime;

    @ApiModelProperty("仪表板状态")
    private String status;
}
