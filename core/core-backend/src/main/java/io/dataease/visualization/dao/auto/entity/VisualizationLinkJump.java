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
@TableName("visualization_link_jump")
public class VisualizationLinkJump implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 源仪表板ID
     */
    private Long sourceDvId;

    /**
     * 源视图ID
     */
    private Long sourceViewId;

    /**
     * 跳转信息
     */
    private String linkJumpInfo;

    /**
     * 是否启用
     */
    private Boolean checked;

    private Long copyFrom;

    private Long copyId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSourceDvId() {
        return sourceDvId;
    }

    public void setSourceDvId(Long sourceDvId) {
        this.sourceDvId = sourceDvId;
    }

    public Long getSourceViewId() {
        return sourceViewId;
    }

    public void setSourceViewId(Long sourceViewId) {
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
