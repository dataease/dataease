package io.dataease.api.xpack.share.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "分享详情VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class XpackShareVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 7364165756855382682L;

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "分享ID")
    private Long id;
    @Schema(description = "分享有效期")
    private Long exp;
    @Schema(description = "分享UUID")
    private String uuid;
    @Schema(description = "分享密码")
    private String pwd;
    @Schema(description = "自动生成密码")
    private Boolean autoPwd = true;
    @Schema(description = "ticket必须")
    private Boolean ticketRequire = false;
}
