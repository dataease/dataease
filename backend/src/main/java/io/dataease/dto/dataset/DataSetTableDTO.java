package io.dataease.dto.dataset;

import io.dataease.plugins.common.base.domain.DatasetTable;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Author gin
 * @Date 2021/2/23 2:55 下午
 */
@Setter
@Getter
public class DataSetTableDTO extends DatasetTable {
    @ApiModelProperty("子节点")
    private List<DataSetTableDTO> children;
    @ApiModelProperty("权限")
    private String privileges;
    @ApiModelProperty("是否叶子结点")
    private Boolean isLeaf;
    @ApiModelProperty("父ID")
    private String pid;
    @ApiModelProperty("创建者姓名")
    private String creatorName;
}
