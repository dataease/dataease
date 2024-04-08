package io.dataease.api.xpack.share.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Schema(description = "分享UUID编辑器")
@Data
public class XpackShareUuidEditor implements Serializable {

    @Schema(description = "资源ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long resourceId;
    @Schema(description = "分享UUID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String uuid;
}
