package io.dataease.api.ds.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class DatasourceDTO implements Serializable {


    @Serial
    private static final long serialVersionUID = 1175287571828910222L;

    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using= ToStringSerializer.class)
    private Long pid;
    /**
     * 数据源名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 类型
     */
    private String type;

    private String typeAlias;

    private String catalog;

    private String catalogDesc;

    /**
     * 详细信息
     */
    private String configuration;

    private String apiConfigurationStr;

    /**
     * Create timestamp
     */
    private Long createTime;

    /**
     * Update timestamp
     */
    private Long updateTime;

    /**
     * 创建人ID
     */
    private String createBy;

    /**
     * 状态
     */
    private String status;

    private TaskDTO syncSetting;

    private Integer editType;
    private String  nodeType;
    private String  action;
}
