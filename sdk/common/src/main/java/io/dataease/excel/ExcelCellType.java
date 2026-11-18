package io.dataease.excel;

/**
 * 项目自有 Excel 单元格类型，不直接暴露 Fesod 的 CellDataTypeEnum。
 */
public enum ExcelCellType {

    STRING,
    NUMBER,
    BOOLEAN,
    EMPTY,
    ERROR,
    FORMULA,
    OTHER
}
