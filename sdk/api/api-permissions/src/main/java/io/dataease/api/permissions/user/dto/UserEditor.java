package io.dataease.api.permissions.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户编辑器")
@Data
public class UserEditor extends UserCreator{

    @Serial
    private static final long serialVersionUID = 1580870660998152922L;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;
}
