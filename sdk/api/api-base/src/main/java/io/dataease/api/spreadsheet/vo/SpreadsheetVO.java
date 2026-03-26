package io.dataease.api.spreadsheet.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "电子表格VO")
@Data
public class SpreadsheetVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
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

    @Schema(description = "层级")
    private Integer level;

    @Schema(description = "Univer数据模型JSON")
    private String sheetData;

    @Schema(description = "版本号")
    private Integer version;

    @Schema(description = "状态: 0-未发布, 1-已发布")
    private Integer status;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "创建人姓名")
    private String creator;

    @Schema(description = "更新时间")
    private Long updateTime;

    @Schema(description = "更新人")
    private String updateBy;

    @Schema(description = "更新人姓名")
    private String updater;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "是否收藏")
    private Boolean favorite;
}