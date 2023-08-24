package io.dataease.controller.request.panel;

import io.dataease.commons.model.AuthURD;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;


@Data
public class PanelShareFineDto implements Serializable {


    private static final long serialVersionUID = -792964171742204428L;
    @ApiModelProperty("资源ID")
    private  String resourceId;
    @ApiModelProperty("分享信息")
    private AuthURD authURD;
}
