package io.dataease.api.permissions.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
@Schema(description = "第三方平台用户构造器")
@EqualsAndHashCode(callSuper = true)
@Data
public class PlatformUserCreator extends UserCreator implements Serializable {
    @Schema(description = "用户来源")
    private int origin;
}
