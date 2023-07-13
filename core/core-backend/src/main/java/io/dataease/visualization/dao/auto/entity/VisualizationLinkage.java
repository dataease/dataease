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
@TableName("visualization_linkage")
public class VisualizationLinkage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String dvId;

    /**
     * 源视图id
     */
    private String sourceViewId;

    /**
     * 联动视图id
     */
    private String targetViewId;

    /**
     * 更新时间
     */
    private Long updateTime;

    /**
     * 更新人
     */
    private String updatePeople;

    /**
     * 是否启用关联
     */
    private Boolean linkageActive;

    private String ext1;

    private String ext2;

    private String copyFrom;

    private String copyId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDvId() {
        return dvId;
    }

    public void setDvId(String dvId) {
        this.dvId = dvId;
    }

    public String getSourceViewId() {
        return sourceViewId;
    }

    public void setSourceViewId(String sourceViewId) {
        this.sourceViewId = sourceViewId;
    }

    public String getTargetViewId() {
        return targetViewId;
    }

    public void setTargetViewId(String targetViewId) {
        this.targetViewId = targetViewId;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public String getUpdatePeople() {
        return updatePeople;
    }

    public void setUpdatePeople(String updatePeople) {
        this.updatePeople = updatePeople;
    }

    public Boolean getLinkageActive() {
        return linkageActive;
    }

    public void setLinkageActive(Boolean linkageActive) {
        this.linkageActive = linkageActive;
    }

    public String getExt1() {
        return ext1;
    }

    public void setExt1(String ext1) {
        this.ext1 = ext1;
    }

    public String getExt2() {
        return ext2;
    }

    public void setExt2(String ext2) {
        this.ext2 = ext2;
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
        return "VisualizationLinkage{" +
        "id = " + id +
        ", dvId = " + dvId +
        ", sourceViewId = " + sourceViewId +
        ", targetViewId = " + targetViewId +
        ", updateTime = " + updateTime +
        ", updatePeople = " + updatePeople +
        ", linkageActive = " + linkageActive +
        ", ext1 = " + ext1 +
        ", ext2 = " + ext2 +
        ", copyFrom = " + copyFrom +
        ", copyId = " + copyId +
        "}";
    }
}
