package io.dataease.visualization.dao.auto.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 跳转目标仪表板图表字段配置表
 * </p>
 *
 * @author fit2cloud
 * @since 2025-03-24
 */
@TableName("snapshot_visualization_link_jump_target_view_info")
public class SnapshotVisualizationLinkJumpTargetViewInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId("target_id")
    private Long targetId;

    /**
     * visualization_link_jump_info 表的 ID
     */
    private Long linkJumpInfoId;

    /**
     * 勾选字段设置的匹配字段，也可以不是勾选字段本身
     */
    private Long sourceFieldActiveId;

    /**
     * 目标图表ID
     */
    private String targetViewId;

    /**
     * 目标字段ID
     */
    private String targetFieldId;

    /**
     * 复制来源
     */
    private Long copyFrom;

    /**
     * 复制来源ID
     */
    private Long copyId;

    /**
     * 联动目标类型 view 图表 filter 过滤组件 outParams 外部参数
     */
    private String targetType;

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

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    @Override
    public String toString() {
        return "SnapshotVisualizationLinkJumpTargetViewInfo{" +
        "targetId = " + targetId +
        ", linkJumpInfoId = " + linkJumpInfoId +
        ", sourceFieldActiveId = " + sourceFieldActiveId +
        ", targetViewId = " + targetViewId +
        ", targetFieldId = " + targetFieldId +
        ", copyFrom = " + copyFrom +
        ", copyId = " + copyId +
        ", targetType = " + targetType +
        "}";
    }
}
