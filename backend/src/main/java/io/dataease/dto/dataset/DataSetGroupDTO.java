package io.dataease.dto.dataset;

import io.dataease.plugins.common.base.domain.DatasetGroup;
import io.dataease.plugins.common.model.ITreeBase;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author gin
 * @Date 2021/2/20 8:17 下午
 */
@Data
public class DataSetGroupDTO extends DatasetGroup implements ITreeBase<DataSetGroupDTO> {
    @ApiModelProperty("标签")
    private String label;
    @ApiModelProperty("子节点")
    private List<DataSetGroupDTO> children;
    @ApiModelProperty("权限")
    private String privileges;
    @ApiModelProperty("节点类型")
    public String getNodeType(){
        return super.getType();
    };
}
