package io.dataease.api.spreadsheet.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.dataease.model.TreeResultModel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Schema(description = "电子表格树节点VO")
@Data
public class SpreadsheetTreeVO implements Serializable, TreeResultModel<SpreadsheetTreeVO> {

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

    @Schema(description = "是否叶子节点")
    private Boolean leaf;

    @Schema(description = "层级")
    private Integer level;

    @Schema(description = "状态: 0-未发布, 1-已发布")
    private Integer status;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "子节点")
    private List<SpreadsheetTreeVO> children;

    @Override
    public void setChildren(List<SpreadsheetTreeVO> children) {
        this.children = children;
    }
}