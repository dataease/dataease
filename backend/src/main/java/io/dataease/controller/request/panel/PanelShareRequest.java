package io.dataease.controller.request.panel;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PanelShareRequest implements Serializable {

    @ApiModelProperty("分享目标用户集合")
    private List<Long> userIds;
    @ApiModelProperty("分享仪表板集合")
    private List<String> panelIds;
}
