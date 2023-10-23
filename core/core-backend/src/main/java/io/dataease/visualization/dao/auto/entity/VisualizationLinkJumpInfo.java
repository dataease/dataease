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
@TableName("visualization_link_jump_info")
public class VisualizationLinkJumpInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * link jump ID
     */
    private Long linkJumpId;

    /**
     * 关联类型 inner 内部仪表板，outer 外部链接
     */
    private String linkType;

    /**
     * 跳转类型 _blank 新开页面 _self 当前窗口
     */
    private String jumpType;

    /**
     * 关联仪表板ID
     */
    private Long targetDvId;

    /**
     * 字段ID
     */
    private Long sourceFieldId;

    /**
     * 内容 linkType = outer时使用
     */
    private String content;

    /**
     * 是否可用
     */
    private Boolean checked;

    /**
     * 是否附加点击参数
     */
    private Boolean attachParams;

    private Long copyFrom;

    private Long copyId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLinkJumpId() {
        return linkJumpId;
    }

    public void setLinkJumpId(Long linkJumpId) {
        this.linkJumpId = linkJumpId;
    }

    public String getLinkType() {
        return linkType;
    }

    public void setLinkType(String linkType) {
        this.linkType = linkType;
    }

    public String getJumpType() {
        return jumpType;
    }

    public void setJumpType(String jumpType) {
        this.jumpType = jumpType;
    }

    public Long getTargetDvId() {
        return targetDvId;
    }

    public void setTargetDvId(Long targetDvId) {
        this.targetDvId = targetDvId;
    }

    public Long getSourceFieldId() {
        return sourceFieldId;
    }

    public void setSourceFieldId(Long sourceFieldId) {
        this.sourceFieldId = sourceFieldId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

    public Boolean getAttachParams() {
        return attachParams;
    }

    public void setAttachParams(Boolean attachParams) {
        this.attachParams = attachParams;
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
        return "VisualizationLinkJumpInfo{" +
        "id = " + id +
        ", linkJumpId = " + linkJumpId +
        ", linkType = " + linkType +
        ", jumpType = " + jumpType +
        ", targetDvId = " + targetDvId +
        ", sourceFieldId = " + sourceFieldId +
        ", content = " + content +
        ", checked = " + checked +
        ", attachParams = " + attachParams +
        ", copyFrom = " + copyFrom +
        ", copyId = " + copyId +
        "}";
    }
}
