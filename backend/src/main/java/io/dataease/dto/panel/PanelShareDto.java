package io.dataease.dto.panel;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class PanelShareDto extends PanelSharePo{

    @ApiModelProperty("子节点")
    private List<PanelShareDto> children;


}
