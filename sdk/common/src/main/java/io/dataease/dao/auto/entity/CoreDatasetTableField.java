package io.dataease.dao.auto.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@Entity
@Table(name = "core_dataset_table_field")
@Comment("数据集字段")
public class CoreDatasetTableField {
    @Comment("ID")
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Comment("数据源ID")
    @Column(name = "datasource_id")
    private Long datasourceId;

    @Comment("数据表ID")
    @Column(name = "dataset_table_id")
    private Long datasetTableId;

    @Comment("数据集ID")
    @Column(name = "dataset_group_id")
    private Long datasetGroupId;

    @Comment("图表ID")
    @Column(name = "chart_id")
    private Long chartId;

    @Comment("原始字段名")
    @NotNull
    @Lob
    @Column(name = "origin_name", nullable = false, length = 16777216)
    private String originName;

    @Comment("字段名用于展示")
    @Lob
    @Column(name = "name", length = 16777216)
    private String name;

    @Comment("描述")
    @Lob
    @Column(name = "description", length = 16777216)
    private String description;

    @Comment("de字段名用作唯一标识")
    @Size(max = 255)
    @Column(name = "dataease_name")
    private String dataeaseName;

    @Comment("de字段别名")
    @Size(max = 255)
    @Column(name = "field_short_name")
    private String fieldShortName;

    @Lob
    @Column(name = "group_list", length = 16777216)
    private String groupList;

    @Lob
    @Column(name = "other_group", length = 16777216)
    private String otherGroup;

    @Comment("数据集字段")
    @Size(max = 50)
    @Column(name = "维度/指标标识 d:维度，q:指标", length = 50)
    private String groupType;

    @Comment("原始字段类型")
    @Size(max = 255)
    @NotNull
    @Column(name = "type", nullable = false)
    private String type;

    @Comment("字段长度（允许为空，默认0）")
    @Column(name = "size")
    private Integer size;

    @Comment("dataease字段类型：0-文本，1-时间，2-整型数值，3-浮点数值，4-布尔，5-地理位置，6-二进制，7-URL")
    @NotNull
    @Column(name = "de_type", nullable = false)
    private Integer deType;

    @Comment("de记录的原始类型")
    @NotNull
    @Column(name = "de_extract_type", nullable = false)
    private Integer deExtractType;

    @Comment("是否扩展字段 0原始 1复制 2计算字段...")
    @Column(name = "ext_field")
    private Integer extField;

    @Comment("是否选中")
    @ColumnDefault("1")
    @Column(name = "checked")
    private Boolean checked;

    @Comment("列位置")
    @Column(name = "column_index")
    private Integer columnIndex;

    @Comment("同步时间")
    @Column(name = "last_sync_time")
    private Long lastSyncTime;

    @Comment("精度")
    @ColumnDefault("0")
    @Column(name = "accuracy")
    private Integer accuracy;

    @Comment("时间字段类型")
    @Size(max = 255)
    @Column(name = "date_format")
    private String dateFormat;

    @Comment("时间格式类型")
    @Size(max = 255)
    @Column(name = "date_format_type")
    private String dateFormatType;

    @Comment("计算字段参数")
    @Lob
    @Column(name = "params", length = 16777216)
    private String params;

    public CoreDatasetTableField() {
    }

    public CoreDatasetTableField(Long id, Long datasourceId, Long datasetTableId, Long datasetGroupId,
                                 Long chartId, String originName, String name, String description,
                                 String dataeaseName, String fieldShortName, String groupType,
                                 String type, Integer size, Integer deType, Integer deExtractType,
                                 Integer extField, Boolean checked, Integer columnIndex,
                                 Long lastSyncTime, Integer accuracy, String dateFormat,
                                 String dateFormatType) {
        this.id = id;
        this.datasourceId = datasourceId;
        this.datasetTableId = datasetTableId;
        this.datasetGroupId = datasetGroupId;
        this.chartId = chartId;
        this.originName = originName;
        this.name = name;
        this.description = description;
        this.dataeaseName = dataeaseName;
        this.fieldShortName = fieldShortName;
        this.groupType = groupType;
        this.type = type;
        this.size = size;
        this.deType = deType;
        this.deExtractType = deExtractType;
        this.extField = extField;
        this.checked = checked;
        this.columnIndex = columnIndex;
        this.lastSyncTime = lastSyncTime;
        this.accuracy = accuracy;
        this.dateFormat = dateFormat;
        this.dateFormatType = dateFormatType;
    }
}
