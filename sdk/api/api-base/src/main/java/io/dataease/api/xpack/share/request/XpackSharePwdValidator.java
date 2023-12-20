package io.dataease.api.xpack.share.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "分享验证器")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class XpackSharePwdValidator implements Serializable {
    @Serial
    private static final long serialVersionUID = 5723073697210793005L;


    @Schema(description = "密钥")
    private String ciphertext;
}
