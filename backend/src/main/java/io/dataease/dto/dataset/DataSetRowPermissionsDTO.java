package io.dataease.dto.dataset;

import io.dataease.base.domain.DatasetRowPermissions;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author gin
 * @Date 2021/3/9 3:19 下午
 */
@Getter
@Setter
public class DataSetRowPermissionsDTO extends DatasetRowPermissions {
    @ApiModelProperty("数据集名称")
    private String datasetName;
    @ApiModelProperty("字段名称")
    private String fieldName;
    @ApiModelProperty("授权对象名称")
    private String authTargetName;
}
