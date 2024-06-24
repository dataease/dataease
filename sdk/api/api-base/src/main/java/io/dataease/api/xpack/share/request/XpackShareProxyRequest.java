package io.dataease.api.xpack.share.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "分享代理信息过滤器")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class XpackShareProxyRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 7758730984988104057L;
    @Schema(description = "分享UUID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String uuid;
    @Schema(description = "密钥", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ciphertext;
    private boolean inIframe;

    private String ticket;
}
