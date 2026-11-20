package io.dataease.api.visualization.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.dataease.constant.CommonConstants;
import lombok.Data;

/**
 * @author : WangJiaHao
 * @date : 2023/7/18 14:26
 */
@Data
public class VisualizationLinkJumpBaseRequest {

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private VisualizationDraftRevision revision;

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

    private String resourceTable = CommonConstants.RESOURCE_TABLE.CORE;

    // 缺省沿用仪表板/大屏；电子表格目标始终校验正式稿。
    private String targetResourceType;

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
