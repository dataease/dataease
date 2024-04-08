package io.dataease.api.xpack.share.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "分享密码编辑器")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class XpackSharePwdRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -4399320897911936623L;
    @Schema(description = "资源ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long resourceId;

    @Schema(description = "密码")
    private String pwd;
    @Schema(description = "自动生成密码")
    private Boolean autoPwd = true;
}
