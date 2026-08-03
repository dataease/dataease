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
    private String targetViewId;
    private String targetFieldId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long copyFrom;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long copyId;

    /**
     * 联动目标类型 view 图表 filter 过滤组件 outParams 外部参数
     */
    private String targetType;

    /**
     * 外部参数名称 当targetType==outParams时 实时查询对应名称
     */
    private String outerParamsName;
}
