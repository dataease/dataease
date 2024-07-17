package io.dataease.template.dao.auto.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 模板表
 * </p>
 *
 * @author fit2cloud
 * @since 2024-07-17
 */
@TableName("visualization_template")
public class VisualizationTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private String id;

    /**
     * 名称
     */
    private String name;

    /**
     * 父级id
     */
    private String pid;

    /**
     * 层级
     */
    private Integer level;

    /**
     * 模板种类  dataV or dashboard 目录或者文件夹
     */
    private String dvType;

    /**
     * 节点类型  app or template 应用 或者 模板
     */
    private String nodeType;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 缩略图
     */
    private String snapshot;

    /**
     * 模板类型 system 系统内置 self 用户自建 
     */
    private String templateType;

    /**
     * template 样式
     */
    private String templateStyle;

    /**
     * template 数据
     */
    private String templateData;

    /**
     * 预存数据
     */
    private String dynamicData;

    /**
     * app数据
     */
    private String appData;

    /**
     * 使用次数
     */
    private Integer useCount;

    private Integer version;

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

    public String getDvType() {
        return dvType;
    }

    public void setDvType(String dvType) {
        this.dvType = dvType;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
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

    public String getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(String snapshot) {
        this.snapshot = snapshot;
    }

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    public String getTemplateStyle() {
        return templateStyle;
    }

    public void setTemplateStyle(String templateStyle) {
        this.templateStyle = templateStyle;
    }

    public String getTemplateData() {
        return templateData;
    }

    public void setTemplateData(String templateData) {
        this.templateData = templateData;
    }

    public String getDynamicData() {
        return dynamicData;
    }

    public void setDynamicData(String dynamicData) {
        this.dynamicData = dynamicData;
    }

    public String getAppData() {
        return appData;
    }

    public void setAppData(String appData) {
        this.appData = appData;
    }

    public Integer getUseCount() {
        return useCount;
    }

    public void setUseCount(Integer useCount) {
        this.useCount = useCount;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "VisualizationTemplate{" +
        "id = " + id +
        ", name = " + name +
        ", pid = " + pid +
        ", level = " + level +
        ", dvType = " + dvType +
        ", nodeType = " + nodeType +
        ", createBy = " + createBy +
        ", createTime = " + createTime +
        ", snapshot = " + snapshot +
        ", templateType = " + templateType +
        ", templateStyle = " + templateStyle +
        ", templateData = " + templateData +
        ", dynamicData = " + dynamicData +
        ", appData = " + appData +
        ", useCount = " + useCount +
        ", version = " + version +
        "}";
    }
}
