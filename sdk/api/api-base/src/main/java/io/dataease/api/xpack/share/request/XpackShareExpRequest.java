package io.dataease.api.xpack.share.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "有效期设置器")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class XpackShareExpRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 5519219260721146347L;
    @Schema(description = "资源ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long resourceId;

    @Schema(description = "有效期")
    private Long exp;
}
