package io.dataease.api.ds.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author fit2cloud
 * @since 2023-06-08
 */
@Data
public class CoreDatasourceTaskLogDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    /**
     * 数据源ID
     */
    @JsonSerialize(using= ToStringSerializer.class)
    private Long dsId;

    /**
     * 任务ID
     */
    @JsonSerialize(using= ToStringSerializer.class)
    private Long taskId;

    /**
     * 开始时间
     */
    private Long startTime;

    /**
     * 结束时间
     */
    private Long endTime;

    /**
     * 执行状态
     */
    private String status;

    /**
     * 错误信息
     */
    private String info;

    /**
     * 创建时间
     */
    private Long createTime;

    private String triggerType;

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

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    @Override
    public String toString() {
        return "CoreDatasourceTaskLog{" +
        "id = " + id +
        ", dsId = " + dsId +
        ", taskId = " + taskId +
        ", startTime = " + startTime +
        ", endTime = " + endTime +
        ", status = " + status +
        ", info = " + info +
        ", createTime = " + createTime +
        ", triggerType = " + triggerType +
        "}";
    }
}
