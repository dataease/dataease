package io.dataease.dto.panel;

import io.dataease.plugins.common.base.domain.PanelTemplateWithBLOBs;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Author: wangjiahao
 * Date: 2021-03-05
 * Description:
 */
@Data
public class PanelTemplateDTO extends PanelTemplateWithBLOBs {

    @ApiModelProperty("标签")
    private String label;
    @ApiModelProperty("子节点数量")
    private Integer childrenCount;
    @ApiModelProperty("子节点")
    private List<PanelTemplateDTO> children;


}
