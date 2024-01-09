package io.dataease.api.visualization.vo;

public class VisualizationWatermarkVO{

    /**
     * 主键
     */
    private String id;

    /**
     * 版本号
     */
    private String version;

    /**
     * 设置内容
     */
    private String settingContent;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private Long createTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSettingContent() {
        return settingContent;
    }

    public void setSettingContent(String settingContent) {
        this.settingContent = settingContent;
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

    @Override
    public String toString() {
        return "VisualizationWatermark{" +
        "id = " + id +
        ", version = " + version +
        ", settingContent = " + settingContent +
        ", createBy = " + createBy +
        ", createTime = " + createTime +
        "}";
    }
}
