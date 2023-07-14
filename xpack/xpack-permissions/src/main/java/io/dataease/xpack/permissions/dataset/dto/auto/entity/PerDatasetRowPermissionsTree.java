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
@TableName("per_dataset_row_permissions_tree")
public class PerDatasetRowPermissionsTree implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 是否启用
     */
    private Boolean enable;

    /**
     * 权限类型：dept/role/user
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
     * 关系表达式
     */
    private String expressionTree;

    /**
     * 用户白名单
     */
    private String whiteListUser;

    /**
     * 角色白名单
     */
    private String whiteListRole;

    /**
     * 组织白名单
     */
    private String whiteListDept;

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

    public String getExpressionTree() {
        return expressionTree;
    }

    public void setExpressionTree(String expressionTree) {
        this.expressionTree = expressionTree;
    }

    public String getWhiteListUser() {
        return whiteListUser;
    }

    public void setWhiteListUser(String whiteListUser) {
        this.whiteListUser = whiteListUser;
    }

    public String getWhiteListRole() {
        return whiteListRole;
    }

    public void setWhiteListRole(String whiteListRole) {
        this.whiteListRole = whiteListRole;
    }

    public String getWhiteListDept() {
        return whiteListDept;
    }

    public void setWhiteListDept(String whiteListDept) {
        this.whiteListDept = whiteListDept;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "PerDatasetRowPermissionsTree{" +
        "id = " + id +
        ", enable = " + enable +
        ", authTargetType = " + authTargetType +
        ", authTargetId = " + authTargetId +
        ", datasetId = " + datasetId +
        ", expressionTree = " + expressionTree +
        ", whiteListUser = " + whiteListUser +
        ", whiteListRole = " + whiteListRole +
        ", whiteListDept = " + whiteListDept +
        ", updateTime = " + updateTime +
        "}";
    }
}
