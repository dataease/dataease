package io.dataease.visualization.dao.auto.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author fit2cloud
 * @since 2023-07-19
 */
@TableName("visualization_link_jump_target_view_info")
public class VisualizationLinkJumpTargetViewInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String targetId;

    private String linkJumpInfoId;

    /**
     * 勾选字段设置的匹配字段，也可以不是勾选字段本身
     */
    private String sourceFieldActiveId;

    private String targetViewId;

    private String targetFieldId;

    private String copyFrom;

    private String copyId;

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getLinkJumpInfoId() {
        return linkJumpInfoId;
    }

    public void setLinkJumpInfoId(String linkJumpInfoId) {
        this.linkJumpInfoId = linkJumpInfoId;
    }

    public String getSourceFieldActiveId() {
        return sourceFieldActiveId;
    }

    public void setSourceFieldActiveId(String sourceFieldActiveId) {
        this.sourceFieldActiveId = sourceFieldActiveId;
    }

    public String getTargetViewId() {
        return targetViewId;
    }

    public void setTargetViewId(String targetViewId) {
        this.targetViewId = targetViewId;
    }

    public String getTargetFieldId() {
        return targetFieldId;
    }

    public void setTargetFieldId(String targetFieldId) {
        this.targetFieldId = targetFieldId;
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
        return "VisualizationLinkJumpTargetViewInfo{" +
        "targetId = " + targetId +
        ", linkJumpInfoId = " + linkJumpInfoId +
        ", sourceFieldActiveId = " + sourceFieldActiveId +
        ", targetViewId = " + targetViewId +
        ", targetFieldId = " + targetFieldId +
        ", copyFrom = " + copyFrom +
        ", copyId = " + copyId +
        "}";
    }
}
