package io.dataease.excel;

import java.util.Map;

/**
 * 无模型读取回调。表头回调保留原始单元格类型，数据行回调以 stringValue 为准。
 */
public interface ExcelReadHandler {

    default void onHeader(Map<Integer, ExcelCell> cells) {
    }

    void onRow(int rowIndex, Map<Integer, ExcelCell> cells);
}
