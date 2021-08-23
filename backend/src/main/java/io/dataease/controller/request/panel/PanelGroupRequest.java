package io.dataease.controller.request.panel;

import io.dataease.dto.panel.PanelGroupDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Author: wangjiahao
 * Date: 2021-03-05
 * Description:
 */
@Data
public class PanelGroupRequest extends PanelGroupDTO {
    @ApiModelProperty("排序")
    private String sort;
    @ApiModelProperty("用户ID")
    private String userId;
    @ApiModelProperty("操作类型")
    private String optType;

    public PanelGroupRequest() {
    }

    public PanelGroupRequest(String pid,String userId) {
        super.setPid(pid);
        this.userId= userId;
    }
}
