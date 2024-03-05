package io.dataease.api.sync.task.vo;

import io.dataease.api.sync.task.dto.Source;
import io.dataease.api.sync.task.dto.Target;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author fit2cloud
 * @date 2023/11/28 17:15
 **/
@Data
public class TaskInfoVO {

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
    private Long createBy;;
    /**
     * 创建人
     */
    private String userName;

    /**
     * 任务参数
     */
    private String parameter;

    /**
     * 扩展参数
     */
    private String extParameter;

    /**
     * 任务状态
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
    private Long startTime;

    /**
     * 结束时间
     */
    private Long stopTime;

    private Source source;
    private Target target;

    /**
     * 上次执行结果，获取任务最新的日志状态
     * running执行中
     * success
     * fail失败
     */
    private String lastExecuteStatus;
    /**
     * 增量任务
     */
    private boolean incrementTask;

    // 以下为日志信息
    private String logId;
    private Long executorStartTime;
    private Long executorEndTime;
    private String executorMsg;
    /**
     * 成功SUCCESS,失败FAIL,执行中RUNNING
     */
    private String logStatus;

    /**
     * 在执行周期内
     */
    private boolean withinCycle;

}
