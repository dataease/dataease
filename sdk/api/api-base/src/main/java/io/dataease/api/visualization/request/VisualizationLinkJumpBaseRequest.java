package io.dataease.api.visualization.request;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * @author : WangJiaHao
 * @date : 2023/7/18 14:26
 */
@Data
public class VisualizationLinkJumpBaseRequest {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long sourceDvId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sourceViewId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sourceFieldId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long targetDvId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long targetViewId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long linkJumpId;

    private Boolean activeStatus;

    public VisualizationLinkJumpBaseRequest() {
    }

    public VisualizationLinkJumpBaseRequest(Long sourceDvId, Long sourceViewId, Long targetDvId, Long targetViewId, Long linkJumpId) {
        this.sourceDvId = sourceDvId;
        this.sourceViewId = sourceViewId;
        this.targetDvId = targetDvId;
        this.targetViewId = targetViewId;
        this.linkJumpId = linkJumpId;
    }
}
