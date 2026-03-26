package io.dataease.api.spreadsheet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Schema(description = "电子表格编辑DTO")
@Data
public class SpreadsheetEditor implements Serializable {

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

    @Schema(description = "Univer数据模型JSON")
    private String sheetData;

    @Schema(description = "版本号")
    private Integer version;

    @Schema(description = "状态: 0-未发布, 1-已发布")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}