package io.dataease.api.visualization.vo;

import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author fit2cloud
 * @since 2024-06-25
 */
public class VisualizationReportFilterVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 定时报告id
     */
    private Long reportId;

    /**
     * 任务id
     */
    private Long taskId;

    /**
     * 资源id
     */
    private Long resourceId;

    /**
     * 资源类型
     */
    private String dvType;

    /**
     * 组件id
     */
    private Long componentId;

    /**
     * 过滤项id
     */
    private Long filterId;

    /**
     * 过滤组件内容
     */
    private String filterInfo;

    /**
     * 过滤组件版本
     */
    private Integer filterVersion;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 创建人
     */
    private String createUser;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public String getDvType() {
        return dvType;
    }

    public void setDvType(String dvType) {
        this.dvType = dvType;
    }

    public Long getComponentId() {
        return componentId;
    }

    public void setComponentId(Long componentId) {
        this.componentId = componentId;
    }

    public Long getFilterId() {
        return filterId;
    }

    public void setFilterId(Long filterId) {
        this.filterId = filterId;
    }

    public String getFilterInfo() {
        return filterInfo;
    }

    public void setFilterInfo(String filterInfo) {
        this.filterInfo = filterInfo;
    }

    public Integer getFilterVersion() {
        return filterVersion;
    }

    public void setFilterVersion(Integer filterVersion) {
        this.filterVersion = filterVersion;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    @Override
    public String toString() {
        return "VisualizationReportFilter{" +
                "id = " + id +
                ", reportId = " + reportId +
                ", taskId = " + taskId +
                ", resourceId = " + resourceId +
                ", dvType = " + dvType +
                ", componentId = " + componentId +
                ", filterId = " + filterId +
                ", filterInfo = " + filterInfo +
                ", filterVersion = " + filterVersion +
                ", createTime = " + createTime +
                ", createUser = " + createUser +
                "}";
    }
}
