package io.dataease.dto.dataset;

import io.dataease.plugins.common.base.domain.DatasetTableTaskLog;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author gin
 * @Date 2021/3/9 3:19 下午
 */
@Getter
@Setter
public class DataSetTaskLogDTO extends DatasetTableTaskLog {
    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("数据集名称")
    private String datasetName;
}
