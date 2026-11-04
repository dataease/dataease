package io.dataease.extensions.sync.model.task;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author fit2cloud
 **/
@Getter
@Setter
public class TaskInfoVO {

    private String id;
    private String name;
    private String jobKey;
    private String desc;
    private LocalDateTime createTime;
    private LocalDateTime modifyTime;
    private Long createBy;
    private Long modifyBy;
    private String parameter;
    private String extParameter;
    private String status;
    private Boolean deleted;
    private Long executorTimeout;
    private Long executorFailRetryCount;
    private Long triggerLastTime;
    private Long triggerNextTime;
    private String schedulerType;
    private String schedulerConf;
    private Source source;
    private Target target;
    private boolean editing;
    private boolean editable;
    private boolean editableCheckFailed;
    /**
     * 连接可用性和目标表检查分别展示，避免把连接改名或目标表缺失误认为绑定了其他数据库
     */
    private boolean sourceDatasourceMissing;
    private boolean targetDatasourceMissing;
    private String targetTableCheckStatus;
    private String targetTableCheckError;

    private String userName;
    private String lastExecuteStatus;
    private boolean incrementTask;
    private boolean withinCycle;
    private Long oid;
    private Long startTime;
    private Long stopTime;
    private String startTimeString;
    private String stopTimeString;

    // 以下为日志信息
    private String logId;
    private Long executorStartTime;
    private Long executorEndTime;
    private String executorMsg;
    private String logStatus;
    /**
     * 当前维护摘要独立于原任务启停和运行状态，供列表展示处理入口，不携带恢复快照
     */
    private String maintenanceState;
    private boolean maintenanceNeedsAttention;

}
