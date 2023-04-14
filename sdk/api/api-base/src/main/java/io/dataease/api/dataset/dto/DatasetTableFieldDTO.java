package io.dataease.api.dataset.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author Junjun
 */
@Data
public class DatasetTableFieldDTO implements Serializable {
    /**
     * ID
     */
    private Long id;

    /**
     * 数据源ID
     */
    private Long datasourceId;

    /**
     * 数据表ID
     */
    private Long datasetTableId;

    /**
     * 数据集ID
     */
    private Long datasetGroupId;

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
}
