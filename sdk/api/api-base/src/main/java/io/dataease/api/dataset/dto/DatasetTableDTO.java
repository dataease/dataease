package io.dataease.api.dataset.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

@Data
public class DatasetTableDTO implements Serializable {

    /**
     * ID
     */
    @JsonSerialize(using= ToStringSerializer.class)
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
    @JsonSerialize(using= ToStringSerializer.class)
    private Long datasourceId;

    /**
     * 数据集ID
     */
    @JsonSerialize(using= ToStringSerializer.class)
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
