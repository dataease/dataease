package io.dataease.datasource.dto;

import io.dataease.datasource.dao.auto.entity.CoreDatasourceTask;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author gin
 * @Date 2021/3/9 3:19 下午
 */
@Getter
@Setter
public class CoreDatasourceTaskDTO extends CoreDatasourceTask {
    @ApiModelProperty("数据源名称")
    private String datasourceName;
    @ApiModelProperty("下次执行时间")
    private Long nextExecTime;
    @ApiModelProperty("任务状态")
    private String taskStatus;
    @ApiModelProperty("消息")
    private String msg;
    @ApiModelProperty("权限")
    private String privileges;
}
