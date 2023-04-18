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
 * @since 2023-04-18
 */
@TableName("core_datasource")
public class CoreDatasource implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String desc;

    /**
     * 类型
     */
    private String type;

    /**
     * 详细信息
     */
    private String configuration;

    /**
     * 创健时间
     */
    private Long createTime;

    /**
     * 更新时间
     */
    private Long updateTime;

    /**
     * 创建人ID
     */
    private String createBy;

    /**
     * 状态
     */
    private String status;

    /**
     * 状态
     */
    private String qrtzInstance;

    /**
     * 任务状态
     */
    private String taskStatus;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getQrtzInstance() {
        return qrtzInstance;
    }

    public void setQrtzInstance(String qrtzInstance) {
        this.qrtzInstance = qrtzInstance;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    @Override
    public String toString() {
        return "CoreDatasource{" +
        "id = " + id +
        ", name = " + name +
        ", desc = " + desc +
        ", type = " + type +
        ", configuration = " + configuration +
        ", createTime = " + createTime +
        ", updateTime = " + updateTime +
        ", createBy = " + createBy +
        ", status = " + status +
        ", qrtzInstance = " + qrtzInstance +
        ", taskStatus = " + taskStatus +
        "}";
    }
}
