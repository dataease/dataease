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
     * 报告ID
     */
    private Long id;

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
