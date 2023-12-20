package io.dataease.api.visualization.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "分享列表过滤器")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VisualizationWorkbranchQueryRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -3522243514336261778L;

    @Schema(description = "类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String type;
    @Schema(description = "关键字")
    private String keyword;
    @Schema(description = "查询来源")
    private String queryFrom;
    @Schema(description = "是否升序", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean asc = false;
}
