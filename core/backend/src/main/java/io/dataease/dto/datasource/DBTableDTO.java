package io.dataease.dto.datasource;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author gin
 * @Date 2021/4/30 10:57 上午
 */
@Getter
@Setter
public class DBTableDTO {
    @ApiModelProperty("数据源ID")
    private String datasourceId;
    @ApiModelProperty("数据源名称")
    private String name;
    @ApiModelProperty("表注释")
    private String remark;
    @ApiModelProperty("启用检测")
    private boolean enableCheck;
    @ApiModelProperty("数据集路径")
    private String datasetPath;
}
