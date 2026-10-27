package io.dataease.excel;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 项目自有单元格模型，保留原始列索引、空值与单元格类型。
 */
@Getter
@AllArgsConstructor
public class ExcelCell {

    private int columnIndex;

    /**
     * 数据行回调中可能为 null（此时以 stringValue 为准）。
     */
    private ExcelCellType type;

    private String stringValue;

    private BigDecimal numberValue;

    private Boolean booleanValue;

    /**
     * Excel 单元格格式，如 "@"、"yyyy-MM-dd"、"General"；可能为 null。
     */
    private String dataFormat;
}
