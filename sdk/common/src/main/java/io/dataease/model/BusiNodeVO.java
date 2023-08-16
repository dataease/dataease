package io.dataease.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Schema(description = "业务资源结点")
@Data
public class BusiNodeVO implements TreeResultModel<BusiNodeVO>, Serializable {


    @Serial
    private static final long serialVersionUID = 8191619596741217494L;

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "是否叶子")
    private Boolean leaf;
    @Schema(description = "权重")
    private Integer weight;
    @Schema(description = "额外标识")
    private int extraFlag;
    @Schema(description = "类型")
    private String type;
    @Schema(description = "子节点")
    private List<BusiNodeVO> children;
}
