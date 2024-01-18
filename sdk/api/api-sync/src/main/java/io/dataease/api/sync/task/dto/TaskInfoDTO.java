package io.dataease.api.sync.task.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author fit2cloud
 * @date 2023/11/28 17:17
 **/
@Data
public class TaskInfoDTO {
    private String id;

    private String name;

    /**
     * 任务类型KEY
     */
    private String jobKey;

    private String desc;

    private LocalDateTime createTime;

    private LocalDateTime modifyTime;

    /**
     * 创建人
     */
    private Long createBy;

    /**
     * 修改人
     */
    private Long modifyBy;

    /**
     * 任务参数
     */
    private String parameter;

    /**
     * 扩展参数
     */
    private String extParameter;

    /**
     * 当前任务状态
     * unexecuted未执行 currentTime<startTime
     * waiting等待执行 stopTime>=currentTime>=startTime,status=1
     * suspend暂停 stopTime>=currentTime>=startTime,status=0
     * done执行结束 currentTime>stopTime
     * running执行中,通过当前任务的日志状态判断，如果有日志在执行中
     */
    private String status;

    /**
     * 删除标识
     */
    private Boolean deleted;

    /**
     * 任务执行超时时间
     */
    private Long executorTimeout;

    /**
     * 任务执行失败重试次数
     */
    private Long executorFailRetryCount;

    /**
     * 上次调度时间
     */
    private Long triggerLastTime;

    /**
     * 下次次调度时间
     */
    private Long triggerNextTime;

    /**
     * 调度类型,NONE,CRON,FIX_RATE,FIX_DELAY
     */
    private String schedulerType;

    /**
     * 调度配置，取决于调度类型
     */
    private String schedulerConf;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String stopTime;


    /**
     * 源数据源信息
     */
    private Source source;
    /**
     * 目标数据源信息
     */
    private Target target;
}
