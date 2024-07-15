package io.dataease.api.visualization.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class AppCoreDatasetTableVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 物理表名
     */
    private String tableName;

    /**
     * 数据源ID
     */
    private Long datasourceId;

    /**
     * 数据集ID
     */
    private Long datasetGroupId;

    /**
     * db,sql,union,excel,api
     */
    private String type;

    /**
     * 表原始信息,表名,sql等
     */
    private String info;

    /**
     * SQL参数
     */
    private String sqlVariableDetails;
}
