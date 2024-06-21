package io.dataease.api.xpack.share.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "分享代理信息VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class XpackShareProxyVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 6520351268758198483L;

    @Schema(description = "资源ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long resourceId;

    @Schema(description = "用户ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long uid;
    @Schema(description = "有效期")
    private boolean exp;
    @Schema(description = "密码验证生效")
    private boolean pwdValid;
    @Schema(description = "类型")
    private String type;
    private boolean inIframeError = true;

    private TicketValidVO ticketValidVO;
}
