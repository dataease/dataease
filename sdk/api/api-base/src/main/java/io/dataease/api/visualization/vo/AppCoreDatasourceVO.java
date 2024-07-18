package io.dataease.api.visualization.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

@Data
public class AppCoreDatasourceVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 名称
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

    /**
     * 父级ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long pid;

    /**
     * 更新方式：0：替换；1：追加
     */
    private String editType;

    /**
     * 详细信息
     */
    private String configuration;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 更新时间
     */
    private Long updateTime;

    /**
     * 变更人
     */
    private Long updateBy;

    /**
     * 创建人ID
     */
    private String createBy;

    /**
     * 状态
     */
    private String status;

    /**
     * 状态
     */
    private String qrtzInstance;

    /**
     * 任务状态
     */
    private String taskStatus;

    /**
     * 映射系统数据源ID
     */
    private Long systemDatasourceId;

}
