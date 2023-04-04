package io.dataease.visualization.dao.auto.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author fit2cloud
 * @since 2023-04-04
 */
@TableName("data_visualization_info")
public class DataVisualizationInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * 名称
     */
    private String name;

    /**
     * 父id
     */
    private String pid;

    /**
     * 所属组织id
     */
    private String orgId;

    /**
     * 层级
     */
    private Integer level;

    /**
     * 节点类型  folder or panel 目录或者文件夹
     */
    private String nodeType;

    /**
     * 类型
     */
    private String type;

    /**
     * 样式数据
     */
    private String canvasStyleData;

    /**
     * 组件数据
     */
    private String componentData;

    /**
     * 移动端布局
     */
    private String mobileLayout;

    /**
     * 状态 0-未发布 1-已发布
     */
    private Integer status;

    /**
     * 是否单独打开水印 0-关闭 1-开启
     */
    private Integer selfWatermarkStatus;

    /**
     * 排序
     */
    private Integer sort;

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
     * 备注
     */
    private String remark;

    /**
     * 数据来源
     */
    private String source;

    /**
     * 删除标志
     */
    private Boolean deleteFlag;

    /**
     * 删除时间
     */
    private Long deleteTime;

    /**
     * 删除人
     */
    private String deleteBy;

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

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCanvasStyleData() {
        return canvasStyleData;
    }

    public void setCanvasStyleData(String canvasStyleData) {
        this.canvasStyleData = canvasStyleData;
    }

    public String getComponentData() {
        return componentData;
    }

    public void setComponentData(String componentData) {
        this.componentData = componentData;
    }

    public String getMobileLayout() {
        return mobileLayout;
    }

    public void setMobileLayout(String mobileLayout) {
        this.mobileLayout = mobileLayout;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getSelfWatermarkStatus() {
        return selfWatermarkStatus;
    }

    public void setSelfWatermarkStatus(Integer selfWatermarkStatus) {
        this.selfWatermarkStatus = selfWatermarkStatus;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Boolean getDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(Boolean deleteFlag) {
        this.deleteFlag = deleteFlag;
    }

    public Long getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(Long deleteTime) {
        this.deleteTime = deleteTime;
    }

    public String getDeleteBy() {
        return deleteBy;
    }

    public void setDeleteBy(String deleteBy) {
        this.deleteBy = deleteBy;
    }

    @Override
    public String toString() {
        return "DataVisualizationInfo{" +
        "id = " + id +
        ", name = " + name +
        ", pid = " + pid +
        ", orgId = " + orgId +
        ", level = " + level +
        ", nodeType = " + nodeType +
        ", type = " + type +
        ", canvasStyleData = " + canvasStyleData +
        ", componentData = " + componentData +
        ", mobileLayout = " + mobileLayout +
        ", status = " + status +
        ", selfWatermarkStatus = " + selfWatermarkStatus +
        ", sort = " + sort +
        ", createTime = " + createTime +
        ", createBy = " + createBy +
        ", updateTime = " + updateTime +
        ", updateBy = " + updateBy +
        ", remark = " + remark +
        ", source = " + source +
        ", deleteFlag = " + deleteFlag +
        ", deleteTime = " + deleteTime +
        ", deleteBy = " + deleteBy +
        "}";
    }
}
