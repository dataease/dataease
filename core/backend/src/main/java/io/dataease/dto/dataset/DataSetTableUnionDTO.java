package io.dataease.dto.dataset;

import io.dataease.plugins.common.base.domain.DatasetTableUnion;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author gin
 * @Date 2021/5/6 6:08 下午
 */
@Getter
@Setter
public class DataSetTableUnionDTO extends DatasetTableUnion {
    @ApiModelProperty("源表名称")
    private String sourceTableName;
    @ApiModelProperty("源表字段名称")
    private String sourceTableFieldName;
    @ApiModelProperty("目标表名称")
    private String targetTableName;
    @ApiModelProperty("目标表字段名称")
    private String targetTableFieldName;
}
