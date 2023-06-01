package io.dataease.datasource.dao.auto.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author fit2cloud
 * @since 2023-05-31
 */
@TableName("core_datasource_task")
public class CoreDatasourceTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 数据源ID
     */
    private Long dsId;

    /**
     * 任务名称
     */
    private String name;

    /**
     * 更新方式
     */
    private String updateType;

    /**
     * 开始时间
     */
    private Long startTime;

    /**
     * 执行频率：0 一次性 1 cron
     */
    private String syncRate;

    /**
     * cron表达式
     */
    private String cron;

    /**
     * 简单重复间隔
     */
    private Long simpleCronValue;

    /**
     * 简单重复类型：分、时、天
     */
    private String simpleCronType;

    /**
     * 结束限制 0 无限制 1 设定结束时间
     */
    private String endLimit;

    /**
     * 结束时间
     */
    private Long endTime;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 上次执行时间
     */
    private Long lastExecTime;

    /**
     * 上次执行结果
     */
    private String lastExecStatus;

    private String extraData;

    /**
     * 任务状态
     */
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDsId() {
        return dsId;
    }

    public void setDsId(Long dsId) {
        this.dsId = dsId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUpdateType() {
        return updateType;
    }

    public void setUpdateType(String updateType) {
        this.updateType = updateType;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public String getSyncRate() {
        return syncRate;
    }

    public void setSyncRate(String syncRate) {
        this.syncRate = syncRate;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public Long getSimpleCronValue() {
        return simpleCronValue;
    }

    public void setSimpleCronValue(Long simpleCronValue) {
        this.simpleCronValue = simpleCronValue;
    }

    public String getSimpleCronType() {
        return simpleCronType;
    }

    public void setSimpleCronType(String simpleCronType) {
        this.simpleCronType = simpleCronType;
    }

    public String getEndLimit() {
        return endLimit;
    }

    public void setEndLimit(String endLimit) {
        this.endLimit = endLimit;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getLastExecTime() {
        return lastExecTime;
    }

    public void setLastExecTime(Long lastExecTime) {
        this.lastExecTime = lastExecTime;
    }

    public String getLastExecStatus() {
        return lastExecStatus;
    }

    public void setLastExecStatus(String lastExecStatus) {
        this.lastExecStatus = lastExecStatus;
    }

    public String getExtraData() {
        return extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "CoreDatasourceTask{" +
        "id = " + id +
        ", dsId = " + dsId +
        ", name = " + name +
        ", updateType = " + updateType +
        ", startTime = " + startTime +
        ", syncRate = " + syncRate +
        ", cron = " + cron +
        ", simpleCronValue = " + simpleCronValue +
        ", simpleCronType = " + simpleCronType +
        ", endLimit = " + endLimit +
        ", endTime = " + endTime +
        ", createTime = " + createTime +
        ", lastExecTime = " + lastExecTime +
        ", lastExecStatus = " + lastExecStatus +
        ", extraData = " + extraData +
        ", status = " + status +
        "}";
    }
}
