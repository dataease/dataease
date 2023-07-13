package io.dataease.visualization.dao.auto.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author fit2cloud
 * @since 2023-07-13
 */
@TableName("visualization_linkage_field")
public class VisualizationLinkageField implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * 联动ID
     */
    private String linkageId;

    /**
     * 源视图字段
     */
    private String sourceField;

    /**
     * 目标视图字段
     */
    private String targetField;

    /**
     * 更新时间
     */
    private Long updateTime;

    private String copyFrom;

    private String copyId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLinkageId() {
        return linkageId;
    }

    public void setLinkageId(String linkageId) {
        this.linkageId = linkageId;
    }

    public String getSourceField() {
        return sourceField;
    }

    public void setSourceField(String sourceField) {
        this.sourceField = sourceField;
    }

    public String getTargetField() {
        return targetField;
    }

    public void setTargetField(String targetField) {
        this.targetField = targetField;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public String getCopyFrom() {
        return copyFrom;
    }

    public void setCopyFrom(String copyFrom) {
        this.copyFrom = copyFrom;
    }

    public String getCopyId() {
        return copyId;
    }

    public void setCopyId(String copyId) {
        this.copyId = copyId;
    }

    @Override
    public String toString() {
        return "VisualizationLinkageField{" +
        "id = " + id +
        ", linkageId = " + linkageId +
        ", sourceField = " + sourceField +
        ", targetField = " + targetField +
        ", updateTime = " + updateTime +
        ", copyFrom = " + copyFrom +
        ", copyId = " + copyId +
        "}";
    }
}
