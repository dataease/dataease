package io.dataease.api.permissions.auth.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class TargetPerCreator implements Serializable {

    @Serial
    private static final long serialVersionUID = 6469957337188015981L;
    @JsonSerialize(using= ToStringSerializer.class)
    @ApiModelProperty("资源ID集合")
    private List<Long> ids;
}
