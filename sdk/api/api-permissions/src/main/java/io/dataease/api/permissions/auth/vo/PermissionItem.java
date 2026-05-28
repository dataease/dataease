package io.dataease.api.permissions.auth.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Schema(description = "权限项")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PermissionItem extends BasePermissionItem implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "组织ID")
    private Long oid;
}
