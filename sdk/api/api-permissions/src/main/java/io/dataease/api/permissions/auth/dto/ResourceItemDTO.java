package io.dataease.api.permissions.auth.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
public class ResourceItemDTO implements Serializable {

    @JsonSerialize(using= ToStringSerializer.class)
    @Schema(description = "资源ID")
    private Long id;
    @JsonSerialize(using= ToStringSerializer.class)
    @Schema(description = "组织ID")
    private Long oid;
}
