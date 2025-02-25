package io.dataease.api.xpack.dataFilling.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


@Data
public class DataFillingDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 父级ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long pid;

    /**
     * folder/form 目录或文件夹
     */
    private String nodeType;

    /**
     * 表名
     */
    private String tableName;

    /**
     * 数据源
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long datasource;

    private String datasourceName;

    /**
     * 表单内容
     */
    private String forms;

    /**
     * 是否创建索引
     */
    private Boolean createIndex;

    /**
     * 索引
     */
    private String tableIndexes;

    /**
     * 创建人
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long createBy;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 更新人
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long updateBy;

    /**
     * 更新时间
     */
    private Long updateTime;

    private String creator;
    private String updater;

    private boolean useExistsTable;
}
