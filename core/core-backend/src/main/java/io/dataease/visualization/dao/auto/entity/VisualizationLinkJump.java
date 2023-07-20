package io.dataease.visualization.dao.auto.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author fit2cloud
 * @since 2023-07-18
 */
@TableName("visualization_link_jump")
public class VisualizationLinkJump implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * 源仪表板ID
     */
    private String sourceDvId;

    /**
     * 源视图ID
     */
    private String sourceViewId;

    /**
     * 跳转信息
     */
    private String linkJumpInfo;

    /**
     * 是否启用
     */
    private Boolean checked;

    private String copyFrom;

    private String copyId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSourceDvId() {
        return sourceDvId;
    }

    public void setSourceDvId(String sourceDvId) {
        this.sourceDvId = sourceDvId;
    }

    public String getSourceViewId() {
        return sourceViewId;
    }

    public void setSourceViewId(String sourceViewId) {
        this.sourceViewId = sourceViewId;
    }

    public String getLinkJumpInfo() {
        return linkJumpInfo;
    }

    public void setLinkJumpInfo(String linkJumpInfo) {
        this.linkJumpInfo = linkJumpInfo;
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
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
        return "VisualizationLinkJump{" +
        "id = " + id +
        ", sourceDvId = " + sourceDvId +
        ", sourceViewId = " + sourceViewId +
        ", linkJumpInfo = " + linkJumpInfo +
        ", checked = " + checked +
        ", copyFrom = " + copyFrom +
        ", copyId = " + copyId +
        "}";
    }
}
