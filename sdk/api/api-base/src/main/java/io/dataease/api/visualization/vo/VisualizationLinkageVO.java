package io.dataease.api.visualization.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

@Data
public class VisualizationLinkageVO implements Serializable {

    private static final long serialVersionUID = 1L;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dvId;

    /**
     * 源图表id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sourceViewId;

    /**
     * 联动图表id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long targetViewId;

    /**
     * 更新时间
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long updateTime;

    /**
     * 更新人
     */
    private String updatePeople;

    /**
     * 是否启用关联
     */
    private Boolean linkageActive;

    private String ext1;

    private String ext2;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long copyFrom;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long copyId;
}
