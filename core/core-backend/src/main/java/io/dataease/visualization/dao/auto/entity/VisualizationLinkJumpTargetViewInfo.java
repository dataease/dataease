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
@TableName("visualization_link_jump_target_view_info")
public class VisualizationLinkJumpTargetViewInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long targetId;

    private Long linkJumpInfoId;

    /**
     * 勾选字段设置的匹配字段，也可以不是勾选字段本身
     */
    private Long sourceFieldActiveId;

    private Long targetViewId;

    private Long targetFieldId;

    private Long copyFrom;

    private Long copyId;

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public Long getLinkJumpInfoId() {
        return linkJumpInfoId;
    }

    public void setLinkJumpInfoId(Long linkJumpInfoId) {
        this.linkJumpInfoId = linkJumpInfoId;
    }

    public Long getSourceFieldActiveId() {
        return sourceFieldActiveId;
    }

    public void setSourceFieldActiveId(Long sourceFieldActiveId) {
        this.sourceFieldActiveId = sourceFieldActiveId;
    }

    public Long getTargetViewId() {
        return targetViewId;
    }

    public void setTargetViewId(Long targetViewId) {
        this.targetViewId = targetViewId;
    }

    public Long getTargetFieldId() {
        return targetFieldId;
    }

    public void setTargetFieldId(Long targetFieldId) {
        this.targetFieldId = targetFieldId;
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
