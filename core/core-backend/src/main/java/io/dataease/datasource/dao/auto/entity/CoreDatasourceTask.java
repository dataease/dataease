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
 * @since 2023-04-17
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
     * 表ID
     */
    private String tableId;

    /**
     * 任务名称
     */
    private String name;

    /**
     * 更新方式：0-全量更新 1-增量更新
     */
    private String type;

    /**
     * 开始时间
     */
    private Long startTime;

    /**
     * 执行频率：0 一次性 1 cron
     */
    private String rate;

    /**
     * cron表达式
     */
    private String cron;

    /**
     * 结束限制 0 无限制 1 设定结束时间
     */
    private String end;

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

    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
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
        ", tableId = " + tableId +
        ", name = " + name +
        ", type = " + type +
        ", startTime = " + startTime +
        ", rate = " + rate +
        ", cron = " + cron +
        ", end = " + end +
        ", endTime = " + endTime +
        ", createTime = " + createTime +
        ", lastExecTime = " + lastExecTime +
        ", lastExecStatus = " + lastExecStatus +
        ", extraData = " + extraData +
        ", status = " + status +
        "}";
    }
}
