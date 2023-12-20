package io.dataease.api.permissions.auth.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
@Schema(description = "权限构造器")
@Data
public class TargetPerCreator implements Serializable {

    @Serial
    private static final long serialVersionUID = 6469957337188015981L;
    @JsonSerialize(using= ToStringSerializer.class)
    @Schema(description = "权限ID集合")
    private List<Long> ids;
}
