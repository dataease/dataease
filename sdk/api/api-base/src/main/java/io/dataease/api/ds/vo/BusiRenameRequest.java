package io.dataease.api.ds.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class BusiRenameRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1175287571828910222L;

    @JsonSerialize(using= ToStringSerializer.class)
    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;
    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
    @Schema(description = "节点类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String  nodeType;
    @Schema(description = "操作类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String  action;

}
