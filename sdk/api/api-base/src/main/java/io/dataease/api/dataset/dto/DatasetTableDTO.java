package io.dataease.api.dataset.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DatasetTableDTO implements Serializable {

    /**
     * ID
     */
    private String id;

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
    private String datasourceId;

    /**
     * 数据集ID
     */
    private String datasetGroupId;

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
