package io.dataease.api.visualization.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * @author : WangJiaHao
 * @date : 2023/7/18 12:40
 */
@Data
public class VisualizationLinkJumpVO {
    private static final long serialVersionUID = 1L;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 源仪表板ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sourceDvId;

    /**
     * 源视图ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sourceViewId;

    /**
     * 跳转信息
     */
    private String linkJumpInfo;

    /**
     * 是否启用
     */
    private Boolean checked;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long copyFrom;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long copyId;
}
