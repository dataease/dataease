package io.dataease.api.permissions.org.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "组织编辑器")
@Data
public class OrgEditor implements Serializable {

    @Serial
    private static final long serialVersionUID = -5571486179570725994L;
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "组织名称")
    private String name;
}
