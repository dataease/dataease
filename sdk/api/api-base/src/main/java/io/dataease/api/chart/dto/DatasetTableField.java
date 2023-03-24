package io.dataease.api.chart.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DatasetTableField implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private String id;

    /**
     * 表ID
     */
    private String datasetTableId;

    /**
     * 原始字段名
     */
    private String originName;

    /**
     * 字段名用于展示
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * de字段名用作唯一标识
     */
    private String dataeaseName;

    /**
     * 维度/指标标识 d:维度，q:指标
     */
    private String groupType;

    /**
     * 原始字段类型
     */
    private String type;

    private Integer size;

    /**
     * dataease字段类型：0-文本，1-时间，2-整型数值，3-浮点数值，4-布尔，5-地理位置，6-二进制
     */
    private Integer deType;

    /**
     * de记录的原始类型
     */
    private Integer deExtractType;

    /**
     * 是否扩展字段 0原始 1复制 2计算字段...
     */
    private Integer extField;

    /**
     * 是否选中
     */
    private Boolean checked;

    /**
     * 列位置
     */
    private Integer columnIndex;

    /**
     * 同步时间
     */
    private Long lastSyncTime;

    /**
     * 精度
     */
    private Integer accuracy;

    private String dateFormat;

    /**
     * 时间格式类型
     */
    private String dateFormatType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDatasetTableId() {
        return datasetTableId;
    }

    public void setDatasetTableId(String datasetTableId) {
        this.datasetTableId = datasetTableId;
    }

    public String getOriginName() {
        return originName;
    }

    public void setOriginName(String originName) {
        this.originName = originName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDataeaseName() {
        return dataeaseName;
    }

    public void setDataeaseName(String dataeaseName) {
        this.dataeaseName = dataeaseName;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getDeType() {
        return deType;
    }

    public void setDeType(Integer deType) {
        this.deType = deType;
    }

    public Integer getDeExtractType() {
        return deExtractType;
    }

    public void setDeExtractType(Integer deExtractType) {
        this.deExtractType = deExtractType;
    }

    public Integer getExtField() {
        return extField;
    }

    public void setExtField(Integer extField) {
        this.extField = extField;
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

    public Integer getColumnIndex() {
        return columnIndex;
    }

    public void setColumnIndex(Integer columnIndex) {
        this.columnIndex = columnIndex;
    }

    public Long getLastSyncTime() {
        return lastSyncTime;
    }

    public void setLastSyncTime(Long lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }

    public Integer getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Integer accuracy) {
        this.accuracy = accuracy;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getDateFormatType() {
        return dateFormatType;
    }

    public void setDateFormatType(String dateFormatType) {
        this.dateFormatType = dateFormatType;
    }

    @Override
    public String toString() {
        return "CoreDatasetTableField{" +
                "id = " + id +
                ", datasetTableId = " + datasetTableId +
                ", originName = " + originName +
                ", name = " + name +
                ", description = " + description +
                ", dataeaseName = " + dataeaseName +
                ", groupType = " + groupType +
                ", type = " + type +
                ", size = " + size +
                ", deType = " + deType +
                ", deExtractType = " + deExtractType +
                ", extField = " + extField +
                ", checked = " + checked +
                ", columnIndex = " + columnIndex +
                ", lastSyncTime = " + lastSyncTime +
                ", accuracy = " + accuracy +
                ", dateFormat = " + dateFormat +
                ", dateFormatType = " + dateFormatType +
                "}";
    }
}
