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
    @ApiModelProperty("新建来源")
    private String newFrom;
    @ApiModelProperty("模板动态数据")
    private String dynamicData;
    @ApiModelProperty("内部模板ID")
    private String templateId;


    public PanelGroupRequest() {
    }

    public PanelGroupRequest(String pid,String userId) {
        super.setPid(pid);
        this.userId= userId;
    }
}
