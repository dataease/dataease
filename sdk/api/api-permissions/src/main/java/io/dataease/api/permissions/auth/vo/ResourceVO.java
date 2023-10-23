package io.dataease.api.permissions.auth.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.dataease.model.TreeResultModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "资源结点")
public class ResourceVO implements TreeResultModel<ResourceVO>, Serializable {

    @Serial
    private static final long serialVersionUID = -8523999682424087399L;
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "子节点")
    private List<ResourceVO> children;
    @Schema(description = "叶子节点")
    private boolean leaf = false;
    @Schema(description = "额外标识")
    private Integer extraFlag = 0;
}
