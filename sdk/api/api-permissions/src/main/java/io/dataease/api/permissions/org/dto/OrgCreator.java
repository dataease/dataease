package io.dataease.api.permissions.org.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "组织构造器")
@Data
public class OrgCreator implements Serializable {

    @Serial
    private static final long serialVersionUID = -4246980891732805368L;

    private Long id;
    @Schema(description = "组织名称")
    private String name;
    @Schema(description = "上级ID")
    private Long pid;
}
