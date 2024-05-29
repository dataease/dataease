package io.dataease.api.xpack.share.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "分享列表VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class XpackShareGridVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -1633588323141385486L;

    @Schema(description = "分享ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long shareId;

    @Schema(description = "资源ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long resourceId;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "创建人")
    private String creator;
    @Schema(description = "创建时间")
    private Long time;
    @Schema(description = "有效期")
    private Long exp;
    @Schema(description = "权重")
    private Integer weight;

    private Integer extFlag;

    @Schema(description = "类型")
    private String type;

}
