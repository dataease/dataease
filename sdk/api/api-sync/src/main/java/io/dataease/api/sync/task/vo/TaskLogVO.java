package io.dataease.api.sync.task.vo;

import lombok.Data;

/**
 * 任务日志
 * @author fit2cloud
 * @date 2023/9/19 17:44
 **/
@Data
public class TaskLogVO {

    private String id;
    private String jobId;
    private String jobName;
    private String jobDesc;
    private Long executorStartTime;
    private Long executorEndTime;
    private String status;
    private String executorMsg;
    private String executorAddress;

    private String clearType;

}
