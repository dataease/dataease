package io.dataease.api.permissions.org.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Schema(description = "组织VO")
@Data
public class MountedVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -7642741925705465785L;

    @Schema(description = "ID")
    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "只读")
    private boolean readOnly = true;
    @Schema(description = "子集")
    private List<MountedVO> children;
}
