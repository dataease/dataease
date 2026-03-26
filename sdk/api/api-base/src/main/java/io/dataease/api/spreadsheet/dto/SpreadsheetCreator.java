package io.dataease.api.spreadsheet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "电子表格创建DTO")
@Data
public class SpreadsheetCreator implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "父目录ID")
    private Long pid;

    @Schema(description = "节点类型: folder/sheet")
    private String nodeType;

    @Schema(description = "所属组织ID")
    private Long orgId;
}