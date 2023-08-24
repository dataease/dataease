package io.dataease.dto.dataset;

import io.dataease.plugins.common.base.domain.DatasetTableTask;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author gin
 * @Date 2021/3/9 3:19 下午
 */
@Getter
@Setter
public class DataSetTaskDTO extends DatasetTableTask {
    @ApiModelProperty("数据集名称")
    private String datasetName;
    @ApiModelProperty("下次执行时间")
    private Long nextExecTime;
    @ApiModelProperty("任务状态")
    private String taskStatus;
    @ApiModelProperty("消息")
    private String msg;
    @ApiModelProperty("权限")
    private String privileges;
}
