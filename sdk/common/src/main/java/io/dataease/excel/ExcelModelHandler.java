package io.dataease.excel;

/**
 * 模型读取回调，rowIndex 为 0 基行号（表头行同样回调）。
 */
public interface ExcelModelHandler<T> {

    void onRow(int rowIndex, T data);

    default void onFinished() {
    }
}
