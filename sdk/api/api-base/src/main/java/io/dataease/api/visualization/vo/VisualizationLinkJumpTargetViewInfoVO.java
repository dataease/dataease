package io.dataease.api.visualization.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * @author : WangJiaHao
 * @date : 2023/7/18 14:18
 */
@Data
public class VisualizationLinkJumpTargetViewInfoVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long targetId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long linkJumpInfoId;
    /**
     * 勾选字段设置的匹配字段，也可以不是勾选字段本身
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sourceFieldActiveId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long targetViewId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long targetFieldId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long copyFrom;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long copyId;
}
