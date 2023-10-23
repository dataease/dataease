package io.dataease.visualization.dao.auto.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author fit2cloud
 * @since 2023-09-22
 */
@TableName("visualization_linkage_field")
public class VisualizationLinkageField implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 联动ID
     */
    private Long linkageId;

    /**
     * 源视图字段
     */
    private Long sourceField;

    /**
     * 目标视图字段
     */
    private Long targetField;

    /**
     * 更新时间
     */
    private Long updateTime;

    private Long copyFrom;

    private Long copyId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLinkageId() {
        return linkageId;
    }

    public void setLinkageId(Long linkageId) {
        this.linkageId = linkageId;
    }

    public Long getSourceField() {
        return sourceField;
    }

    public void setSourceField(Long sourceField) {
        this.sourceField = sourceField;
    }

    public Long getTargetField() {
        return targetField;
    }

    public void setTargetField(Long targetField) {
        this.targetField = targetField;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public Long getCopyFrom() {
        return copyFrom;
    }

    public void setCopyFrom(Long copyFrom) {
        this.copyFrom = copyFrom;
    }

    public Long getCopyId() {
        return copyId;
    }

    public void setCopyId(Long copyId) {
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
