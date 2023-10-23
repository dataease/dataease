package io.dataease.visualization.dao.auto.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author fit2cloud
 * @since 2023-07-12
 */
@TableName("visualization_subject")
public class VisualizationSubject implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * 主题名称
     */
    private String name;

    /**
     * 主题类型 system 系统主题，self 自定义主题
     */
    private String type;

    /**
     * 主题内容
     */
    private String details;

    /**
     * 删除标记
     */
    private Boolean deleteFlag;

    /**
     * 封面信息
     */
    private String coverUrl;

    private Integer createNum;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新时间
     */
    private Long updateTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 删除时间
     */
    private Long deleteTime;

    /**
     * 删除人
     */
    private Long deleteBy;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Boolean getDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(Boolean deleteFlag) {
        this.deleteFlag = deleteFlag;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public Integer getCreateNum() {
        return createNum;
    }

    public void setCreateNum(Integer createNum) {
        this.createNum = createNum;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Long getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(Long deleteTime) {
        this.deleteTime = deleteTime;
    }

    public Long getDeleteBy() {
        return deleteBy;
    }

    public void setDeleteBy(Long deleteBy) {
        this.deleteBy = deleteBy;
    }

    @Override
    public String toString() {
        return "VisualizationSubject{" +
        "id = " + id +
        ", name = " + name +
        ", type = " + type +
        ", details = " + details +
        ", deleteFlag = " + deleteFlag +
        ", coverUrl = " + coverUrl +
        ", createNum = " + createNum +
        ", createTime = " + createTime +
        ", createBy = " + createBy +
        ", updateTime = " + updateTime +
        ", updateBy = " + updateBy +
        ", deleteTime = " + deleteTime +
        ", deleteBy = " + deleteBy +
        "}";
    }
}
