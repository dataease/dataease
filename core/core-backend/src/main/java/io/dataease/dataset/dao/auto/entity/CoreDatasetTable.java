package io.dataease.dataset.dao.auto.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author fit2cloud
 * @since 2023-03-22
 */
@TableName("core_dataset_table")
public class CoreDatasetTable implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private String id;

    /**
     * 名称
     */
    private String name;

    /**
     * 父级ID
     */
    private String pid;

    /**
     * 当前分组处于第几级
     */
    private Integer level;

    /**
     * node类型：folder or dataset
     */
    private String nodeType;

    /**
     * 数据源ID
     */
    private String datasourceId;

    /**
     * db,sql,union
     */
    private String type;

    /**
     * 连接模式：0-直连，1-同步(excel、api等数据存在de中的表)
     */
    private Integer mode;

    /**
     * 表原始信息
     */
    private String info;

    /**
     * 创建人ID
     */
    private String createBy;

    /**
     * 创建时间
     */
    private Long createTime;

    private String qrtzInstance;

    /**
     * 同步状态
     */
    private String syncStatus;

    /**
     * 最后同步时间
     */
    private Long lastUpdateTime;

    /**
     * SQL数据集参数
     */
    private String sqlVariableDetails;

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

    public String getDatasourceId() {
        return datasourceId;
    }

    public void setDatasourceId(String datasourceId) {
        this.datasourceId = datasourceId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getMode() {
        return mode;
    }

    public void setMode(Integer mode) {
        this.mode = mode;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public String getQrtzInstance() {
        return qrtzInstance;
    }

    public void setQrtzInstance(String qrtzInstance) {
        this.qrtzInstance = qrtzInstance;
    }

    public String getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
    }

    public Long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getSqlVariableDetails() {
        return sqlVariableDetails;
    }

    public void setSqlVariableDetails(String sqlVariableDetails) {
        this.sqlVariableDetails = sqlVariableDetails;
    }

    @Override
    public String toString() {
        return "CoreDatasetTable{" +
        "id = " + id +
        ", name = " + name +
        ", pid = " + pid +
        ", level = " + level +
        ", nodeType = " + nodeType +
        ", datasourceId = " + datasourceId +
        ", type = " + type +
        ", mode = " + mode +
        ", info = " + info +
        ", createBy = " + createBy +
        ", createTime = " + createTime +
        ", qrtzInstance = " + qrtzInstance +
        ", syncStatus = " + syncStatus +
        ", lastUpdateTime = " + lastUpdateTime +
        ", sqlVariableDetails = " + sqlVariableDetails +
        "}";
    }
}
