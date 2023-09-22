package io.dataease.api.visualization.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

@Data
public class VisualizationLinkageFieldVO implements Serializable {

    private static final long serialVersionUID = 1L;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 联动ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long linkageId;

    /**
     * 源视图字段
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sourceField;

    /**
     * 目标视图字段
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long targetField;

    /**
     * 更新时间
     */
    private Long updateTime;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long copyFrom;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long copyId;

}
