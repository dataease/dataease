package io.dataease.dto.panel;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PanelShareDto {

    @ApiModelProperty("节点ID")
    private String id;
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("节点父ID")
    private String creator;
    @ApiModelProperty("子节点")
    private List<PanelShareDto> children;


}
