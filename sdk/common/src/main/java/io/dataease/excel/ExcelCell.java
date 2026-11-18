package io.dataease.excel;

import lombok.Getter;

import java.math.BigDecimal;

/**
 * 项目自有单元格模型，保留原始列索引、空值与单元格类型。
 */
@Getter
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

    /**
     * 是否为公式单元格；完整单元格模式下由适配层从原始单元格元数据填充。
     */
    private boolean formula;

    public ExcelCell(int columnIndex, ExcelCellType type, String stringValue, BigDecimal numberValue,
                     Boolean booleanValue, String dataFormat) {
        this(columnIndex, type, stringValue, numberValue, booleanValue, dataFormat, false);
    }

    public ExcelCell(int columnIndex, ExcelCellType type, String stringValue, BigDecimal numberValue,
                     Boolean booleanValue, String dataFormat, boolean formula) {
        this.columnIndex = columnIndex;
        this.type = type;
        this.stringValue = stringValue;
        this.numberValue = numberValue;
        this.booleanValue = booleanValue;
        this.dataFormat = dataFormat;
        this.formula = formula;
    }
}
