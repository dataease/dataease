package io.dataease.controller.request.panel;

import io.dataease.plugins.common.base.domain.PanelTemplateWithBLOBs;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Author: wangjiahao
 * Date: 2021-03-05
 * Description:
 */
@Data
public class PanelTemplateRequest extends PanelTemplateWithBLOBs {
    @ApiModelProperty("排序")
    private String sort;
    @ApiModelProperty("详息")
    private String withBlobs="Y";
    @ApiModelProperty("操作类型")
    private String optType;
    @ApiModelProperty("静态文件")
    private String staticResource;
    @ApiModelProperty("是否及联")
    private Boolean withChildren = false;

    public PanelTemplateRequest() {
    }

    public PanelTemplateRequest(String pid) {
        super.setPid(pid);
        withBlobs="N";
    }
}
