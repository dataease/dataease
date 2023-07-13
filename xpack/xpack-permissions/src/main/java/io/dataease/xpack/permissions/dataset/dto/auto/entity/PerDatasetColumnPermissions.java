package io.dataease.xpack.permissions.dataset.dto.auto.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author fit2cloud
 * @since 2023-07-06
 */
@TableName("per_dataset_column_permissions")
public class PerDatasetColumnPermissions implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * File ID
     */
    private Long id;

    /**
     * 是否启用
     */
    private Boolean enable;

    /**
     * 权限类型：组织/角色/用户
     */
    private String authTargetType;

    /**
     * 权限对象ID
     */
    private Long authTargetId;

    /**
     * 数据集ID
     */
    private Long datasetId;

    /**
     * 权限
     */
    private String permissions;

    /**
     * 白名单
     */
    private String whiteListUser;

    private Long updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public String getAuthTargetType() {
        return authTargetType;
    }

    public void setAuthTargetType(String authTargetType) {
        this.authTargetType = authTargetType;
    }

    public Long getAuthTargetId() {
        return authTargetId;
    }

    public void setAuthTargetId(Long authTargetId) {
        this.authTargetId = authTargetId;
    }

    public Long getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(Long datasetId) {
        this.datasetId = datasetId;
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    public String getWhiteListUser() {
        return whiteListUser;
    }

    public void setWhiteListUser(String whiteListUser) {
        this.whiteListUser = whiteListUser;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "PerDatasetColumnPermissions{" +
        "id = " + id +
        ", enable = " + enable +
        ", authTargetType = " + authTargetType +
        ", authTargetId = " + authTargetId +
        ", datasetId = " + datasetId +
        ", permissions = " + permissions +
        ", whiteListUser = " + whiteListUser +
        ", updateTime = " + updateTime +
        "}";
    }
}
