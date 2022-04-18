package io.datains.dto.chart;

import io.datains.base.domain.DatasetGroup;
import io.datains.commons.model.ITreeBase;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;


@Data
public class ChartGroupDTO extends DatasetGroup implements ITreeBase<ChartGroupDTO> {
    @ApiModelProperty("标签")
    private String label;
    @ApiModelProperty("子节点")
    private List<ChartGroupDTO> children;
    @ApiModelProperty("权限")
    private String privileges;
    @ApiModelProperty("节点类型")
    public String getNodeType(){
        return super.getType();
    };
}
